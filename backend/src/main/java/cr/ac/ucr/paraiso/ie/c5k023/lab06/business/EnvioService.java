package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvioService {

        private final EnvioRepository envioRepository;
        private final VehiculoRepository vehiculoRepository;
        private final ConductorRepository conductorRepository;
        private final UsuarioRepository usuarioRepository;
        private final BitacoraEnvioRepository bitacoraEnvioRepository;

        public EnvioService(EnvioRepository envioRepository,
                        VehiculoRepository vehiculoRepository,
                        ConductorRepository conductorRepository,
                        UsuarioRepository usuarioRepository,
                        BitacoraEnvioRepository bitacoraEnvioRepository) {
                this.envioRepository = envioRepository;
                this.vehiculoRepository = vehiculoRepository;
                this.conductorRepository = conductorRepository;
                this.usuarioRepository = usuarioRepository;
                this.bitacoraEnvioRepository = bitacoraEnvioRepository;
        }

        @Transactional(readOnly = true)
        public List<EnvioResponseDTO> obtenerEnviosOptimizados() {
                return envioRepository.findAllOptimizado().stream()
                                .map(this::aResponseDTO)
                                .toList();
        }

        @Transactional
        public EnvioResponseDTO registrarEnvio(EnvioRequestDTO request) {
                Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Vehículo no encontrado con id " + request.getVehiculoId()));

                Conductor conductor = conductorRepository.findById(request.getConductorId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conductor no encontrado con id " + request.getConductorId()));

                if (request.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
                        throw new IllegalArgumentException(
                                        "El peso del envío (" + request.getPesoKg() +
                                                        " kg) supera la capacidad del vehículo (" +
                                                        vehiculo.getCapacidadKg() + " kg)");
                }

                Envio envio = new Envio();
                envio.setCodigoRastreo(request.getCodigoRastreo());
                envio.setDireccionDestino(request.getDireccionDestino());
                envio.setPesoKg(request.getPesoKg());
                envio.setCosto(request.getCosto());
                envio.setVehiculo(vehiculo);
                envio.setConductor(conductor);
                envio.setEstadoEnvio("PENDIENTE");

                Envio guardado = envioRepository.save(envio);
                return aResponseDTO(guardado);
        }

        @Transactional
        public EnvioResponseDTO actualizarEstado(Integer envioId, CambioEstadoDTO cambio) {
                Envio envio = envioRepository.findById(envioId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Envío no encontrado con id " + envioId));

                String estadoAnterior = envio.getEstadoEnvio();
                String estadoNuevo = cambio.getNuevoEstado();

                // Regla de negocio (reto autónomo): un envío ENTREGADO o CANCELADO
                // no puede volver a un estado temprano (PENDIENTE / EN_TRANSITO).
                List<String> estadosFinales = List.of("ENTREGADO", "CANCELADO");
                List<String> estadosTempranos = List.of("PENDIENTE", "EN_TRANSITO");

                if (estadosFinales.contains(estadoAnterior) && estadosTempranos.contains(estadoNuevo)) {
                        throw new InvalidStateTransitionException(
                                        "Transición de estado no permitida para el envío " + envio.getCodigoRastreo());

                }

                envio.setEstadoEnvio(estadoNuevo);

                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Usuario usuario = usuarioRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

                BitacoraEnvio bitacora = new BitacoraEnvio();
                bitacora.setEnvio(envio);
                bitacora.setEstadoAnterior(estadoAnterior);
                bitacora.setEstadoNuevo(estadoNuevo);
                bitacora.setFechaCambio(LocalDateTime.now());
                bitacora.setUsuario(usuario);
                bitacora.setObservaciones(cambio.getObservaciones());
                bitacoraEnvioRepository.save(bitacora);

                return aResponseDTO(envio);
        }

        @Transactional(readOnly = true)
        public List<BitacoraResponseDTO> obtenerBitacora(Integer envioId) {
                return bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(envioId).stream()
                                .map(b -> new BitacoraResponseDTO(
                                                b.getId(), b.getEstadoAnterior(), b.getEstadoNuevo(),
                                                b.getFechaCambio(), b.getUsuario().getUsername(), b.getObservaciones()))
                                .toList();
        }

        private EnvioResponseDTO aResponseDTO(Envio envio) {
                return new EnvioResponseDTO(
                                envio.getId(), envio.getCodigoRastreo(), envio.getDireccionDestino(),
                                envio.getPesoKg(), envio.getCosto(), envio.getEstadoEnvio(),
                                envio.getVehiculo().getPlaca(),
                                envio.getConductor().getNombre() + " " + envio.getConductor().getApellidos());
        }
}
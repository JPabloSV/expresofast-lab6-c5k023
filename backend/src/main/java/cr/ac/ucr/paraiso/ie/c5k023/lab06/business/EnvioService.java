package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnvioService {

    /** Estados finales: ya no admiten retroceso a estados tempranos. */
    private static final List<String> ESTADOS_FINALES = List.of("ENTREGADO", "CANCELADO");
    /** Estados tempranos del ciclo de vida de un envio. */
    private static final List<String> ESTADOS_TEMPRANOS = List.of("PENDIENTE", "EN_TRANSITO");

    /** Tarifa base fija por gestion logistica (colones). */
    private static final double TARIFA_BASE = 1000.0;
    /** Costo por kilogramo dentro del tramo normal. */
    private static final double COSTO_POR_KG = 100.0;
    /** Umbral de peso a partir del cual aplica recargo por carga pesada. */
    private static final double UMBRAL_SOBREPESO_KG = 50.0;
    /** Costo por kilogramo del excedente sobre el umbral (recargo del 15%). */
    private static final double COSTO_POR_KG_EXCEDENTE = 115.0;
    /** Costo por kilometro recorrido. */
    private static final double COSTO_POR_KM = 100.0;

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

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerPorId(Integer envioId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Envio no encontrado con id " + envioId));
        return aResponseDTO(envio);
    }

    @Transactional
    public EnvioResponseDTO registrarEnvio(EnvioRequestDTO request) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehiculo no encontrado con id " + request.getVehiculoId()));

        Conductor conductor = conductorRepository.findById(request.getConductorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conductor no encontrado con id " + request.getConductorId()));

        if (request.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new CapacidadExcedidaException(
                    "El peso del envio (" + request.getPesoKg() +
                            " kg) supera la capacidad del vehiculo " + vehiculo.getPlaca() +
                            " (" + vehiculo.getCapacidadKg() + " kg)");
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
                        "Envio no encontrado con id " + envioId));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = cambio.getNuevoEstado();

        // Regla de negocio: un envio ENTREGADO o CANCELADO no puede volver
        // a un estado temprano (PENDIENTE / EN_TRANSITO).
        if (ESTADOS_FINALES.contains(estadoAnterior) && ESTADOS_TEMPRANOS.contains(estadoNuevo)) {
            throw new InvalidStateTransitionException(
                    "Transicion de estado no permitida para el envio " + envio.getCodigoRastreo());
        }

        envio.setEstadoEnvio(estadoNuevo);
        registrarBitacora(envio, estadoAnterior, estadoNuevo, cambio.getObservaciones());

        return aResponseDTO(envio);
    }

    /**
     * Cancela un envio. Solo se permite cancelar envios que aun no han
     * salido a ruta; un envio EN_TRANSITO, ENTREGADO o ya CANCELADO
     * no puede cancelarse.
     */
    @Transactional
    public EnvioResponseDTO cancelarEnvio(Integer envioId, String observaciones) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Envio no encontrado con id " + envioId));

        String estadoAnterior = envio.getEstadoEnvio();

        if (!"PENDIENTE".equals(estadoAnterior)) {
            throw new InvalidStateTransitionException(
                    "No se puede cancelar el envio " + envio.getCodigoRastreo() +
                            " porque se encuentra en estado " + estadoAnterior);
        }

        envio.setEstadoEnvio("CANCELADO");
        registrarBitacora(envio, estadoAnterior, "CANCELADO", observaciones);

        return aResponseDTO(envio);
    }

    /**
     * Calcula la tarifa de un envio a partir del peso y la distancia.
     *
     * Formula: tarifa base + componente de peso + componente de distancia.
     * El componente de peso cobra los primeros 50 kg a la tarifa normal y
     * el excedente con un recargo del 15% por carga pesada.
     *
     * @param pesoKg      peso del paquete en kilogramos
     * @param distanciaKm distancia del trayecto en kilometros
     * @return tarifa total en colones
     */
    public double calcularTarifa(double pesoKg, double distanciaKm) {
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a cero");
        }
        if (distanciaKm <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor a cero");
        }

        double componentePeso;
        if (pesoKg <= UMBRAL_SOBREPESO_KG) {
            componentePeso = pesoKg * COSTO_POR_KG;
        } else {
            double excedente = pesoKg - UMBRAL_SOBREPESO_KG;
            componentePeso = (UMBRAL_SOBREPESO_KG * COSTO_POR_KG)
                    + (excedente * COSTO_POR_KG_EXCEDENTE);
        }

        double componenteDistancia = distanciaKm * COSTO_POR_KM;

        return TARIFA_BASE + componentePeso + componenteDistancia;
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacora(Integer envioId) {
        return bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(envioId).stream()
                .map(b -> new BitacoraResponseDTO(
                        b.getId(), b.getEstadoAnterior(), b.getEstadoNuevo(),
                        b.getFechaCambio(), b.getUsuario().getUsername(), b.getObservaciones()))
                .toList();
    }

    /** Registra en la bitacora el cambio de estado junto al usuario autenticado. */
    private void registrarBitacora(Envio envio, String estadoAnterior,
            String estadoNuevo, String observaciones) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuario);
        bitacora.setObservaciones(observaciones);
        bitacoraEnvioRepository.save(bitacora);
    }

    private EnvioResponseDTO aResponseDTO(Envio envio) {
        return new EnvioResponseDTO(
                envio.getId(), envio.getCodigoRastreo(), envio.getDireccionDestino(),
                envio.getPesoKg(), envio.getCosto(), envio.getEstadoEnvio(),
                envio.getVehiculo().getPlaca(),
                envio.getConductor().getNombre() + " " + envio.getConductor().getApellidos());
    }

    @Transactional(readOnly = true)
    public Page<EnvioDTO> listarPaginado(int page, int size, String sortBy, String dir,
            String busqueda, String estado) {
        String campoOrden = (sortBy != null && !sortBy.isBlank()) ? sortBy : "fechaCreacion";
        Sort.Direction direccion = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direccion, campoOrden));
        return envioRepository.buscarPaginado(busqueda, estado, pageable)
                .map(this::aEnvioDTO);
    }

    @Transactional(readOnly = true)
    public List<EnvioDTO> listarViaStoredProcedure(String estado) {
        return envioRepository.obtenerEnviosPorEstadoSP(estado).stream()
                .map(this::aEnvioDTO)
                .toList();
    }

    private EnvioDTO aEnvioDTO(Envio envio) {
        return new EnvioDTO(
                envio.getId(), envio.getCodigoRastreo(), envio.getDestinatario(),
                envio.getDireccionDestino(), envio.getCosto(), envio.getEstadoEnvio(),
                envio.getFechaCreacion());
    }
}

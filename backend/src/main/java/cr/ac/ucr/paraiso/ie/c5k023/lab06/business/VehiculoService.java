package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio para la gestion de la flota de vehiculos:
 * alta con validacion de placa unica y asignacion de conductores.
 */
@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           ConductorRepository conductorRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listar() {
        return vehiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehiculo obtenerPorId(Integer id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehiculo no encontrado con id " + id));
    }

    /**
     * Registra un vehiculo nuevo validando que la placa no exista ya
     * en la flota.
     *
     * @throws DuplicateResourceException si la placa ya esta registrada
     */
    @Transactional
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new DuplicateResourceException(
                    "Ya existe un vehiculo registrado con la placa " + vehiculo.getPlaca());
        }
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo actualizar(Integer id, Vehiculo cambios) {
        Vehiculo vehiculo = obtenerPorId(id);
        vehiculo.setPlaca(cambios.getPlaca());
        vehiculo.setCapacidadKg(cambios.getCapacidadKg());
        vehiculo.setEstado(cambios.getEstado());
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehiculo no encontrado con id " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    /**
     * Asigna un conductor a un vehiculo de la flota. Solo se admiten
     * conductores activos.
     *
     * @throws ResourceNotFoundException si el vehiculo o el conductor no existen
     * @throws IllegalStateException     si el conductor esta inactivo
     */
    @Transactional
    public Vehiculo asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehiculo no encontrado con id " + vehiculoId));

        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conductor no encontrado con id " + conductorId));

        if (!Boolean.TRUE.equals(conductor.getActivo())) {
            throw new IllegalStateException(
                    "El conductor " + conductor.getNombre() + " " + conductor.getApellidos() +
                            " se encuentra inactivo y no puede ser asignado");
        }

        vehiculo.setConductorAsignado(conductor);
        return vehiculoRepository.save(vehiculo);
    }
}

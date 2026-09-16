package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias aisladas de la gestion de la flota de vehiculos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehiculoService - Pruebas unitarias")
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private ConductorRepository conductorRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private Vehiculo vehiculo;
    private Conductor conductor;

    @BeforeEach
    void prepararDatos() {
        vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("CL-123456");
        vehiculo.setCapacidadKg(new BigDecimal("1500.00"));
        vehiculo.setEstado("DISPONIBLE");

        conductor = new Conductor();
        conductor.setId(1);
        conductor.setNombre("Luis");
        conductor.setApellidos("Fernandez Mora");
        conductor.setActivo(true);
    }

    @Test
    @DisplayName("Debe registrar el vehiculo cuando la placa no existe")
    void registrarVehiculo_PlacaNueva_RegistraCorrectamente() {
        when(vehiculoRepository.existsByPlaca("CL-123456")).thenReturn(false);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehiculo resultado = vehiculoService.registrarVehiculo(vehiculo);

        assertNotNull(resultado);
        assertEquals("CL-123456", resultado.getPlaca());
        verify(vehiculoRepository, times(1)).save(vehiculo);
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException cuando la placa ya existe")
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        when(vehiculoRepository.existsByPlaca("CL-123456")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> vehiculoService.registrarVehiculo(vehiculo));

        assertTrue(ex.getMessage().contains("CL-123456"));
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe asignar un conductor activo al vehiculo")
    void asignarConductor_ConductorActivo_AsignaCorrectamente() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehiculo resultado = vehiculoService.asignarConductor(1, 1);

        assertNotNull(resultado.getConductorAsignado());
        assertEquals("Luis", resultado.getConductorAsignado().getNombre());
    }

    @Test
    @DisplayName("No debe permitir asignar un conductor inactivo")
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        conductor.setActivo(false);
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> vehiculoService.asignarConductor(1, 1));

        assertTrue(ex.getMessage().contains("inactivo"));
        assertNull(vehiculo.getConductorAsignado());
        verify(vehiculoRepository, never()).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el vehiculo no existe al asignar")
    void asignarConductor_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.asignarConductor(99, 1));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el conductor no existe al asignar")
    void asignarConductor_ConductorInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.asignarConductor(1, 99));
    }

    @Test
    @DisplayName("Debe retornar el vehiculo solicitado por identificador")
    void obtenerPorId_VehiculoExistente_RetornaVehiculo() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));

        Vehiculo resultado = vehiculoService.obtenerPorId(1);

        assertEquals("CL-123456", resultado.getPlaca());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el vehiculo no existe")
    void obtenerPorId_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.obtenerPorId(99));
    }

    @Test
    @DisplayName("Debe retornar la flota completa de vehiculos")
    void listar_RetornaFlotaCompleta() {
        when(vehiculoRepository.findAll()).thenReturn(List.of(vehiculo));

        List<Vehiculo> resultado = vehiculoService.listar();

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Debe actualizar los datos del vehiculo existente")
    void actualizar_VehiculoExistente_ActualizaDatos() {
        Vehiculo cambios = new Vehiculo();
        cambios.setPlaca("SJ-999999");
        cambios.setCapacidadKg(new BigDecimal("2000.00"));
        cambios.setEstado("EN_MANTENIMIENTO");

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehiculo resultado = vehiculoService.actualizar(1, cambios);

        assertEquals("SJ-999999", resultado.getPlaca());
        assertEquals("EN_MANTENIMIENTO", resultado.getEstado());
    }

    @Test
    @DisplayName("Debe eliminar el vehiculo cuando existe")
    void eliminar_VehiculoExistente_EliminaCorrectamente() {
        when(vehiculoRepository.existsById(1)).thenReturn(true);

        vehiculoService.eliminar(1);

        verify(vehiculoRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al eliminar un vehiculo inexistente")
    void eliminar_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.eliminar(99));
        verify(vehiculoRepository, never()).deleteById(anyInt());
    }
}

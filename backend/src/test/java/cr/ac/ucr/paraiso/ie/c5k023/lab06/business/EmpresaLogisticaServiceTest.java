package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias aisladas de la gestion de empresas logisticas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmpresaLogisticaService - Pruebas unitarias")
class EmpresaLogisticaServiceTest {

    @Mock
    private EmpresaLogisticaRepository empresaRepository;

    @InjectMocks
    private EmpresaLogisticaService empresaService;

    private EmpresaLogistica empresa;

    @BeforeEach
    void prepararDatos() {
        empresa = new EmpresaLogistica();
        empresa.setId(1);
        empresa.setNombre("ExpresoFast Costa Rica");
        empresa.setCedulaJuridica("3-101-987654");
        empresa.setTelefono("2200-1234");
    }

    @Test
    @DisplayName("Debe registrar la empresa y asignar la fecha de registro automaticamente")
    void registrarEmpresa_DatosValidos_RegistraCorrectamente() {
        when(empresaRepository.existsByCedulaJuridica("3-101-987654")).thenReturn(false);
        when(empresaRepository.save(any(EmpresaLogistica.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpresaLogistica resultado = empresaService.registrarEmpresa(empresa);

        assertNotNull(resultado.getFechaRegistro());
        assertEquals("ExpresoFast Costa Rica", resultado.getNombre());
        verify(empresaRepository, times(1)).save(empresa);
    }

    @Test
    @DisplayName("Debe respetar la fecha de registro cuando ya viene informada")
    void registrarEmpresa_ConFecha_ConservaLaFecha() {
        LocalDateTime fecha = LocalDateTime.of(2026, 1, 15, 10, 30);
        empresa.setFechaRegistro(fecha);
        when(empresaRepository.existsByCedulaJuridica("3-101-987654")).thenReturn(false);
        when(empresaRepository.save(any(EmpresaLogistica.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpresaLogistica resultado = empresaService.registrarEmpresa(empresa);

        assertEquals(fecha, resultado.getFechaRegistro());
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si la cedula juridica ya existe")
    void registrarEmpresa_CedulaDuplicada_LanzaExcepcion() {
        when(empresaRepository.existsByCedulaJuridica("3-101-987654")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> empresaService.registrarEmpresa(empresa));

        assertTrue(ex.getMessage().contains("3-101-987654"));
        verify(empresaRepository, never()).save(any(EmpresaLogistica.class));
    }

    @Test
    @DisplayName("Debe retornar la empresa solicitada por identificador")
    void obtenerPorId_EmpresaExistente_RetornaEmpresa() {
        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa));

        EmpresaLogistica resultado = empresaService.obtenerPorId(1);

        assertEquals("ExpresoFast Costa Rica", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la empresa no existe")
    void obtenerPorId_EmpresaInexistente_LanzaExcepcion() {
        when(empresaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.obtenerPorId(99));
    }

    @Test
    @DisplayName("Debe retornar el listado de empresas registradas")
    void listar_RetornaEmpresas() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa));

        List<EmpresaLogistica> resultado = empresaService.listar();

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Debe actualizar los datos de la empresa existente")
    void actualizar_EmpresaExistente_ActualizaDatos() {
        EmpresaLogistica cambios = new EmpresaLogistica();
        cambios.setNombre("ExpresoFast Internacional");
        cambios.setTelefono("2200-9999");

        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(EmpresaLogistica.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpresaLogistica resultado = empresaService.actualizar(1, cambios);

        assertEquals("ExpresoFast Internacional", resultado.getNombre());
        assertEquals("2200-9999", resultado.getTelefono());
    }

    @Test
    @DisplayName("Debe eliminar la empresa cuando existe")
    void eliminar_EmpresaExistente_EliminaCorrectamente() {
        when(empresaRepository.existsById(1)).thenReturn(true);

        empresaService.eliminar(1);

        verify(empresaRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al eliminar una empresa inexistente")
    void eliminar_EmpresaInexistente_LanzaExcepcion() {
        when(empresaRepository.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> empresaService.eliminar(99));
        verify(empresaRepository, never()).deleteById(anyInt());
    }
}

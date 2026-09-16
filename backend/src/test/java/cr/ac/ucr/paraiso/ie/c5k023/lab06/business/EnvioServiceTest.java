package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.*;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias aisladas de la logica de negocio de envios.
 * Todas las dependencias de persistencia se sustituyen por mocks.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnvioService - Pruebas unitarias")
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private ConductorRepository conductorRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BitacoraEnvioRepository bitacoraEnvioRepository;

    @InjectMocks
    private EnvioService envioService;

    private Vehiculo vehiculo;
    private Conductor conductor;
    private Usuario usuario;

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

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("admin");
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String username) {
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(username, null, List.of())));
    }

    private EnvioRequestDTO construirRequest(String peso) {
        EnvioRequestDTO request = new EnvioRequestDTO();
        request.setCodigoRastreo("EXP-1234");
        request.setDireccionDestino("Cartago, Paraiso centro");
        request.setPesoKg(new BigDecimal(peso));
        request.setCosto(new BigDecimal("8500.00"));
        request.setVehiculoId(1);
        request.setConductorId(1);
        return request;
    }

    private Envio construirEnvio(Integer id, String estado) {
        Envio envio = new Envio();
        envio.setId(id);
        envio.setCodigoRastreo("EXP-1234");
        envio.setDireccionDestino("Cartago, Paraiso centro");
        envio.setPesoKg(new BigDecimal("12.50"));
        envio.setCosto(new BigDecimal("8500.00"));
        envio.setEstadoEnvio(estado);
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        return envio;
    }

    // ------------------------------------------------------------------
    // Creacion de envios
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Debe crear el envio y asignar el estado inicial PENDIENTE")
    void crearEnvio_DatosValidos_RetornaEnvioDTO() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioResponseDTO resultado = envioService.registrarEnvio(construirRequest("12.50"));

        assertNotNull(resultado);
        assertEquals("EXP-1234", resultado.codigoRastreo());
        assertEquals("PENDIENTE", resultado.estadoEnvio());
        assertEquals("CL-123456", resultado.placaVehiculo());
        assertEquals("Luis Fernandez Mora", resultado.nombreConductor());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar CapacidadExcedidaException si el peso supera la capacidad del vehiculo")
    void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
        vehiculo.setCapacidadKg(new BigDecimal("100.00"));
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        CapacidadExcedidaException ex = assertThrows(CapacidadExcedidaException.class,
                () -> envioService.registrarEnvio(construirRequest("500.00")));

        assertTrue(ex.getMessage().contains("supera la capacidad"));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el vehiculo no existe")
    void crearEnvio_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.registrarEnvio(construirRequest("12.50")));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el conductor no existe")
    void crearEnvio_ConductorInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.registrarEnvio(construirRequest("12.50")));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    // ------------------------------------------------------------------
    // Cambios de estado y bitacora
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Un envio ENTREGADO no puede regresar a EN_TRANSITO")
    void actualizarEstado_TransicionInvalida_LanzaExcepcion() {
        Envio envio = construirEnvio(1, "ENTREGADO");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");

        InvalidStateTransitionException ex = assertThrows(InvalidStateTransitionException.class,
                () -> envioService.actualizarEstado(1, cambio));

        assertTrue(ex.getMessage().contains("EXP-1234"));
        verify(bitacoraEnvioRepository, never()).save(any(BitacoraEnvio.class));
    }

    @Test
    @DisplayName("Debe actualizar el estado y registrar el cambio en la bitacora")
    void actualizarEstado_TransicionValida_RegistraBitacora() {
        autenticarComo("admin");
        Envio envio = construirEnvio(1, "PENDIENTE");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");
        cambio.setObservaciones("Salida de bodega central");

        EnvioResponseDTO resultado = envioService.actualizarEstado(1, cambio);

        assertEquals("EN_TRANSITO", resultado.estadoEnvio());
        verify(bitacoraEnvioRepository, times(1)).save(any(BitacoraEnvio.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el usuario autenticado no existe")
    void actualizarEstado_UsuarioInexistente_LanzaExcepcion() {
        autenticarComo("fantasma");
        Envio envio = construirEnvio(1, "PENDIENTE");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.actualizarEstado(1, cambio));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al actualizar un envio inexistente")
    void actualizarEstado_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.actualizarEstado(99, cambio));
    }

    // ------------------------------------------------------------------
    // Cancelacion de envios
    // ------------------------------------------------------------------

    @Test
    @DisplayName("No se puede cancelar un envio que ya se encuentra en ruta")
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
        Envio envio = construirEnvio(1, "EN_TRANSITO");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        InvalidStateTransitionException ex = assertThrows(InvalidStateTransitionException.class,
                () -> envioService.cancelarEnvio(1, "Cliente desistio"));

        assertTrue(ex.getMessage().contains("EN_TRANSITO"));
        assertEquals("EN_TRANSITO", envio.getEstadoEnvio());
        verify(bitacoraEnvioRepository, never()).save(any(BitacoraEnvio.class));
    }

    @Test
    @DisplayName("Debe cancelar un envio PENDIENTE y registrarlo en la bitacora")
    void cancelarEnvio_EnvioPendiente_CancelaCorrectamente() {
        autenticarComo("admin");
        Envio envio = construirEnvio(1, "PENDIENTE");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        EnvioResponseDTO resultado = envioService.cancelarEnvio(1, "Cliente desistio");

        assertEquals("CANCELADO", resultado.estadoEnvio());
        verify(bitacoraEnvioRepository, times(1)).save(any(BitacoraEnvio.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al cancelar un envio inexistente")
    void cancelarEnvio_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.cancelarEnvio(99, "Sin motivo"));
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar el envio solicitado por identificador")
    void obtenerPorId_EnvioExistente_RetornaDTO() {
        when(envioRepository.findById(1)).thenReturn(Optional.of(construirEnvio(1, "PENDIENTE")));

        EnvioResponseDTO resultado = envioService.obtenerPorId(1);

        assertEquals("EXP-1234", resultado.codigoRastreo());
        assertEquals("PENDIENTE", resultado.estadoEnvio());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el envio no existe")
    void obtenerPorId_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.obtenerPorId(99));
    }

    @Test
    @DisplayName("Debe retornar el listado optimizado de envios")
    void obtenerEnviosOptimizados_RetornaListaDTO() {
        when(envioRepository.findAllOptimizado())
                .thenReturn(List.of(construirEnvio(1, "PENDIENTE"), construirEnvio(2, "EN_TRANSITO")));

        List<EnvioResponseDTO> resultado = envioService.obtenerEnviosOptimizados();

        assertEquals(2, resultado.size());
        assertEquals("CL-123456", resultado.get(0).placaVehiculo());
    }

    @Test
    @DisplayName("Debe retornar el historial de bitacora de un envio")
    void obtenerBitacora_RetornaHistorial() {
        BitacoraEnvio registro = new BitacoraEnvio();
        registro.setId(1);
        registro.setEstadoAnterior("PENDIENTE");
        registro.setEstadoNuevo("EN_TRANSITO");
        registro.setFechaCambio(LocalDateTime.now());
        registro.setUsuario(usuario);
        registro.setObservaciones("Salida de bodega");

        when(bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(1))
                .thenReturn(List.of(registro));

        List<BitacoraResponseDTO> resultado = envioService.obtenerBitacora(1);

        assertEquals(1, resultado.size());
        assertEquals("admin", resultado.get(0).usuario());
        assertEquals("EN_TRANSITO", resultado.get(0).estadoNuevo());
    }

    // ------------------------------------------------------------------
    // Calculo de tarifas (pruebas parametrizadas)
    // ------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "5.0, 10.0, 2500.0",
            "15.0, 50.0, 7500.0",
            "100.0, 2.5, 12000.0"
    })
    @DisplayName("Debe calcular la tarifa correcta segun peso y distancia")
    void calcularTarifa_CasosVariados_CalculaCorrectamente(
            double pesoKg, double distanciaKm, double tarifaEsperada) {

        double tarifaCalculada = envioService.calcularTarifa(pesoKg, distanciaKm);

        assertEquals(tarifaEsperada, tarifaCalculada, 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 10.0",
            "-5.0, 10.0"
    })
    @DisplayName("Debe rechazar pesos menores o iguales a cero")
    void calcularTarifa_PesoInvalido_LanzaExcepcion(double pesoKg, double distanciaKm) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> envioService.calcularTarifa(pesoKg, distanciaKm));

        assertTrue(ex.getMessage().contains("peso"));
    }

    @ParameterizedTest
    @CsvSource({
            "10.0, 0.0",
            "10.0, -3.0"
    })
    @DisplayName("Debe rechazar distancias menores o iguales a cero")
    void calcularTarifa_DistanciaInvalida_LanzaExcepcion(double pesoKg, double distanciaKm) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> envioService.calcularTarifa(pesoKg, distanciaKm));

        assertTrue(ex.getMessage().contains("distancia"));
    }
}

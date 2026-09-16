package cr.ac.ucr.paraiso.ie.c5k023.lab06.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.config.SecurityConfig;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EnvioController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EnvioController - Pruebas de capa web")
class EnvioControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
        private EnvioService envioService;

        private EnvioResponseDTO envioEjemplo() {
                return new EnvioResponseDTO(
                                1,
                                "EXP-1234",
                                "Cartago, Paraiso centro",
                                new BigDecimal("12.50"),
                                new BigDecimal("8500.00"),
                                "PENDIENTE",
                                "CL-123456",
                                "Luis Fernandez Mora");
        }

        @Test
        @DisplayName("GET /api/envios/{id} exitoso retorna 200 con los datos del envio")
        void obtenerEnvio_Existente_Retorna200() throws Exception {
                when(envioService.obtenerPorId(1)).thenReturn(envioEjemplo());

                mockMvc.perform(get("/api/envios/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"))
                                .andExpect(jsonPath("$.estadoEnvio").value("PENDIENTE"))
                                .andExpect(jsonPath("$.placaVehiculo").value("CL-123456"));
        }

        @Test
        @DisplayName("GET /api/envios/{id} inexistente retorna 404 en formato RFC 7807")
        void obtenerEnvio_Inexistente_Retorna404() throws Exception {
                when(envioService.obtenerPorId(99))
                                .thenThrow(new ResourceNotFoundException("Envio no encontrado con id 99"));

                mockMvc.perform(get("/api/envios/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.detail").value("Envio no encontrado con id 99"));
        }

        @Test
        @DisplayName("POST /api/envios con payload valido retorna 201 Created")
        void registrarEnvio_PayloadValido_Retorna201() throws Exception {
                EnvioRequestDTO request = new EnvioRequestDTO();
                request.setCodigoRastreo("EXP-1234");
                request.setDireccionDestino("Cartago, Paraiso centro");
                request.setPesoKg(new BigDecimal("12.50"));
                request.setCosto(new BigDecimal("8500.00"));
                request.setVehiculoId(1);
                request.setConductorId(1);

                when(envioService.registrarEnvio(any(EnvioRequestDTO.class))).thenReturn(envioEjemplo());

                mockMvc.perform(post("/api/envios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"));
        }

        @Test
        @DisplayName("POST /api/envios con payload invalido retorna 400 con errores de validacion")
        void registrarEnvio_PayloadInvalido_Retorna400() throws Exception {
                EnvioRequestDTO request = new EnvioRequestDTO();
                request.setCodigoRastreo("");
                request.setDireccionDestino("");
                request.setPesoKg(new BigDecimal("-5.00"));
                request.setCosto(new BigDecimal("-100.00"));

                mockMvc.perform(post("/api/envios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.codigoRastreo").exists())
                                .andExpect(jsonPath("$.direccionDestino").exists())
                                .andExpect(jsonPath("$.pesoKg").exists());
        }

        @Test
        @DisplayName("GET /api/envios/{id}/bitacora retorna 200 con el historial")
        void obtenerBitacora_Retorna200() throws Exception {
                when(envioService.obtenerBitacora(eq(1))).thenReturn(java.util.List.of());

                mockMvc.perform(get("/api/envios/1/bitacora"))
                                .andExpect(status().isOk());
        }
}

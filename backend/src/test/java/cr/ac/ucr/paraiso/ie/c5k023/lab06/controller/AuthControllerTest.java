package cr.ac.ucr.paraiso.ie.c5k023.lab06.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.config.SecurityConfig;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de corte de controlador para el endpoint publico de
 * autenticacion y emision del token JWT.
 */
@WebMvcTest(controllers = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController - Pruebas de capa web")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
        private AuthService authService;

        @Test
        @DisplayName("POST /api/auth/login con credenciales correctas retorna 200 y el token")
        void login_CredencialesCorrectas_Retorna200ConToken() throws Exception {
                AuthResponseDTO respuesta = new AuthResponseDTO(
                                "token-jwt-simulado", "admin", List.of("ROLE_ADMIN"), 3600000L);

                when(authService.login(any(AuthRequestDTO.class))).thenReturn(respuesta);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new AuthRequestDTO("admin", "Password123!"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("token-jwt-simulado"))
                                .andExpect(jsonPath("$.username").value("admin"))
                                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("POST /api/auth/login con credenciales incorrectas retorna 401")
        void login_CredencialesIncorrectas_Retorna401() throws Exception {
                when(authService.login(any(AuthRequestDTO.class)))
                                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new AuthRequestDTO("admin", "clave-incorrecta"))))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.mensaje").value("Credenciales invalidas"));
        }
}

package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Rol;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de autenticacion y emision de tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Pruebas unitarias")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void prepararDatos() {
        ReflectionTestUtils.setField(authService, "expirationMs", 3600000L);

        Rol rolAdmin = new Rol();
        rolAdmin.setId(1);
        rolAdmin.setNombreRol("ROLE_ADMIN");

        Set<Rol> roles = new HashSet<>();
        roles.add(rolAdmin);

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("admin");
        usuario.setNombreCompleto("Carlos Alvarado");
        usuario.setActivo(true);
        usuario.setRoles(roles);
    }

    @Test
    @DisplayName("Debe retornar el token JWT cuando las credenciales son correctas")
    void login_CredencialesValidas_RetornaToken() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generarToken(eq("admin"), anyList())).thenReturn("token-jwt-simulado");

        AuthResponseDTO respuesta = authService.login(new AuthRequestDTO("admin", "Password123!"));

        assertNotNull(respuesta);
        assertEquals("token-jwt-simulado", respuesta.token());
        assertEquals("admin", respuesta.username());
        assertTrue(respuesta.roles().contains("ROLE_ADMIN"));
        assertEquals(3600000L, respuesta.expirationTime());
    }

    @Test
    @DisplayName("Debe propagar BadCredentialsException cuando la autenticacion falla")
    void login_CredencialesInvalidas_LanzaExcepcion() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new AuthRequestDTO("admin", "clave-incorrecta")));

        verify(jwtTokenProvider, never()).generarToken(any(), anyList());
    }

    @Test
    @DisplayName("Debe fallar si el usuario autenticado no existe en la base de datos")
    void login_UsuarioInexistente_LanzaExcepcion() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> authService.login(new AuthRequestDTO("fantasma", "Password123!")));
    }
}

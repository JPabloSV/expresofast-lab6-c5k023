package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public AuthService(AuthenticationManager authenticationManager,
                        JwtTokenProvider jwtTokenProvider,
                        UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        List<String> roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombreRol())
                .collect(Collectors.toList());

        String token = jwtTokenProvider.generarToken(usuario.getUsername(), roles);

        return new AuthResponseDTO(token, usuario.getUsername(), roles, expirationMs);
    }
}
// AuthResponseDTO.java
package cr.ac.ucr.paraiso.ie.c5k023.lab06.dto;

import java.util.List;

public record AuthResponseDTO(String token, String username, List<String> roles, long expirationTime) {}
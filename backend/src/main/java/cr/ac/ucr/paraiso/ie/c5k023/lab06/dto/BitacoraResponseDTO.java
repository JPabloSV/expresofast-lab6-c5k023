package cr.ac.ucr.paraiso.ie.c5k023.lab06.dto;

import java.time.LocalDateTime;

public record BitacoraResponseDTO(
        Integer id,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaCambio,
        String usuario,
        String observaciones
) {}
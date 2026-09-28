package cr.ac.ucr.paraiso.ie.c5k023.lab06.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvioDTO(
        Integer id,
        String codigoRastreo,
        String destinatario,
        String direccionDestino,
        BigDecimal costo,
        String estadoEnvio,
        LocalDateTime fechaCreacion
) {}
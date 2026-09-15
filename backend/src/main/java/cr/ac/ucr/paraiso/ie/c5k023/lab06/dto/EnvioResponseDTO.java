package cr.ac.ucr.paraiso.ie.c5k023.lab06.dto;

import java.math.BigDecimal;

public record EnvioResponseDTO(
        Integer id,
        String codigoRastreo,
        String direccionDestino,
        BigDecimal pesoKg,
        BigDecimal costo,
        String estadoEnvio,
        String placaVehiculo,
        String nombreConductor
) {}
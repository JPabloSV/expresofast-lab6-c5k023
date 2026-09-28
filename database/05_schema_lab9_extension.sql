USE ExpresoFastC5K023_II2026;
GO

-- 1. Nueva columna destinatario en Envio (idempotente)
IF COL_LENGTH('dbo.Envio', 'destinatario') IS NULL
BEGIN
    ALTER TABLE dbo.Envio ADD destinatario VARCHAR(150) NULL;
END
GO

-- 2. SP_OBTENER_ENVIOS_POR_ESTADO
IF OBJECT_ID('dbo.SP_OBTENER_ENVIOS_POR_ESTADO', 'P') IS NOT NULL
    DROP PROCEDURE dbo.SP_OBTENER_ENVIOS_POR_ESTADO;
GO

CREATE PROCEDURE dbo.SP_OBTENER_ENVIOS_POR_ESTADO
    @pEstado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        envio_id,
        codigo_rastreo,
        destinatario,
        direccion_destino,
        costo,
        estado_envio,
        fecha_creacion
    FROM dbo.Envio
    WHERE estado_envio = @pEstado
    ORDER BY fecha_creacion DESC;
END
GO

-- 3. SP_RESUMEN_METRICAS_ENVIOS (reto opcional)
IF OBJECT_ID('dbo.SP_RESUMEN_METRICAS_ENVIOS', 'P') IS NOT NULL
    DROP PROCEDURE dbo.SP_RESUMEN_METRICAS_ENVIOS;
GO

CREATE PROCEDURE dbo.SP_RESUMEN_METRICAS_ENVIOS
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        estado_envio,
        COUNT(*)   AS total_envios,
        SUM(costo) AS costo_total
    FROM dbo.Envio
    GROUP BY estado_envio;
END
GO
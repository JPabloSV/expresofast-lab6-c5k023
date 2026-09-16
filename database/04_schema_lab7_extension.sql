USE ExpresoFastC5K023_II2026;
GO

-- Columna activo en Conductor
IF COL_LENGTH('dbo.Conductor', 'activo') IS NULL
BEGIN
    ALTER TABLE Conductor
        ADD activo BIT NOT NULL CONSTRAINT DF_Conductor_activo DEFAULT 1;
END
GO

-- Columna conductor_asignado_id en Vehiculo
IF COL_LENGTH('dbo.Vehiculo', 'conductor_asignado_id') IS NULL
BEGIN
    ALTER TABLE Vehiculo
        ADD conductor_asignado_id INT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_Vehiculo_ConductorAsignado')
BEGIN
    ALTER TABLE Vehiculo
        ADD CONSTRAINT FK_Vehiculo_ConductorAsignado
        FOREIGN KEY (conductor_asignado_id) REFERENCES Conductor(conductor_id);
END
GO

-- Datos de prueba: un conductor inactivo para validar la regla de negocio
UPDATE Conductor SET activo = 1 WHERE licencia = 'B1-123456';
UPDATE Conductor SET activo = 0 WHERE licencia = 'B1-654321';
GO

IF DB_ID('ExpresoFastC5K023_II2026') IS NULL
BEGIN
    CREATE DATABASE ExpresoFastC5K023_II2026;
END
GO

USE ExpresoFastC5K023_II2026;
GO

IF OBJECT_ID('dbo.Envio', 'U') IS NOT NULL DROP TABLE dbo.Envio;
IF OBJECT_ID('dbo.Vehiculo', 'U') IS NOT NULL DROP TABLE dbo.Vehiculo;
IF OBJECT_ID('dbo.Conductor', 'U') IS NOT NULL DROP TABLE dbo.Conductor;
IF OBJECT_ID('dbo.EmpresaLogistica', 'U') IS NOT NULL DROP TABLE dbo.EmpresaLogistica;
GO

-- Tabla EmpresaLogistica
CREATE TABLE EmpresaLogistica (
    empresa_id       INT IDENTITY(1,1) PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL UNIQUE,
    cedula_juridica  VARCHAR(20)  NOT NULL UNIQUE,
    telefono         VARCHAR(20)  NOT NULL,
    fecha_registro   DATETIME2    NOT NULL
);
GO

-- Tabla Conductor
CREATE TABLE Conductor (
    conductor_id INT IDENTITY(1,1) PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL,
    apellidos    VARCHAR(50) NOT NULL,
    licencia     VARCHAR(20) NOT NULL UNIQUE,
    telefono     VARCHAR(20) NOT NULL
);
GO

-- Tabla Vehiculo
CREATE TABLE Vehiculo (
    vehiculo_id  INT IDENTITY(1,1) PRIMARY KEY,
    placa        VARCHAR(15)    NOT NULL UNIQUE,
    capacidad_kg DECIMAL(10,2)  NOT NULL,
    estado       VARCHAR(20)    NOT NULL,
    empresa_id   INT            NOT NULL,
    CONSTRAINT FK_Vehiculo_Empresa FOREIGN KEY (empresa_id)
        REFERENCES EmpresaLogistica(empresa_id)
);
GO

-- Tabla Envio
CREATE TABLE Envio (
    envio_id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo_rastreo     VARCHAR(30)    NOT NULL UNIQUE,
    direccion_destino  VARCHAR(200)   NOT NULL,
    peso_kg            DECIMAL(10,2)  NOT NULL,
    costo              DECIMAL(10,2)  NOT NULL,
    estado_envio       VARCHAR(20)    NOT NULL,
    vehiculo_id        INT            NOT NULL,
    conductor_id       INT            NOT NULL,
    fecha_creacion     DATETIME2      NULL,
    fecha_modificacion DATETIME2      NULL,
    CONSTRAINT FK_Envio_Vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES Vehiculo(vehiculo_id),
    CONSTRAINT FK_Envio_Conductor FOREIGN KEY (conductor_id)
        REFERENCES Conductor(conductor_id)
);
GO

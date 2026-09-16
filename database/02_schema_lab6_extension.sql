USE ExpresoFastC5K023_II2026;
GO

IF OBJECT_ID('dbo.BitacoraEnvio', 'U') IS NOT NULL DROP TABLE dbo.BitacoraEnvio;
IF OBJECT_ID('dbo.UsuarioRol', 'U') IS NOT NULL DROP TABLE dbo.UsuarioRol;
IF OBJECT_ID('dbo.Rol', 'U') IS NOT NULL DROP TABLE dbo.Rol;
IF OBJECT_ID('dbo.Usuario', 'U') IS NOT NULL DROP TABLE dbo.Usuario;
GO

-- Tabla Usuario
CREATE TABLE Usuario (
    usuario_id      INT IDENTITY(1,1) PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    activo          BIT          NOT NULL DEFAULT 1
);
GO

-- Tabla Rol
CREATE TABLE Rol (
    rol_id     INT IDENTITY(1,1) PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE
);
GO

-- Tabla intermedia UsuarioRol (N:M)
CREATE TABLE UsuarioRol (
    usuario_id INT NOT NULL,
    rol_id     INT NOT NULL,
    CONSTRAINT PK_UsuarioRol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT FK_UsuarioRol_Usuario FOREIGN KEY (usuario_id)
        REFERENCES Usuario(usuario_id),
    CONSTRAINT FK_UsuarioRol_Rol FOREIGN KEY (rol_id)
        REFERENCES Rol(rol_id)
);
GO

-- Tabla BitacoraEnvio (auditoria de cambios de estado)
CREATE TABLE BitacoraEnvio (
    bitacora_id     INT IDENTITY(1,1) PRIMARY KEY,
    envio_id        INT           NOT NULL,
    estado_anterior VARCHAR(20)   NOT NULL,
    estado_nuevo    VARCHAR(20)   NOT NULL,
    fecha_cambio    DATETIME2     NOT NULL,
    usuario_id      INT           NOT NULL,
    observaciones   VARCHAR(250)  NULL,
    CONSTRAINT FK_Bitacora_Envio FOREIGN KEY (envio_id)
        REFERENCES Envio(envio_id),
    CONSTRAINT FK_Bitacora_Usuario FOREIGN KEY (usuario_id)
        REFERENCES Usuario(usuario_id)
);
GO

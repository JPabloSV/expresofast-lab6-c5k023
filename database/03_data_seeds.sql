-- =====================================================================
-- ExpresoFast - Carne C5K023 - II-2026
-- 03_data_seeds.sql
-- Datos de prueba: empresa, vehiculos, conductores, roles y usuarios
-- Ejecutar DESPUES de 01_schema_lab5.sql y 02_schema_lab6_extension.sql
-- Contrasena en texto plano para TODOS los usuarios de prueba: Password123!
-- =====================================================================

USE ExpresoFastC5K023_II2026;
GO

-- Empresa
INSERT INTO EmpresaLogistica (nombre, cedula_juridica, telefono, fecha_registro)
VALUES ('ExpresoFast Costa Rica', '3-101-987654', '2200-1234', GETDATE());
GO

-- Vehiculos
INSERT INTO Vehiculo (placa, capacidad_kg, estado, empresa_id)
VALUES
    ('CL-123456', 1500.00, 'DISPONIBLE', 1),
    ('SJ-654321', 800.00,  'DISPONIBLE', 1),
    ('AL-112233', 2200.00, 'EN_MANTENIMIENTO', 1);
GO

-- Conductores
INSERT INTO Conductor (nombre, apellidos, licencia, telefono)
VALUES
    ('Luis',    'Fernandez Mora',  'B1-123456', '8888-1111'),
    ('Maria',   'Rojas Sanchez',   'B1-654321', '8888-2222');
GO

-- Roles del sistema (RBAC)
INSERT INTO Rol (nombre_rol) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_OPERADOR'),
    ('ROLE_CONDUCTOR');
GO

-- Usuarios de prueba
-- Contrasena en texto plano para los 3: Password123!
INSERT INTO Usuario (username, password_hash, nombre_completo, email, activo)
VALUES
    ('admin',      '$2b$10$3gaXPF9YSST9appnDrYTDOxkwzbxCSpLMJ58AsXBYuXScjozK0w/S', 'Carlos Alvarado',  'admin@expresofast.cr',      1),
    ('operador1',  '$2b$10$SCH9HMmajoxWUu2Tkg6yJeksthiiZA8EL3OKihmKAqbhxGufYuWj.', 'Ana Jimenez',      'operador1@expresofast.cr',  1),
    ('conductor1', '$2b$10$JpBL0XjeZ03jLGf0tviAPOM3WPHN89TQwDd6fc/UaXpPrOQ6TuIw.', 'Luis Fernandez',   'conductor1@expresofast.cr', 1);
GO

-- Asignacion de roles (usuario_id, rol_id)
INSERT INTO UsuarioRol (usuario_id, rol_id) VALUES
    (1, 1),  -- admin -> ROLE_ADMIN
    (2, 2),  -- operador1 -> ROLE_OPERADOR
    (3, 3);  -- conductor1 -> ROLE_CONDUCTOR
GO

-- Envios de ejemplo
INSERT INTO Envio (codigo_rastreo, direccion_destino, peso_kg, costo, estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
VALUES
    ('EXP-1001', 'San Jose, Barrio Escalante',  12.50, 8500.00,  'PENDIENTE',    1, 1, GETDATE(), GETDATE()),
    ('EXP-1002', 'Cartago, Paraiso centro',     35.00, 15200.00, 'EN_TRANSITO',  2, 2, GETDATE(), GETDATE()),
    ('EXP-1003', 'Alajuela, La Fortuna',        60.00, 21000.00, 'ENTREGADO',    1, 1, GETDATE(), GETDATE());
GO

-- Datos de prueba verificados para los 3 roles del sistema (admin, operador1, conductor1)

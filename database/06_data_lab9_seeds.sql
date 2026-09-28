USE ExpresoFastC5K023_II2026;
GO

-- Backfill de destinatario en los envios ya existentes
UPDATE Envio SET destinatario = 'Jorge Castro Mena'    WHERE codigo_rastreo = 'EXP-1001';
UPDATE Envio SET destinatario = 'Silvia Vargas Leiton' WHERE codigo_rastreo = 'EXP-1002';
UPDATE Envio SET destinatario = 'Andres Solis Umana'   WHERE codigo_rastreo = 'EXP-1003';
UPDATE Envio SET destinatario = 'Mariana Solis Vega'   WHERE codigo_rastreo = 'EXP-0001';
GO

-- Envios adicionales para completar el minimo de 15 registros con los 4 estados
INSERT INTO Envio (codigo_rastreo, destinatario, direccion_destino, peso_kg, costo, estado_envio, vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
VALUES
    ('EXP-1004', 'Maria Jose Quiros',   'Heredia, San Rafael',           8.00,  6500.00,  'PENDIENTE',   3, 2, GETDATE(), GETDATE()),
    ('EXP-1005', 'Kevin Mora Delgado',  'San Jose, Curridabat',          22.30, 11200.00, 'EN_TRANSITO', 1, 1, GETDATE(), GETDATE()),
    ('EXP-1006', 'Paola Chinchilla',    'Cartago, Tres Rios',            5.10,  4200.00,  'ENTREGADO',   2, 2, GETDATE(), GETDATE()),
    ('EXP-1007', 'Esteban Jimenez',     'Alajuela, Grecia',              45.00, 18500.00, 'CANCELADO',   1, 1, GETDATE(), GETDATE()),
    ('EXP-1008', 'Natalia Rojas',       'San Jose, Desamparados',        14.75, 9000.00,  'PENDIENTE',   2, 2, GETDATE(), GETDATE()),
    ('EXP-1009', 'Diego Fernandez',     'Cartago, El Guarco',            30.00, 14300.00, 'EN_TRANSITO', 3, 1, GETDATE(), GETDATE()),
    ('EXP-1010', 'Gabriela Solano',     'Heredia, Barva',                9.90,  7100.00,  'ENTREGADO',   1, 2, GETDATE(), GETDATE()),
    ('EXP-1011', 'Luis Carlos Mata',    'San Jose, Escazu',              18.40, 10800.00, 'CANCELADO',   2, 1, GETDATE(), GETDATE()),
    ('EXP-1012', 'Ana Lucia Rodriguez', 'Alajuela, San Ramon',           27.60, 13900.00, 'PENDIENTE',   3, 2, GETDATE(), GETDATE()),
    ('EXP-1013', 'Carlos Brenes',       'Cartago, Oreamuno',             11.20, 8300.00,  'EN_TRANSITO', 1, 1, GETDATE(), GETDATE()),
    ('EXP-1014', 'Marcela Ugalde',      'San Jose, Moravia',             6.50,  5400.00,  'ENTREGADO',   2, 2, GETDATE(), GETDATE()),
    ('EXP-1015', 'Ronald Salazar',      'Heredia, Santo Domingo',        40.00, 17200.00, 'PENDIENTE',   3, 1, GETDATE(), GETDATE()),
    ('EXP-1016', 'Fabian Araya',        'San Jose, Tibas',               16.80, 9700.00,  'EN_TRANSITO', 1, 2, GETDATE(), GETDATE()),
    ('EXP-1017', 'Sofia Chaves',        'Cartago, La Union',             20.00, 12100.00, 'CANCELADO',   2, 1, GETDATE(), GETDATE());
GO
-- ===================================================
-- SCRIPT DE DATOS PARA EL SISTEMA ACADEMIA
-- Base de datos: MySQL
-- ===================================================

-- Limpiar datos existentes (opcional - comentar si no deseas borrar)
DELETE FROM entregas;
DELETE FROM tarea_alumnos;
DELETE FROM tareas;
DELETE FROM curso_alumnos;
DELETE FROM curso_profesores;
DELETE FROM usuarios;
DELETE FROM cursos;
DELETE FROM alumnos;
DELETE FROM profesores;

-- ===================================================
-- 1. INSERTAR PROFESORES
-- ===================================================

INSERT INTO profesores (id, nombre, apellido, telefono, email, especialidad, anhos_experiencia) VALUES
                                                                                                   (1, 'Carlos', 'Martínez', '666123456', 'carlos.martinez@academia.es', 'Matemáticas', 15),
                                                                                                   (2, 'Ana', 'García', '666234567', 'ana.garcia@academia.es', 'Física', 12),
                                                                                                   (3, 'Miguel', 'López', '666345678', 'miguel.lopez@academia.es', 'Química', 8),
                                                                                                   (4, 'Laura', 'Fernández', '666456789', 'laura.fernandez@academia.es', 'Biología', 10),
                                                                                                   (5, 'David', 'Rodríguez', '666567890', 'david.rodriguez@academia.es', 'Historia', 18),
                                                                                                   (6, 'María', 'Sánchez', '666678901', 'maria.sanchez@academia.es', 'Literatura', 14),
                                                                                                   (7, 'Javier', 'Torres', '666789012', 'javier.torres@academia.es', 'Inglés', 9),
                                                                                                   (8, 'Carmen', 'Ruiz', '666890123', 'carmen.ruiz@academia.es', 'Informática', 11),
                                                                                                   (9, 'Antonio', 'Moreno', '666901234', 'antonio.moreno@academia.es', 'Filosofía', 16),
                                                                                                   (10, 'Elena', 'Jiménez', '666012345', 'elena.jimenez@academia.es', 'Arte', 7);

-- ===================================================
-- 2. INSERTAR ALUMNOS
-- ===================================================

INSERT INTO alumnos (id, nombre, apellido, fecha_nacimiento, telefono, email, direccion) VALUES
                                                                                             (1, 'Pablo', 'González', '2000-03-15', '655111111', 'pablo.gonzalez@email.com', 'Calle Mayor 123, Madrid'),
                                                                                             (2, 'Lucía', 'Hernández', '1999-07-22', '655222222', 'lucia.hernandez@email.com', 'Avenida Sol 45, Barcelona'),
                                                                                             (3, 'Alejandro', 'Muñoz', '2001-01-10', '655333333', 'alejandro.munoz@email.com', 'Plaza Central 78, Valencia'),
                                                                                             (4, 'Sara', 'Álvarez', '2000-11-05', '655444444', 'sara.alvarez@email.com', 'Calle Luna 12, Sevilla'),
                                                                                             (5, 'Diego', 'Vargas', '1998-09-18', '655555555', 'diego.vargas@email.com', 'Avenida Estrella 56, Bilbao'),
                                                                                             (6, 'Andrea', 'Castro', '2001-04-30', '655666666', 'andrea.castro@email.com', 'Calle Mar 89, Málaga'),
                                                                                             (7, 'Raúl', 'Ortega', '1999-12-08', '655777777', 'raul.ortega@email.com', 'Plaza Verde 34, Zaragoza'),
                                                                                             (8, 'Natalia', 'Ramos', '2000-06-25', '655888888', 'natalia.ramos@email.com', 'Avenida Río 67, Murcia'),
                                                                                             (9, 'Iván', 'Guerrero', '2001-08-14', '655999999', 'ivan.guerrero@email.com', 'Calle Monte 23, Alicante'),
                                                                                             (10, 'Marta', 'Rubio', '1999-02-03', '655000000', 'marta.rubio@email.com', 'Plaza Jardín 90, Valladolid'),
                                                                                             (11, 'Carlos', 'Vega', '2000-10-27', '655111222', 'carlos.vega@email.com', 'Calle Bosque 45, Santander'),
                                                                                             (12, 'Cristina', 'Mendoza', '2001-05-16', '655222333', 'cristina.mendoza@email.com', 'Avenida Campo 78, Córdoba'),
                                                                                             (13, 'Óscar', 'Herrera', '1998-08-09', '655333444', 'oscar.herrera@email.com', 'Plaza Fuente 12, Toledo'),
                                                                                             (14, 'Patricia', 'Silva', '2000-12-31', '655444555', 'patricia.silva@email.com', 'Calle Puente 56, Pamplona'),
                                                                                             (15, 'Rubén', 'Peña', '1999-03-20', '655555666', 'ruben.pena@email.com', 'Avenida Torre 89, León');

-- ===================================================
-- 3. INSERTAR USUARIOS
-- ===================================================

-- Usuario Administrador
INSERT INTO usuarios (id, username, password, nombre, apellido, rol, profesor_id, alumno_id) VALUES
-- Usuario Administrador (password: admin123)
(1, 'admin', '$2a$10$7QwWKn9h5.Z3B5K3R7TfI.oR4cjpGZoN0Q6I4p0Iw5m6FJ7G0zF6K', 'Administrador', 'Sistema', 'Admin', NULL, NULL),

-- Usuarios Profesores (password: prof123)
(2, 'carlos.martinez', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Carlos', 'Martínez', 'Profesor', 1, NULL),
(3, 'ana.garcia', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Ana', 'García', 'Profesor', 2, NULL),
(4, 'miguel.lopez', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Miguel', 'López', 'Profesor', 3, NULL),
(5, 'laura.fernandez', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Laura', 'Fernández', 'Profesor', 4, NULL),
(6, 'david.rodriguez', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'David', 'Rodríguez', 'Profesor', 5, NULL),
(7, 'maria.sanchez', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'María', 'Sánchez', 'Profesor', 6, NULL),
(8, 'javier.torres', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Javier', 'Torres', 'Profesor', 7, NULL),
(9, 'carmen.ruiz', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Carmen', 'Ruiz', 'Profesor', 8, NULL),
(10, 'antonio.moreno', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Antonio', 'Moreno', 'Profesor', 9, NULL),
(11, 'elena.jimenez', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ESvNqSmvt6VW7ZlEJ3P4a', 'Elena', 'Jiménez', 'Profesor', 10, NULL),

-- Usuarios Alumnos (password: alumno123)
(12, 'pablo.gonzalez', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Pablo', 'González', 'Alumno', NULL, 1),
(13, 'lucia.hernandez', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Lucía', 'Hernández', 'Alumno', NULL, 2),
(14, 'alejandro.munoz', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Alejandro', 'Muñoz', 'Alumno', NULL, 3),
(15, 'sara.alvarez', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Sara', 'Álvarez', 'Alumno', NULL, 4),
(16, 'diego.vargas', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Diego', 'Vargas', 'Alumno', NULL, 5),
(17, 'andrea.castro', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Andrea', 'Castro', 'Alumno', NULL, 6),
(18, 'raul.ortega', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Raúl', 'Ortega', 'Alumno', NULL, 7),
(19, 'natalia.ramos', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Natalia', 'Ramos', 'Alumno', NULL, 8),
(20, 'ivan.guerrero', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Iván', 'Guerrero', 'Alumno', NULL, 9),
(21, 'marta.rubio', '$2a$10$OqWzkz5uRxo8x9RzpK7v6eyyy2G/d7Gqg5Rm.Pj0zflDqCSlG3V7S', 'Marta', 'Rubio', 'Alumno', NULL, 10);
-- ===================================================
-- 4. INSERTAR CURSOS
-- ===================================================

INSERT INTO cursos (id, nombre, descripcion, nivel, precio) VALUES
                                                                (1, 'Matemáticas Básicas', 'Fundamentos de matemáticas para principiantes', 'Básico', 150.00),
                                                                (2, 'Física Avanzada', 'Conceptos avanzados de física moderna', 'Avanzado', 220.00),
                                                                (3, 'Química Orgánica', 'Introducción a la química orgánica', 'Intermedio', 180.00),
                                                                (4, 'Biología Molecular', 'Estudio de procesos biológicos a nivel molecular', 'Experto', 250.00),
                                                                (5, 'Historia Medieval', 'Historia de Europa en la Edad Media', 'Intermedio', 160.00),
                                                                (6, 'Literatura Española', 'Grandes obras de la literatura española', 'Básico', 140.00),
                                                                (7, 'Inglés Conversacional', 'Práctica de conversación en inglés', 'Básico', 120.00),
                                                                (8, 'Programación Java', 'Desarrollo de aplicaciones con Java', 'Intermedio', 300.00),
                                                                (9, 'Filosofía Antigua', 'Pensadores de la filosofía clásica', 'Avanzado', 170.00),
                                                                (10, 'Arte Contemporáneo', 'Movimientos artísticos del siglo XX', 'Básico', 130.00),
                                                                (11, 'Cálculo Diferencial', 'Fundamentos del cálculo diferencial', 'Avanzado', 200.00),
                                                                (12, 'Inglés Técnico', 'Inglés aplicado a áreas técnicas', 'Intermedio', 180.00);

-- ===================================================
-- 5. ASIGNAR PROFESORES A CURSOS
-- ===================================================

INSERT INTO curso_profesores (curso_id, profesor_id) VALUES
-- Matemáticas Básicas
(1, 1), -- Carlos Martínez
-- Física Avanzada
(2, 2), -- Ana García
-- Química Orgánica
(3, 3), -- Miguel López
-- Biología Molecular
(4, 4), -- Laura Fernández
-- Historia Medieval
(5, 5), -- David Rodríguez
-- Literatura Española
(6, 6), -- María Sánchez
-- Inglés Conversacional
(7, 7), -- Javier Torres
-- Programación Java
(8, 8), -- Carmen Ruiz
-- Filosofía Antigua
(9, 9), -- Antonio Moreno
-- Arte Contemporáneo
(10, 10), -- Elena Jiménez
-- Cálculo Diferencial (también Carlos Martínez)
(11, 1), -- Carlos Martínez
-- Inglés Técnico (también Javier Torres)
(12, 7); -- Javier Torres

-- ===================================================
-- 6. MATRICULAR ALUMNOS EN CURSOS
-- ===================================================

INSERT INTO curso_alumnos (curso_id, alumno_id) VALUES
-- Matemáticas Básicas (Curso 1)
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
-- Física Avanzada (Curso 2)
(2, 3), (2, 4), (2, 5), (2, 6),
-- Química Orgánica (Curso 3)
(3, 2), (3, 5), (3, 7), (3, 8), (3, 9),
-- Biología Molecular (Curso 4)
(4, 6), (4, 8), (4, 10),
-- Historia Medieval (Curso 5)
(5, 1), (5, 7), (5, 9), (5, 11), (5, 12), (5, 13),
-- Literatura Española (Curso 6)
(6, 2), (6, 4), (6, 6), (6, 10), (6, 14),
-- Inglés Conversacional (Curso 7)
(7, 1), (7, 3), (7, 5), (7, 7), (7, 9), (7, 11), (7, 13), (7, 15),
-- Programación Java (Curso 8)
(8, 4), (8, 8), (8, 12), (8, 14),
-- Filosofía Antigua (Curso 9)
(9, 6), (9, 10), (9, 15),
-- Arte Contemporáneo (Curso 10)
(10, 2), (10, 5), (10, 8), (10, 11), (10, 14),
-- Cálculo Diferencial (Curso 11)
(11, 3), (11, 5), (11, 9),
-- Inglés Técnico (Curso 12)
(12, 4), (12, 8), (12, 12), (12, 15);

-- ===================================================
-- 7. INSERTAR TAREAS
-- ===================================================

INSERT INTO tareas (id, nombre, descripcion, fecha_publicacion, fecha_limite, curso_id, profesor_id, para_todos_alumnos) VALUES
-- Tareas de Matemáticas Básicas
(1, 'Ejercicios de Álgebra', 'Resolver ecuaciones lineales y cuadráticas', '2024-01-15', '2024-01-30', 1, 1, TRUE),
(2, 'Problemas de Geometría', 'Calcular áreas y perímetros de figuras', '2024-02-01', '2024-02-15', 1, 1, TRUE),

-- Tareas de Física Avanzada
(3, 'Laboratorio de Mecánica', 'Experimento sobre movimiento parabólico', '2024-01-20', '2024-02-05', 2, 2, TRUE),
(4, 'Teoría de Relatividad', 'Ensayo sobre conceptos de Einstein', '2024-02-10', '2024-03-01', 2, 2, FALSE),

-- Tareas de Química Orgánica
(5, 'Síntesis de Compuestos', 'Práctica de laboratorio de síntesis', '2024-01-25', '2024-02-20', 3, 3, TRUE),
(6, 'Mecanismos de Reacción', 'Analizar mecanismos de reacciones orgánicas', '2024-02-15', '2024-03-10', 3, 3, TRUE),

-- Tareas de Historia Medieval
(7, 'Ensayo sobre Feudalismo', 'Análisis del sistema feudal europeo', '2024-01-18', '2024-02-18', 5, 5, TRUE),
(8, 'Investigación Cruzadas', 'Trabajo de investigación sobre las Cruzadas', '2024-02-05', '2024-03-05', 5, 5, FALSE),

-- Tareas de Programación Java
(9, 'Aplicación Calculadora', 'Desarrollar una calculadora básica en Java', '2024-01-22', '2024-02-22', 8, 8, TRUE),
(10, 'Sistema de Gestión', 'Crear un CRUD básico con base de datos', '2024-02-12', '2024-03-15', 8, 8, TRUE),

-- Tareas de Inglés Conversacional
(11, 'Presentación Oral', 'Presentar un tema de interés personal', '2024-01-30', '2024-02-28', 7, 7, TRUE),
(12, 'Diálogo Situacional', 'Crear diálogos para situaciones cotidianas', '2024-02-20', '2024-03-20', 7, 7, FALSE),

-- Tareas de Arte Contemporáneo
(13, 'Análisis de Obra', 'Analizar una obra de arte contemporáneo', '2024-02-01', '2024-03-01', 10, 10, TRUE),

-- Tareas de Cálculo Diferencial
(14, 'Derivadas Parciales', 'Ejercicios de cálculo de derivadas', '2024-02-08', '2024-03-08', 11, 1, TRUE);

-- ===================================================
-- 8. ASIGNAR TAREAS ESPECÍFICAS A ALUMNOS
-- ===================================================
-- (Solo para tareas que NO son para todos los alumnos)

INSERT INTO tarea_alumnos (tarea_id, alumno_id) VALUES
-- Teoría de Relatividad (Tarea 4) - Solo para algunos alumnos avanzados
(4, 3), (4, 5), (4, 6),
-- Investigación Cruzadas (Tarea 8) - Para alumnos específicos
(8, 1), (8, 9), (8, 11), (8, 13),
-- Diálogo Situacional (Tarea 12) - Para algunos alumnos de inglés
(12, 1), (12, 5), (12, 9), (12, 13);

-- ===================================================
-- 9. INSERTAR ENTREGAS
-- ===================================================

INSERT INTO entregas (id, tarea_id, alumno_id, estado, fecha_entrega, nota, comentarios) VALUES
-- Entregas para Ejercicios de Álgebra (Tarea 1)
(1, 1, 1, 'CALIFICADA', '2024-01-28 14:30:00', 8.5, 'Buen trabajo, algunos errores menores'),
(2, 1, 2, 'CALIFICADA', '2024-01-29 16:45:00', 9.2, 'Excelente resolución de problemas'),
(3, 1, 3, 'ENTREGADA', '2024-01-30 10:15:00', NULL, NULL),
(4, 1, 4, 'CALIFICADA', '2024-01-27 12:20:00', 7.8, 'Necesita mejorar en ecuaciones cuadráticas'),
(5, 1, 5, 'FUERA_PLAZO', '2024-02-02 09:30:00', 6.0, 'Entrega tardía, contenido correcto'),

-- Entregas para Laboratorio de Mecánica (Tarea 3)
(6, 3, 3, 'CALIFICADA', '2024-02-04 11:00:00', 9.0, 'Excelente análisis experimental'),
(7, 3, 4, 'ENTREGADA', '2024-02-05 15:30:00', NULL, NULL),
(8, 3, 5, 'CALIFICADA', '2024-02-03 13:45:00', 8.7, 'Muy buen trabajo de laboratorio'),
(9, 3, 6, 'PENDIENTE', NULL, NULL, NULL),

-- Entregas para Síntesis de Compuestos (Tarea 5)
(10, 5, 2, 'CALIFICADA', '2024-02-18 16:20:00', 8.3, 'Buena técnica de laboratorio'),
(11, 5, 5, 'ENTREGADA', '2024-02-19 14:10:00', NULL, NULL),
(12, 5, 7, 'CALIFICADA', '2024-02-17 10:30:00', 9.1, 'Excelente síntesis y análisis'),
(13, 5, 8, 'FUERA_PLAZO', '2024-02-22 09:15:00', 7.5, 'Entrega tardía pero correcta'),

-- Entregas para Ensayo sobre Feudalismo (Tarea 7)
(14, 7, 1, 'CALIFICADA', '2024-02-16 18:00:00', 8.8, 'Análisis profundo y bien estructurado'),
(15, 7, 7, 'ENTREGADA', '2024-02-17 20:30:00', NULL, NULL),
(16, 7, 9, 'CALIFICADA', '2024-02-15 14:45:00', 9.3, 'Excelente investigación histórica'),
(17, 7, 11, 'PENDIENTE', NULL, NULL, NULL),

-- Entregas para Aplicación Calculadora (Tarea 9)
(18, 9, 4, 'CALIFICADA', '2024-02-20 12:00:00', 8.9, 'Código limpio y funcional'),
(19, 9, 8, 'ENTREGADA', '2024-02-21 16:30:00', NULL, NULL),
(20, 9, 12, 'CALIFICADA', '2024-02-19 11:45:00', 9.4, 'Implementación excelente'),
(21, 9, 14, 'FUERA_PLAZO', '2024-02-25 10:20:00', 8.1, 'Entrega tardía, buen código'),

-- Entregas para Presentación Oral (Tarea 11)
(22, 11, 1, 'CALIFICADA', '2024-02-26 14:15:00', 8.6, 'Buena pronunciación y fluidez'),
(23, 11, 3, 'ENTREGADA', '2024-02-27 16:20:00', NULL, NULL),
(24, 11, 5, 'CALIFICADA', '2024-02-25 13:30:00', 9.0, 'Excelente presentación'),
(25, 11, 7, 'PENDIENTE', NULL, NULL, NULL),

-- Entregas para Teoría de Relatividad (Tarea 4 - específica)
(26, 4, 3, 'CALIFICADA', '2024-02-28 17:45:00', 9.2, 'Comprensión excelente de conceptos complejos'),
(27, 4, 5, 'ENTREGADA', '2024-03-01 15:20:00', NULL, NULL),
(28, 4, 6, 'PENDIENTE', NULL, NULL, NULL);

-- ===================================================
-- VERIFICAR DATOS INSERTADOS
-- ===================================================

-- Mostrar resumen de datos insertados
SELECT 'PROFESORES' as Tabla, COUNT(*) as Total FROM profesores
UNION ALL
SELECT 'ALUMNOS' as Tabla, COUNT(*) as Total FROM alumnos
UNION ALL
SELECT 'USUARIOS' as Tabla, COUNT(*) as Total FROM usuarios
UNION ALL
SELECT 'CURSOS' as Tabla, COUNT(*) as Total FROM cursos
UNION ALL
SELECT 'TAREAS' as Tabla, COUNT(*) as Total FROM tareas
UNION ALL
SELECT 'ENTREGAS' as Tabla, COUNT(*) as Total FROM entregas
UNION ALL
SELECT 'CURSO_PROFESORES' as Tabla, COUNT(*) as Total FROM curso_profesores
UNION ALL
SELECT 'CURSO_ALUMNOS' as Tabla, COUNT(*) as Total FROM curso_alumnos
UNION ALL
SELECT 'TAREA_ALUMNOS' as Tabla, COUNT(*) as Total FROM tarea_alumnos;

-- ===================================================
-- CONSULTAS ÚTILES PARA VERIFICAR RELACIONES
-- ===================================================

-- Ver cursos con sus profesores
-- SELECT c.nombre as Curso, p.nombre as Profesor, p.especialidad
-- FROM cursos c
-- JOIN curso_profesores cp ON c.id = cp.curso_id
-- JOIN profesores p ON cp.profesor_id = p.id
-- ORDER BY c.nombre;

-- Ver alumnos matriculados por curso
-- SELECT c.nombre as Curso, COUNT(ca.alumno_id) as Total_Alumnos
-- FROM cursos c
-- LEFT JOIN curso_alumnos ca ON c.id = ca.curso_id
-- GROUP BY c.id, c.nombre
-- ORDER BY Total_Alumnos DESC;

-- Ver estadísticas de entregas
-- SELECT
--     t.nombre as Tarea,
--     COUNT(e.id) as Total_Entregas,
--     SUM(CASE WHEN e.estado = 'CALIFICADA' THEN 1 ELSE 0 END) as Calificadas,
--     SUM(CASE WHEN e.estado = 'ENTREGADA' THEN 1 ELSE 0 END) as Pendientes_Calificacion,
--     SUM(CASE WHEN e.estado = 'FUERA_PLAZO' THEN 1 ELSE 0 END) as Fuera_Plazo,
--     ROUND(AVG(e.nota), 2) as Nota_Media
-- FROM tareas t
-- LEFT JOIN entregas e ON t.id = e.tarea_id
-- GROUP BY t.id, t.nombre
-- ORDER BY t.id;
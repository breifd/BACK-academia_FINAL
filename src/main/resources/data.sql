-- Deshabilitar verificación de claves foráneas temporalmente para facilitar la eliminación
SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar datos existentes para evitar conflictos
DELETE FROM curso_alumnos;
DELETE FROM curso_profesores;
DELETE FROM tareas;
DELETE FROM cursos;
DELETE FROM usuarios;
DELETE FROM profesores;
DELETE FROM alumnos;

-- Reiniciar secuencias en MySQL
ALTER TABLE profesores AUTO_INCREMENT = 1;
ALTER TABLE alumnos AUTO_INCREMENT = 1;
ALTER TABLE usuarios AUTO_INCREMENT = 1;
ALTER TABLE cursos AUTO_INCREMENT = 1;
ALTER TABLE tareas AUTO_INCREMENT = 1;

-- Habilitar verificación de claves foráneas nuevamente
SET FOREIGN_KEY_CHECKS = 1;

-- Insertar profesores
INSERT INTO profesores (nombre, apellido, telefono, email, especialidad, anhos_experiencia) VALUES
                                                                                                ('Carlos', 'Rodríguez', '612345678', 'carlos.rodriguez@academia.com', 'Matemáticas', 8),
                                                                                                ('Ana', 'García', '612345679', 'ana.garcia@academia.com', 'Física', 5),
                                                                                                ('Juan', 'Martínez', '612345680', 'juan.martinez@academia.com', 'Química', 10),
                                                                                                ('Laura', 'Fernández', '612345681', 'laura.fernandez@academia.com', 'Biología', 6),
                                                                                                ('Miguel', 'López', '612345682', 'miguel.lopez@academia.com', 'Informática', 12),
                                                                                                ('Carmen', 'Sánchez', '612345683', 'carmen.sanchez@academia.com', 'Inglés', 7),
                                                                                                ('Javier', 'Pérez', '612345684', 'javier.perez@academia.com', 'Historia', 9),
                                                                                                ('María', 'González', '612345685', 'maria.gonzalez@academia.com', 'Literatura', 11);

-- Insertar alumnos
INSERT INTO alumnos (nombre, apellido, fecha_nacimiento, telefono, email, direccion) VALUES
                                                                                         ('Pedro', 'Gómez', '2000-05-15', '612345686', 'pedro.gomez@gmail.com', 'Calle Mayor 1, Madrid'),
                                                                                         ('Lucía', 'Díaz', '2001-02-20', '612345687', 'lucia.diaz@gmail.com', 'Calle Luna 2, Barcelona'),
                                                                                         ('Alberto', 'Ruiz', '2002-07-10', '612345688', 'alberto.ruiz@gmail.com', 'Calle Sol 3, Valencia'),
                                                                                         ('Sara', 'Moreno', '2000-12-05', '612345689', 'sara.moreno@gmail.com', 'Calle Estrella 4, Sevilla'),
                                                                                         ('David', 'Jiménez', '2001-09-25', '612345690', 'david.jimenez@gmail.com', 'Calle Mar 5, Bilbao'),
                                                                                         ('Marta', 'Álvarez', '2002-04-30', '612345691', 'marta.alvarez@gmail.com', 'Calle Río 6, Zaragoza'),
                                                                                         ('Pablo', 'Torres', '2000-08-18', '612345692', 'pablo.torres@gmail.com', 'Calle Montaña 7, Málaga'),
                                                                                         ('Elena', 'Navarro', '2001-11-22', '612345693', 'elena.navarro@gmail.com', 'Calle Valle 8, Murcia'),
                                                                                         ('Sergio', 'Molina', '2002-01-12', '612345694', 'sergio.molina@gmail.com', 'Calle Bosque 9, Alicante'),
                                                                                         ('Laura', 'Castro', '2000-06-28', '612345695', 'laura.castro@gmail.com', 'Calle Campo 10, Córdoba'),
                                                                                         ('Daniel', 'Ortega', '2001-03-17', '612345696', 'daniel.ortega@gmail.com', 'Calle Jardín 11, Granada'),
                                                                                         ('Paula', 'Rubio', '2002-10-09', '612345697', 'paula.rubio@gmail.com', 'Calle Parque 12, Valladolid'),
                                                                                         ('Alejandro', 'Serrano', '2000-04-14', '612345698', 'alejandro.serrano@gmail.com', 'Calle Fuente 13, Vigo'),
                                                                                         ('Cristina', 'Ramos', '2001-08-27', '612345699', 'cristina.ramos@gmail.com', 'Calle Playa 14, Gijón'),
                                                                                         ('Jorge', 'Hernández', '2002-12-03', '612345700', 'jorge.hernandez@gmail.com', 'Calle Puerto 15, Santander');

-- Insertar usuarios
-- Administrador
INSERT INTO usuarios (username, password, nombre, apellido, rol) VALUES
    ('admin', 'admin123', 'Administrador', 'Sistema', 'Admin');

-- Usuarios para profesores
INSERT INTO usuarios (username, password, nombre, apellido, rol, profesor_id) VALUES
                                                                                  ('carlos.rodriguez', 'prof123', 'Carlos', 'Rodríguez', 'Profesor', 1),
                                                                                  ('ana.garcia', 'prof123', 'Ana', 'García', 'Profesor', 2),
                                                                                  ('juan.martinez', 'prof123', 'Juan', 'Martínez', 'Profesor', 3),
                                                                                  ('laura.fernandez', 'prof123', 'Laura', 'Fernández', 'Profesor', 4),
                                                                                  ('miguel.lopez', 'prof123', 'Miguel', 'López', 'Profesor', 5),
                                                                                  ('carmen.sanchez', 'prof123', 'Carmen', 'Sánchez', 'Profesor', 6),
                                                                                  ('javier.perez', 'prof123', 'Javier', 'Pérez', 'Profesor', 7),
                                                                                  ('maria.gonzalez', 'prof123', 'María', 'González', 'Profesor', 8);

-- Usuarios para alumnos
INSERT INTO usuarios (username, password, nombre, apellido, rol, alumno_id) VALUES
                                                                                ('pedro.gomez', 'alum123', 'Pedro', 'Gómez', 'Alumno', 1),
                                                                                ('lucia.diaz', 'alum123', 'Lucía', 'Díaz', 'Alumno', 2),
                                                                                ('alberto.ruiz', 'alum123', 'Alberto', 'Ruiz', 'Alumno', 3),
                                                                                ('sara.moreno', 'alum123', 'Sara', 'Moreno', 'Alumno', 4),
                                                                                ('david.jimenez', 'alum123', 'David', 'Jiménez', 'Alumno', 5),
                                                                                ('marta.alvarez', 'alum123', 'Marta', 'Álvarez', 'Alumno', 6),
                                                                                ('pablo.torres', 'alum123', 'Pablo', 'Torres', 'Alumno', 7),
                                                                                ('elena.navarro', 'alum123', 'Elena', 'Navarro', 'Alumno', 8),
                                                                                ('sergio.molina', 'alum123', 'Sergio', 'Molina', 'Alumno', 9),
                                                                                ('laura.castro', 'alum123', 'Laura', 'Castro', 'Alumno', 10),
                                                                                ('daniel.ortega', 'alum123', 'Daniel', 'Ortega', 'Alumno', 11),
                                                                                ('paula.rubio', 'alum123', 'Paula', 'Rubio', 'Alumno', 12),
                                                                                ('alejandro.serrano', 'alum123', 'Alejandro', 'Serrano', 'Alumno', 13),
                                                                                ('cristina.ramos', 'alum123', 'Cristina', 'Ramos', 'Alumno', 14),
                                                                                ('jorge.hernandez', 'alum123', 'Jorge', 'Hernández', 'Alumno', 15);

-- Insertar cursos
INSERT INTO cursos (nombre, descripcion, nivel, precio) VALUES
                                                            ('Matemáticas Básicas', 'Fundamentos de cálculo y álgebra', 'Básico', 200.00),
                                                            ('Física Aplicada', 'Principios de física mecánica y termodinámica', 'Intermedio', 250.00),
                                                            ('Química General', 'Fundamentos de química orgánica e inorgánica', 'Básico', 220.00),
                                                            ('Biología Avanzada', 'Genética y evolución', 'Avanzado', 280.00),
                                                            ('Programación Java', 'Fundamentos de programación orientada a objetos', 'Intermedio', 300.00),
                                                            ('Inglés Comercial', 'Inglés para negocios y empresas', 'Avanzado', 230.00),
                                                            ('Historia del Arte', 'Recorrido por los principales movimientos artísticos', 'Básico', 190.00),
                                                            ('Literatura Contemporánea', 'Análisis de obras literarias del siglo XX', 'Intermedio', 210.00),
                                                            ('Matemáticas Avanzadas', 'Cálculo diferencial e integral', 'Avanzado', 270.00),
                                                            ('Desarrollo Web', 'HTML, CSS y JavaScript', 'Básico', 260.00),
                                                            ('Inteligencia Artificial', 'Fundamentos de machine learning', 'Experto', 350.00),
                                                            ('Bioquímica', 'Procesos químicos en sistemas biológicos', 'Avanzado', 290.00);

-- Asignar profesores a cursos
INSERT INTO curso_profesores (curso_id, profesor_id) VALUES
                                                         (1, 1), -- Carlos Rodríguez enseña Matemáticas Básicas
                                                         (9, 1), -- Carlos Rodríguez enseña Matemáticas Avanzadas
                                                         (2, 2), -- Ana García enseña Física Aplicada
                                                         (3, 3), -- Juan Martínez enseña Química General
                                                         (4, 4), -- Laura Fernández enseña Biología Avanzada
                                                         (12, 4), -- Laura Fernández enseña Bioquímica
                                                         (5, 5), -- Miguel López enseña Programación Java
                                                         (10, 5), -- Miguel López enseña Desarrollo Web
                                                         (11, 5), -- Miguel López enseña Inteligencia Artificial
                                                         (6, 6), -- Carmen Sánchez enseña Inglés Comercial
                                                         (7, 7), -- Javier Pérez enseña Historia del Arte
                                                         (8, 8); -- María González enseña Literatura Contemporánea

-- Matricular alumnos en cursos
INSERT INTO curso_alumnos (curso_id, alumno_id) VALUES
-- Matemáticas Básicas
(1, 1), (1, 3), (1, 5), (1, 7), (1, 9), (1, 11), (1, 13), (1, 15),
-- Física Aplicada
(2, 2), (2, 4), (2, 6), (2, 8), (2, 10), (2, 12), (2, 14),
-- Química General
(3, 1), (3, 3), (3, 5), (3, 7), (3, 9),
-- Biología Avanzada
(4, 2), (4, 4), (4, 6), (4, 8),
-- Programación Java
(5, 1), (5, 3), (5, 5), (5, 7), (5, 9), (5, 11), (5, 13), (5, 15),
-- Inglés Comercial
(6, 2), (6, 4), (6, 6), (6, 8), (6, 10), (6, 12), (6, 14),
-- Historia del Arte
(7, 1), (7, 2), (7, 3), (7, 4), (7, 5),
-- Literatura Contemporánea
(8, 6), (8, 7), (8, 8), (8, 9), (8, 10),
-- Matemáticas Avanzadas
(9, 11), (9, 12), (9, 13), (9, 14), (9, 15),
-- Desarrollo Web
(10, 1), (10, 2), (10, 3), (10, 4), (10, 5),
-- Inteligencia Artificial
(11, 6), (11, 7), (11, 8), (11, 9), (11, 10),
-- Bioquímica
(12, 11), (12, 12), (12, 13), (12, 14), (12, 15);

-- Insertar tareas
INSERT INTO tareas (nombre, descripcion, fecha_publicacion, fecha_limite, nota) VALUES
                                                                                    ('Ejercicios de álgebra', 'Resolver los ejercicios de la página 45', '2025-01-15', '2025-01-30', NULL),
                                                                                    ('Laboratorio de física', 'Informe sobre el experimento de péndulo simple', '2025-01-10', '2025-01-25', 8.5),
                                                                                    ('Tabla periódica', 'Completar la tabla periódica con los nuevos elementos', '2025-01-20', '2025-02-05', NULL),
                                                                                    ('Disección de rana', 'Informe sobre la disección realizada en clase', '2025-01-05', '2025-01-20', 9.0),
                                                                                    ('Proyecto Java', 'Crear una aplicación de gestión de biblioteca', '2025-01-25', '2025-02-15', NULL),
                                                                                    ('Redacción en inglés', 'Escribir un ensayo sobre globalización', '2025-01-12', '2025-01-27', 7.5),
                                                                                    ('Análisis de obra pictórica', 'Analizar "La noche estrellada" de Van Gogh', '2025-01-18', '2025-02-03', NULL),
                                                                                    ('Comentario literario', 'Comentar un fragmento de "Cien años de soledad"', '2025-01-08', '2025-01-23', 8.0),
                                                                                    ('Integrales definidas', 'Resolver los problemas del capítulo 7', '2025-01-30', '2025-02-15', NULL),
                                                                                    ('Página web personal', 'Crear una página web con HTML y CSS', '2025-01-22', '2025-02-10', 9.5),
                                                                                    ('Algoritmo de clasificación', 'Implementar un algoritmo de machine learning', '2025-01-14', '2025-02-01', NULL),
                                                                                    ('Análisis de proteínas', 'Informe sobre la estructura de proteínas', '2025-01-28', '2025-02-13', 8.2);
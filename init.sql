-- ==============================================
-- Script de Inicialización para MW Backend
-- Ejecutar en MySQL Workbench o línea de comandos
-- ==============================================

-- Crear la base de datos principal
CREATE DATABASE IF NOT EXISTS modatecdb
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE modatecdb;
-- LUEGO AQUI EJECUTAS EL BACKEND PARA CARGAR LAS TABLAS
-- Las tablas se crearán automáticamente cuando ejecutes la aplicación Spring Boot
-- Opcional: Crear usuario específico para el proyecto
-- (Recomendado para producción, opcional para desarrollo)

-- CREATE USER IF NOT EXISTS 'mw_user'@'localhost' IDENTIFIED BY 'mw_password_seguro';
-- GRANT ALL PRIVILEGES ON modatecdb.* TO 'mw_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Verificar que la base de datos se creó correctamente
SELECT 
    SCHEMA_NAME as 'Base de Datos',
    DEFAULT_CHARACTER_SET_NAME as 'Charset',
    DEFAULT_COLLATION_NAME as 'Collation'
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'mw_backend';

-- Mostrar mensaje de éxito
SELECT 'Base de datos mw_backend creada exitosamente!' as 'Status';

-- Las tablas se crearán automáticamente cuando ejecutes la aplicación Spring Boot
-- debido a la configuración: spring.jpa.hibernate.ddl-auto=update
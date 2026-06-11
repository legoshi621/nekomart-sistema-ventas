-- Script de creación de base de datos para NekoMart
-- Motor: Microsoft SQL Server

-- Crear la base de datos si no existe
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'NekoMartDB')
BEGIN
    CREATE DATABASE NekoMartDB;
END
GO

USE NekoMartDB;
GO

-- Eliminar tablas en orden inverso a sus relaciones para evitar errores de llave foránea
IF OBJECT_ID('dbo.detalle_venta', 'U') IS NOT NULL DROP TABLE dbo.detalle_venta;
IF OBJECT_ID('dbo.ventas', 'U') IS NOT NULL DROP TABLE dbo.ventas;
IF OBJECT_ID('dbo.productos', 'U') IS NOT NULL DROP TABLE dbo.productos;
IF OBJECT_ID('dbo.usuarios', 'U') IS NOT NULL DROP TABLE dbo.usuarios;
GO

-- 1. Tabla de Usuarios
CREATE TABLE usuarios (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL, -- SHA-256 produce 64 caracteres en hexadecimal
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN', 'EMPLEADO')),
    nombre_completo VARCHAR(100) NOT NULL
);
GO

-- 2. Tabla de Productos
CREATE TABLE productos (
    id INT IDENTITY(1,1) PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio >= 0),
    stock INT NOT NULL CHECK (stock >= 0),
    stock_minimo INT NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    categoria VARCHAR(50) NOT NULL
);
GO

-- 3. Tabla de Ventas (Cabecera)
CREATE TABLE ventas (
    id INT IDENTITY(1,1) PRIMARY KEY,
    folio VARCHAR(20) NOT NULL UNIQUE,
    fecha DATETIME NOT NULL DEFAULT GETDATE(),
    total DECIMAL(10, 2) NOT NULL CHECK (total >= 0),
    metodo_pago VARCHAR(50) NOT NULL,
    monto_recibido DECIMAL(10, 2) NOT NULL CHECK (monto_recibido >= 0),
    cambio DECIMAL(10, 2) NOT NULL CHECK (cambio >= 0),
    empleado_id INT NOT NULL FOREIGN KEY REFERENCES usuarios(id)
);
GO

-- 4. Tabla de Detalle de Venta
CREATE TABLE detalle_venta (
    id INT IDENTITY(1,1) PRIMARY KEY,
    venta_id INT NOT NULL FOREIGN KEY REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id INT NOT NULL FOREIGN KEY REFERENCES productos(id),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario >= 0)
);
GO

-- Insertar usuario Administrador por defecto
-- Contraseña original: admin123
-- SHA-256 Hash: 24078914ba28b0f56f03f885f8c8581a5f5f6a921d7b37f44b341f237895e648
INSERT INTO usuarios (username, password_hash, rol, nombre_completo)
VALUES ('admin', '24078914ba28b0f56f03f885f8c8581a5f5f6a921d7b37f44b341f237895e648', 'ADMIN', 'Administrador General');
GO

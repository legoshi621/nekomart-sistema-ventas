-- Script de creación de base de datos para NekoMart
-- Motor: SQLite

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    rol TEXT NOT NULL CHECK (rol IN ('ADMIN', 'EMPLEADO')),
    nombre_completo TEXT NOT NULL,
    foto_ruta TEXT
);

-- 2. Tabla de Productos
CREATE TABLE IF NOT EXISTS productos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    precio REAL NOT NULL CHECK (precio >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    stock_minimo INTEGER NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    categoria TEXT NOT NULL,
    imagen_ruta TEXT,
    fecha_caducidad TEXT,
    lote TEXT,
    activo INTEGER NOT NULL DEFAULT 1
);

-- 3. Tabla de Ventas (Cabecera)
CREATE TABLE IF NOT EXISTS ventas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    folio TEXT NOT NULL UNIQUE,
    fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total REAL NOT NULL CHECK (total >= 0),
    metodo_pago TEXT NOT NULL,
    monto_recibido REAL NOT NULL CHECK (monto_recibido >= 0),
    cambio REAL NOT NULL CHECK (cambio >= 0),
    empleado_id INTEGER NOT NULL,
    FOREIGN KEY(empleado_id) REFERENCES usuarios(id)
);

-- 4. Tabla de Detalle de Venta
CREATE TABLE IF NOT EXISTS detalle_venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario REAL NOT NULL CHECK (precio_unitario >= 0),
    FOREIGN KEY(venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
    FOREIGN KEY(producto_id) REFERENCES productos(id)
);

-- Insertar usuario Administrador por defecto
-- Contraseña original: admin123
-- SHA-256 Hash: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO usuarios (username, password_hash, rol, nombre_completo)
SELECT 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'Administrador General'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

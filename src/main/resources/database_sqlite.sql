-- ============================================
-- NEKOMART - SISTEMA DE VENTAS
-- Script de Base de Datos SQLite
-- Motor: SQLite
-- ============================================

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

-- 5. Tabla de Movimientos de Inventario (KARDEX)
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id INTEGER NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    cantidad INTEGER NOT NULL,
    fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER NOT NULL,
    motivo TEXT,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ============================================
-- DATOS DE EJEMPLO
-- ============================================

-- Usuario Administrador por defecto
-- Contraseña: admin123
INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta)
SELECT 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 'Administrador NekoMart', 'https://i.pravatar.cc/150?img=1'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

-- Empleados de ejemplo
-- Contraseña: user123
INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta)
SELECT 'cami621', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'EMPLEADO', 'Camila López', 'https://i.pravatar.cc/150?img=5'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'cami621');

INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta)
SELECT 'cajera1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'EMPLEADO', 'María García', 'https://i.pravatar.cc/150?img=9'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'cajera1');

INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta)
SELECT 'cajera2', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'EMPLEADO', 'Ana Martínez', 'https://i.pravatar.cc/150?img=10'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'cajera2');

INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta)
SELECT 'supervisor', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'EMPLEADO', 'Carlos Ruiz', 'https://i.pravatar.cc/150?img=12'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'supervisor');

INSERT INTO usuarios (username, password_hash, rol, nombre_completo, foto_ruta)
SELECT 'admin2', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN', 'Roberto Sánchez', 'https://i.pravatar.cc/150?img=16'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin2');

-- 20 Productos de belleza de ejemplo
INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN001', 'Crema Facial Hidratante Snail Mucin', 25.00, 30, 5, 'Cuidado Facial', 1, date('now', '+18 months'), 'L2026001', 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN001');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN002', 'Serum Vitamina C Brightening', 18.50, 25, 5, 'Cuidado Facial', 1, date('now', '+12 months'), 'L2026002', 'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN002');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN003', 'Mascarilla Sheet Mask Aloe Vera', 12.00, 50, 10, 'Mascarillas', 1, date('now', '+24 months'), 'L2026003', 'https://images.unsplash.com/photo-1570194056049-6dd736591911?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN003');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN004', 'Limpiador Facial Espuma Green Tea', 15.00, 35, 6, 'Limpieza', 1, date('now', '+15 months'), 'L2026004', 'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN004');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN005', 'Tónico Facial Rose Water', 14.00, 28, 5, 'Cuidado Facial', 1, date('now', '+18 months'), 'L2026005', 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN005');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN006', 'Protector Solar SPF50+ PA++++', 16.50, 45, 10, 'Protección Solar', 1, date('now', '+20 months'), 'L2026006', 'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN006');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN007', 'Base de Maquillaje Cushion BB Cream', 22.00, 20, 4, 'Maquillaje', 1, date('now', '+14 months'), 'L2026007', 'https://images.unsplash.com/photo-1515688594390-b649af70d282?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN007');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN008', 'Labial Tint Waterproof Long Lasting', 9.50, 50, 10, 'Maquillaje', 1, date('now', '+16 months'), 'L2026008', 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN008');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN009', 'Delineador de Ojos Waterproof Negro', 8.00, 38, 8, 'Maquillaje', 1, date('now', '+22 months'), 'L2026009', 'https://images.unsplash.com/photo-1631730502808-9dd5a5e14061?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN009');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN010', 'Máscara de Pestañas Volume Black', 11.00, 42, 8, 'Maquillaje', 1, date('now', '+18 months'), 'L2026010', 'https://images.unsplash.com/photo-1583001968930-13f3c6461baa?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN010');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN011', 'Paleta de Sombras 18 Colores Nude', 19.00, 15, 3, 'Maquillaje', 1, date('now', '+15 months'), 'L2026011', 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN011');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN012', 'Rubor en Polvo Blush On', 10.50, 32, 6, 'Maquillaje', 1, date('now', '+20 months'), 'L2026012', 'https://images.unsplash.com/photo-1512499617640-c2f999098e95?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN012');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN013', 'Aceite Esencial Tea Tree', 13.00, 25, 5, 'Cuidado Facial', 1, date('now', '+18 months'), 'L2026013', 'https://images.unsplash.com/photo-1616683693504-7ea9f8971a8e?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN013');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN014', 'Exfoliante Facial Scrub', 12.50, 30, 6, 'Limpieza', 1, date('now', '+12 months'), 'L2026014', 'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN014');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN015', 'Crema Contorno de Ojos Anti-Age', 17.00, 22, 4, 'Cuidado Facial', 1, date('now', '+16 months'), 'L2026015', 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN015');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN016', 'Mascarilla Noche Sleeping Pack', 20.00, 18, 4, 'Cuidado Facial', 1, date('now', '+24 months'), 'L2026016', 'https://images.unsplash.com/photo-1570194056049-6dd736591911?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN016');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN017', 'Agua Micelar Cleansing Water 500ml', 14.50, 35, 7, 'Limpieza', 1, date('now', '+18 months'), 'L2026017', 'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN017');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN018', 'Brocha de Maquillaje Set 10 pzas', 16.00, 25, 5, 'Accesorios', 1, date('now', '+20 months'), 'L2026018', 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN018');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN019', 'Espejo de Aumento con LED', 24.00, 12, 3, 'Accesorios', 1, date('now', '+15 months'), 'L2026019', 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN019');

INSERT INTO productos (codigo, nombre, precio, stock, stock_minimo, categoria, activo, fecha_caducidad, lote, imagen_ruta)
SELECT 'CHN020', 'Organizador de Maquillaje Acrílico', 21.00, 15, 3, 'Accesorios', 1, date('now', '+22 months'), 'L2026020', 'https://images.unsplash.com/photo-1631730502808-9dd5a5e14061?w=400'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE codigo = 'CHN020');
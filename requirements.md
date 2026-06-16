# 📋 Requerimientos del Sistema - NekoMart

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Autor:** Equipo de Desarrollo NekoMart

## 🎯 1. DESCRIPCIÓN DEL PROYECTO

Sistema de Punto de Venta (POS) para gestión de inventario, ventas y empleados de una tienda de productos de belleza y cuidado personal.

---

## 👥 2. ACTORES DEL SISTEMA

| Actor             | Descripción                         | Permisos                                               |
| ----------------- | ----------------------------------- | ------------------------------------------------------ |
| **Administrador** | Usuario con acceso total al sistema | CRUD completo de productos, usuarios, ventas, reportes |
| **Empleado**      | Cajero/a de tienda                  | Registrar ventas, consultar historial, ver dashboard   |

---

## ✅ 3. REQUERIMIENTOS FUNCIONALES

### RF-001: Autenticación y Control de Acceso

- **Descripción:** El sistema debe permitir el inicio de sesión con credenciales validadas y control de roles.
- **Criterios:**
  - Validación contra BD con encriptación SHA-256.
  - El admin ve: Dashboard, Inventario, Usuarios, Ventas, Historial.
  - El empleado ve: Dashboard, Ventas, Historial.

### RF-002: Gestión de Inventario

- **Descripción:** El administrador debe poder crear, leer, actualizar y eliminar productos.
- **Criterios:**
  - Campos: código, nombre, precio, stock, stock mínimo, categoría, imagen, fecha caducidad, lote.
  - Búsqueda por nombre o código en tiempo real.
  - Alerta visual cuando stock ≤ stock mínimo.

### RF-003: Proceso de Ventas (POS)

- **Descripción:** Registrar ventas con múltiples productos y métodos de pago.
- **Criterios:**
  - Cálculo automático de subtotal, total y cambio.
  - Métodos de pago: Efectivo, Tarjeta.
  - Descuento automático de inventario y registro en Kardex.

### RF-004: Historial de Ventas y Kardex

- **Descripción:** Visualización de ventas realizadas y movimientos de inventario.
- **Criterios:**
  - Tabla con folio, fecha, total, empleado.
  - Vista detallada de productos por venta.

### RF-005: Gestión de Usuarios

- **Descripción:** Solo el admin puede crear, editar y eliminar usuarios.
- **Criterios:**
  - Roles: ADMIN, EMPLEADO.
  - Validación de username único.

### RF-006: Dashboard de Estadísticas

- **Descripción:** Visualización gráfica de métricas clave.
- **Criterios:**
  - Tarjetas: Ventas hoy, Ventas mes, Tickets hoy, Stock bajo.
  - Gráfico de barras: Ventas últimos 7 días.
  - Tabla: Top 5 productos más vendidos.

### RF-007: Alertas de Stock

- **Descripción:** Alertar visualmente cuando un producto esté por agotarse.
- **Criterios:**
  - Filas resaltadas en inventario cuando stock ≤ stock_minimo.

### RF-008: Cambio de Contraseña

- **Descripción:** Los usuarios pueden cambiar su propia contraseña.
- **Criterios:**
  - Validación de contraseña actual y encriptación SHA-256.

### RF-009: Generación de Facturas (PENDIENTE)

- **Descripción:** Generar facturas en PDF e imprimir.
- **Estado:** _No implementado en esta versión inicial. Planeado para la Fase 10._

---

## ️ 4. REQUERIMIENTOS NO FUNCIONALES

- **RNF-001 Rendimiento:** Carga de dashboard < 2 segundos.
- **RNF-002 Seguridad:** Contraseñas encriptadas con SHA-256.
- **RNF-003 Usabilidad:** Interfaz intuitiva con FlatLaf.
- **RNF-004 Portabilidad:** SQLite (sin servidor), Java 17+.
- **RNF-005 Mantenibilidad:** Arquitectura en capas (DAO, Service, UI).

---

## 📊 5. MODELO DE DATOS

### Tabla: productos

| Campo           | Tipo        | Descripción                   |
| --------------- | ----------- | ----------------------------- |
| id              | INTEGER PK  | Identificador único           |
| codigo          | TEXT UNIQUE | Código de barras/SKU          |
| nombre          | TEXT        | Nombre del producto           |
| precio          | REAL        | Precio de venta               |
| stock           | INTEGER     | Cantidad disponible           |
| stock_minimo    | INTEGER     | Stock mínimo para alerta      |
| categoria       | TEXT        | Categoría del producto        |
| imagen_ruta     | TEXT        | Ruta de la imagen             |
| fecha_caducidad | TEXT        | Fecha de vencimiento          |
| lote            | TEXT        | Número de lote                |
| activo          | INTEGER     | Estado (1=Activo, 0=Inactivo) |

### Tabla: usuarios

| Campo           | Tipo        | Descripción           |
| --------------- | ----------- | --------------------- |
| id              | INTEGER PK  | Identificador único   |
| username        | TEXT UNIQUE | Nombre de usuario     |
| password_hash   | TEXT        | Contraseña encriptada |
| rol             | TEXT        | ADMIN o EMPLEADO      |
| nombre_completo | TEXT        | Nombre completo       |
| foto_ruta       | TEXT        | Ruta de la foto       |

### Tabla: ventas

| Campo          | Tipo        | Descripción         |
| -------------- | ----------- | ------------------- |
| id             | INTEGER PK  | Identificador único |
| folio          | TEXT UNIQUE | Folio de venta      |
| fecha          | TEXT        | Fecha y hora        |
| total          | REAL        | Monto total         |
| metodo_pago    | TEXT        | Efectivo/Tarjeta    |
| monto_recibido | REAL        | Dinero recibido     |
| cambio         | REAL        | Cambio devuelto     |
| empleado_id    | INTEGER FK  | ID del usuario      |

### Tabla: detalle_venta

| Campo           | Tipo       | Descripción         |
| --------------- | ---------- | ------------------- |
| id              | INTEGER PK | Identificador único |
| venta_id        | INTEGER FK | ID de la venta      |
| producto_id     | INTEGER FK | ID del producto     |
| cantidad        | INTEGER    | Cantidad vendida    |
| precio_unitario | REAL       | Precio al momento   |

### Tabla: movimientos_inventario (Kardex)

| Campo       | Tipo       | Descripción                |
| ----------- | ---------- | -------------------------- |
| id          | INTEGER PK | Identificador único        |
| producto_id | INTEGER FK | ID del producto            |
| tipo        | TEXT       | ENTRADA, SALIDA, AJUSTE    |
| cantidad    | INTEGER    | Cantidad del movimiento    |
| fecha       | TEXT       | Fecha y hora               |
| usuario_id  | INTEGER FK | ID del usuario que realizó |
| motivo      | TEXT       | Descripción del movimiento |

---

**Documento elaborado por:** Agente de Desarrollo NekoMart  
**Fecha de última actualización:** 13 de Junio, 2026

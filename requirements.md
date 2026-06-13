# 📋 Requerimientos del Sistema - NekoMart

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Autor:** Equipo de Desarrollo NekoMart

---

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

| ID                          | RF-001                                                                                     |
| --------------------------- | ------------------------------------------------------------------------------------------ |
| **Nombre**                  | Login y Gestión de Sesiones                                                                |
| **Descripción**             | El sistema debe permitir el inicio de sesión con credenciales validadas y control de roles |
| **Prioridad**               | ALTA                                                                                       |
| **Criterios de Aceptación** |

| - El usuario ingresa username y contraseña
| - El sistema valida contra la base de datos con encriptación SHA-256
| - Se crea sesión con SessionManager
| - El rol determina las vistas accesibles
| - El admin ve: Dashboard, Inventario, Usuarios, Ventas, Historial
| - El empleado ve: Dashboard (solo lectura), Ventas, Historial

---

### RF-002: Gestión de Inventario

| ID                          | RF-002                                                                                  |
| --------------------------- | --------------------------------------------------------------------------------------- |
| **Nombre**                  | CRUD de Productos                                                                       |
| **Descripción**             | El administrador debe poder crear, leer, actualizar y eliminar productos del inventario |
| **Prioridad**               | ALTA                                                                                    |
| **Criterios de Aceptación** |

| - Campos: código, nombre, precio, stock, stock mínimo, categoría, imagen
| - Búsqueda por nombre o código en tiempo real
| - Validación de campos obligatorios
| - Actualización automática de stock en ventas
| - Alerta visual cuando stock ≤ stock mínimo

---

### RF-003: Proceso de Ventas (POS)

| ID                          | RF-003                                                                              |
| --------------------------- | ----------------------------------------------------------------------------------- |
| **Nombre**                  | Punto de Venta                                                                      |
| **Descripción**             | El sistema debe permitir registrar ventas con múltiples productos y métodos de pago |
| **Prioridad**               | ALTA                                                                                |
| **Criterios de Aceptación** |

| - Búsqueda de productos por código o nombre
| - Agregar/eliminar productos del carrito
| - Cálculo automático de subtotal, total y cambio
| - Métodos de pago: Efectivo, Tarjeta
| - Generación de folio único por venta
| - Descuento automático de inventario
| - Registro en tabla ventas y detalle_venta

---

### RF-004: Historial de Ventas

| ID                          | RF-004                                                        |
| --------------------------- | ------------------------------------------------------------- |
| **Nombre**                  | Consulta de Ventas                                            |
| **Descripción**             | Visualización de todas las ventas realizadas con sus detalles |
| **Prioridad**               | MEDIA                                                         |
| **Criterios de Aceptación** |

| - Tabla con folio, fecha, total, método de pago, empleado
| - Doble clic para ver detalles de productos
| - Filtros por fecha (opcional)
| - Orden descendente por fecha

---

### RF-005: Gestión de Usuarios

| ID                          | RF-005                                                            |
| --------------------------- | ----------------------------------------------------------------- |
| **Nombre**                  | Administración de Empleados                                       |
| **Descripción**             | Solo el admin puede crear, editar y eliminar usuarios del sistema |
| **Prioridad**               | ALTA                                                              |
| **Criterios de Aceptación** |

| - Campos: username, password, nombre completo, rol, foto
| - Validación de username único
| - Contraseña mínima 4 caracteres
| - Roles: ADMIN, EMPLEADO
| - Subida de foto de perfil

---

### RF-006: Dashboard de Estadísticas

| ID                          | RF-006                                              |
| --------------------------- | --------------------------------------------------- |
| **Nombre**                  | Panel de Control                                    |
| **Descripción**             | Visualización gráfica de métricas clave del negocio |
| **Prioridad**               | MEDIA                                               |
| **Criterios de Aceptación** |

| - Tarjetas: Ventas hoy, Ventas mes, Tickets hoy, Stock bajo
| - Gráfico de barras: Ventas últimos 7 días
| - Tabla: Top 5 productos más vendidos
| - Actualización en tiempo real
| - Accesible para ambos roles (admin completo, empleado solo lectura)

---

### RF-007: Alertas de Stock

| ID                          | RF-007                                                                   |
| --------------------------- | ------------------------------------------------------------------------ |
| **Nombre**                  | Notificación de Stock Bajo                                               |
| **Descripción**             | El sistema debe alertar visualmente cuando un producto esté por agotarse |
| **Prioridad**               | MEDIA                                                                    |
| **Criterios de Aceptación** |

| - Filas en rojo en inventario cuando stock ≤ stock_minimo
| - Ícono de advertencia ⚠️
| - Contador en Dashboard de productos críticos

---

### RF-008: Cambio de Contraseña

| ID                          | RF-008                                           |
| --------------------------- | ------------------------------------------------ |
| **Nombre**                  | Actualización de Credenciales                    |
| **Descripción**             | Los usuarios pueden cambiar su propia contraseña |
| **Prioridad**               | BAJA                                             |
| **Criterios de Aceptación** |

| - Validación de contraseña actual
| - Confirmación de nueva contraseña
| - Mínimo 4 caracteres
| - Encriptación SHA-256

---

### RF-009: Generación de Facturas

| ID                          | RF-009                                         |
| --------------------------- | ---------------------------------------------- |
| **Nombre**                  | Facturación en PDF                             |
| **Descripción**             | Generar, imprimir y enviar facturas por correo |
| **Prioridad**               | BAJA                                           |
| **Criterios de Aceptación** |

| - PDF profesional con logo de NekoMart
| - Datos: folio, fecha, productos, totales
| - Impresión directa
| - Envío por email con adjunto PDF
| - Historial de facturas generadas

---

## 🛠️ 4. REQUERIMIENTOS NO FUNCIONALES

### RNF-001: Rendimiento

- El sistema debe cargar el dashboard en menos de 2 segundos
- Las búsquedas deben responder en menos de 500ms
- Soporte para hasta 10,000 productos en inventario

### RNF-002: Seguridad

- Contraseñas encriptadas con SHA-256
- Sesiones con timeout de 30 minutos de inactividad
- Validación de permisos en cada operación

### RNF-003: Usabilidad

- Interfaz intuitiva con FlatLaf (tema claro/oscuro)
- Atajos de teclado para operaciones frecuentes
- Mensajes de error claros y descriptivos

### RNF-004: Portabilidad

- Compatible con Windows 10/11, Linux, macOS
- Base de datos SQLite (sin configuración de servidor)
- Java 17+ requerido

### RNF-005: Mantenibilidad

- Código documentado en español
- Arquitectura MVC en capas
- Separación clara: DAO, Service, UI

---

## 📊 5. MODELO DE DATOS

### Tabla: productos

| Campo        | Tipo        | Descripción              |
| ------------ | ----------- | ------------------------ |
| id           | INTEGER PK  | Identificador único      |
| codigo       | TEXT UNIQUE | Código de barras/SKU     |
| nombre       | TEXT        | Nombre del producto      |
| precio       | REAL        | Precio de venta          |
| stock        | INTEGER     | Cantidad disponible      |
| stock_minimo | INTEGER     | Stock mínimo para alerta |
| categoria    | TEXT        | Categoría del producto   |
| imagen_ruta  | TEXT        | Ruta de la imagen        |

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

---

## ✅ 6. CRITERIOS DE ACEPTACIÓN GENERAL

1. El sistema debe compilar sin errores con `mvn clean compile`
2. Todas las funcionalidades deben funcionar sin excepciones
3. La base de datos debe crearse automáticamente al primer inicio
4. El login debe funcionar con credenciales precargadas
5. Las imágenes deben guardarse en carpetas locales
6. El sistema debe manejar correctamente cierres inesperados

---

## 📝 7. GLOSARIO

- **POS**: Point of Sale (Punto de Venta)
- **CRUD**: Create, Read, Update, Delete
- **DAO**: Data Access Object
- **MVC**: Model-View-Controller
- **FlatLaf**: Flat Look and Feel (biblioteca de temas para Swing)
- **SQLite**: Base de datos embebida
- **SHA-256**: Algoritmo de encriptación

---

**Documento elaborado por:** Agente de Desarrollo NekoMart  
**Fecha de última actualización:** 10 de Junio, 2026

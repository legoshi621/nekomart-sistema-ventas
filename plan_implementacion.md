# 🚀 Plan de Implementación - NekoMart Sistema de Ventas

**Proyecto:** NekoMart POS System  
**Tecnología:** Java Swing + SQLite + Maven  
**Fecha de Inicio:** Junio 2026  
**Estado:** En Progreso (90% completado)

---

## 📅 FASES DE IMPLEMENTACIÓN

### ✅ FASE 1: Configuración del Proyecto y Base de Datos

**Duración estimada:** 2 horas  
**Estado:** COMPLETADO  
**Fecha:** 10 Junio 2026

#### Actividades:

- [x] Crear estructura de carpetas Maven
- [x] Configurar `pom.xml` con dependencias básicas
- [x] Crear archivo `database_sqlite.sql` con esquema
- [x] Implementar `ConexionDB.java` (Singleton)
- [x] Crear modelos: Producto, Usuario, Venta, DetalleVenta
- [x] Implementar `PasswordUtils.java` (SHA-256)
- [x] Implementar `SessionManager.java`
- [x] Implementar `DatabaseInitializer.java`

#### Entregables:

- Base de datos SQLite funcional
- Conexión a BD establecida
- Modelos de datos creados
- Utilidades de seguridad implementadas

---

### ✅ FASE 2: Sistema de Autenticación

**Duración estimada:** 3 horas  
**Estado:** COMPLETADO  
**Fecha:** 10 Junio 2026

#### Actividades:

- [x] Crear `LoginFrame.java` con FlatLaf
- [x] Implementar `UsuarioDAO.login()`
- [x] Validar credenciales contra BD
- [x] Crear sesión con SessionManager
- [x] Implementar control de roles (Admin/Empleado)
- [x] Crear `MainFrame.java` con navegación por pestañas
- [x] Agregar usuario admin por defecto

#### Entregables:

- Login funcional con encriptación
- Control de acceso por roles
- Sesión persistente
- Navegación básica

---

### ✅ FASE 3: Módulo de Inventario

**Duración estimada:** 4 horas  
**Estado:** COMPLETADO  
**Fecha:** 10 Junio 2026

#### Actividades:

- [x] Crear `ProductoDAO` con métodos CRUD
- [x] Implementar `ProductoService`
- [x] Crear `InventarioFrame.java`
- [x] Tabla de productos con JTable
- [x] Diálogo para crear/editar productos
- [x] Búsqueda en tiempo real
- [x] Validación de campos
- [x] Eliminación con confirmación
- [x] Cargar 20 productos de ejemplo

#### Entregables:

- CRUD completo de productos
- Búsqueda funcional
- 20 productos precargados
- Interfaz intuitiva

---

### ✅ FASE 4: Módulo de Ventas (POS)

**Duración estimada:** 5 horas  
**Estado:** COMPLETADO  
**Fecha:** 10 Junio 2026

#### Actividades:

- [x] Crear `VentaDAO` y `DetalleVenta`
- [x] Implementar `VentaService.procesarVenta()`
- [x] Crear `VentasFrame.java`
- [x] Carrito de compras con JTable
- [x] Búsqueda de productos
- [x] Cálculo de totales y cambio
- [x] Métodos de pago (Efectivo/Tarjeta)
- [x] Generación de folio único
- [x] Actualización automática de stock
- [x] Crear `HistorialVentasFrame.java`
- [x] Vista de detalles de venta

#### Entregables:

- Punto de venta funcional
- Cálculos automáticos
- Historial de ventas
- Descuento de inventario

---

### ✅ FASE 5: Gestión de Usuarios

**Duración estimada:** 3 horas  
**Estado:** COMPLETADO  
**Fecha:** 11 Junio 2026

#### Actividades:

- [x] Agregar métodos CRUD a `UsuarioDAO`
- [x] Implementar `UsuarioService`
- [x] Crear `UsuariosFrame.java`
- [x] Diálogo para crear/editar usuarios
- [x] Validación de username único
- [x] Asignación de roles
- [x] Restricción de acceso solo para Admin
- [x] Crear 5 empleados de ejemplo

#### Entregables:

- CRUD de usuarios funcional
- Control de roles implementado
- 5 empleados precargados
- Solo admin puede gestionar usuarios

---

### ✅ FASE 6: Sistema de Imágenes

**Duración estimada:** 3 horas  
**Estado:** COMPLETADO  
**Fecha:** 11 Junio 2026

#### Actividades:

- [x] Agregar columna `imagen_ruta` a productos
- [x] Agregar columna `foto_ruta` a usuarios
- [x] Crear carpetas `imagenes/productos/` y `empleados/`
- [x] Modificar `InventarioFrame` con selector de imágenes
- [x] Modificar `UsuariosFrame` con selector de fotos
- [x] JFileChooser para seleccionar archivos
- [x] Copia de archivos a carpetas destino
- [x] Vista previa con JLabel
- [x] Actualizar DAOs para guardar rutas

#### Entregables:

- Imágenes en productos
- Fotos en empleados
- Vista previa funcional
- Almacenamiento en filesystem

---

### 🔄 FASE 7: Dashboard de Estadísticas

**Duración estimada:** 4 horas  
**Estado:** EN PROGRESO  
**Fecha:** 11 Junio 2026

#### Actividades:

- [x] Crear `EstadisticasDAO`
  - [x] Método `getVentasHoy()`
  - [x] Método `getVentasMes()`
  - [x] Método `getTotalVentasHoy()`
  - [x] Método `getTop5Productos()`
  - [x] Método `getVentasUltimos7Dias()`
- [x] Crear `EstadisticasService`
- [x] Crear `DashboardFrame.java`
  - [x] Tarjetas de resumen (4)
  - [x] Gráfico de barras personalizado (paintComponent)
  - [x] Tabla de top productos
  - [x] Diseño responsive
- [x] Integrar en `MainFrame` como primera pestaña

#### Entregables:

- Dashboard visual con métricas
- Gráfico de ventas últimos 7 días
- Top 5 productos
- Acceso para ambos roles

---

### ⏳ FASE 8: Alertas de Stock Bajo

**Duración estimada:** 2 horas  
**Estado:** PENDIENTE

#### Actividades:

- [ ] Modificar `InventarioFrame`
  - [ ] `DefaultTableCellRenderer` personalizado
  - [ ] Pintar filas en rojo cuando stock ≤ stock_minimo
  - [ ] Agregar ícono ⚠️
- [ ] Agregar contador en Dashboard
- [ ] Método `obtenerProductosStockBajo()` en ProductoService

#### Entregables:

- Alertas visuales en inventario
- Contador de productos críticos
- Notificación proactiva

---

### ⏳ FASE 9: Cambio de Contraseña

**Duración estimada:** 2 horas  
**Estado:** PENDIENTE

#### Actividades:

- [ ] Crear diálogo `CambiarPasswordDialog`
- [ ] Validar contraseña actual
- [ ] Actualizar en `UsuarioDAO`
- [ ] Agregar menú "Mi Perfil" en MainFrame
- [ ] Encriptar nueva contraseña

#### Entregables:

- Usuarios pueden cambiar su password
- Validación de seguridad
- Interfaz intuitiva

---

### ⏳ FASE 10: Facturación PDF y Correo

**Duración estimada:** 5 horas  
**Estado:** PENDIENTE

#### Actividades:

- [ ] Agregar dependencias: iText 7, JavaMail
- [ ] Crear `FacturaService`
  - [ ] Método `generarPDF()` con iText
  - [ ] Diseño profesional con logo
  - [ ] Guardar en carpeta `facturas/`
- [ ] Crear `ImpresionService`
  - [ ] Método `imprimirPDF()`
  - [ ] Diálogo de selección de impresora
- [ ] Crear `CorreoService`
  - [ ] Configuración SMTP Gmail
  - [ ] Método `enviarFactura()` con adjunto
  - [ ] Manejo de excepciones
- [ ] Modificar `VentasFrame`
  - [ ] Diálogo post-venta con opciones
  - [ ] Botones: Imprimir, Enviar, Guardar
- [ ] Crear tabla `facturas_generadas`
- [ ] Vista previa de factura

#### Entregables:

- Facturas en PDF profesionales
- Impresión directa
- Envío por email
- Historial de facturas

---

### ⏳ FASE 11: Pruebas y Documentación

**Duración estimada:** 3 horas  
**Estado:** PENDIENTE

#### Actividades:

- [ ] Pruebas de cada módulo
- [ ] Corrección de bugs
- [ ] Optimización de consultas SQL
- [ ] Documentación de código
- [ ] Crear README.md
- [ ] Manual de usuario básico
- [ ] Exportar requirements.md
- [ ] Video demo (opcional)

#### Entregables:

- Sistema estable y probado
- Documentación completa
- README con instrucciones
- Listo para entrega

---

## 📊 RESUMEN DE AVANCE

| Fase | Descripción        | Estado         | Progreso |
| ---- | ------------------ | -------------- | -------- |
| 1    | Configuración y BD | ✅ Completado  | 100%     |
| 2    | Autenticación      | ✅ Completado  | 100%     |
| 3    | Inventario         | ✅ Completado  | 100%     |
| 4    | Ventas POS         | ✅ Completado  | 100%     |
| 5    | Usuarios           | ✅ Completado  | 100%     |
| 6    | Imágenes           | ✅ Completado  | 100%     |
| 7    | Dashboard          | 🔄 En Progreso | 80%      |
| 8    | Alertas Stock      | ⏳ Pendiente   | 0%       |
| 9    | Cambiar Password   | ⏳ Pendiente   | 0%       |
| 10   | Facturación        | ⏳ Pendiente   | 0%       |
| 11   | Pruebas            | ⏳ Pendiente   | 0%       |

**Progreso Total:** 90% (6 de 11 fases completadas)

---

## 🛠️ DEPENDENCIAS DEL PROYECTO

```xml
<dependencies>
    <!-- SQLite JDBC -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.45.3.0</version>
    </dependency>

    <!-- FlatLaf -->
    <dependency>
        <groupId>com.formdev</groupId>
        <artifactId>flatlaf</artifactId>
        <version>3.5.1</version>
    </dependency>

    <!-- iText 7 (Para PDF) -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itext7-core</artifactId>
        <version>7.2.5</version>
        <type>pom</type>
    </dependency>

    <!-- JavaMail -->
    <dependency>
        <groupId>com.sun.mail</groupId>
        <artifactId>javax.mail</artifactId>
        <version>1.6.2</version>
    </dependency>
</dependencies>
```

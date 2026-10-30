# Especificación de Requerimientos de Software (ERS)

**Proyecto:** MAHN 3.0 · Museo Americano de Historia Natural
**Curso:** Programación II, I Ciclo 2025 · Universidad Nacional, Sede Regional Brunca
**Basado en:** IEEE 830

## 1. Introducción

### 1.1 Propósito
Definir los requerimientos de la aplicación de escritorio que usará la administración del Museo Americano de Historia
Natural para registrar la información del museo y de sus visitantes, vender entradas en las cajas, autorizar el ingreso
a las salas mediante códigos QR, recibir valoraciones de las salas y generar reportes.

### 1.2 Alcance
El sistema MAHN 3.0 cubre cinco módulos: mantenimientos, entradas, registro de comisiones, valoración de salas y
reportes. No incluye venta en línea, cobro real con la tarjeta (solo se registra la marca) ni control de usuarios.

### 1.3 Definiciones
| Término | Significado |
|---|---|
| Sala | Espacio de exhibición de un museo con su propia temática (el MAHN tiene 46). |
| Colección | Conjunto de piezas que se exhibe en una sala, con su siglo y descripción. |
| Especie | Ser vivo (actual o extinto) que se exhibe dentro de una colección. |
| Temática | Tema de una sala que no exhibe especies (por ejemplo, meteoritos o pueblos del Pacífico). |
| Precio diferido | Precio distinto que tiene cada sala los domingos. |
| Comisión | Porcentaje que cobra la marca de la tarjeta por cada transacción. |
| QR de entrada | Código con el texto `MAHN-ENTRADA:<código de 12 caracteres>`. |
| QR de sala | Código con el texto `MAHN-SALA:<id de la sala>`. |
| IVA | Impuesto al valor agregado del 13 % que se suma al subtotal de la entrada. |

## 2. Descripción general

### 2.1 Perspectiva del producto
Aplicación de escritorio Java con interfaz JavaFX, desarrollada en NetBeans como proyecto Maven, que se conecta por JDBC
a una base de datos MySQL 8 local o en red.

### 2.2 Usuarios
| Usuario | Tareas |
|---|---|
| Administración del museo | Mantenimientos y reportes. |
| Personal de cajas | Venta de entradas e historial de entradas. |
| Personal de acceso a salas | Validación de entradas con el lector de QR. |
| Visitante | Valoración anónima de las salas desde una terminal con lector de QR. |

### 2.3 Restricciones
- Java 17 o superior, JavaFX 21, MySQL 8.0.16 o superior (por las restricciones `CHECK`).
- NetBeans y Maven para compilar; Git como repositorio.
- Los montos se manejan en dólares (USD) con dos decimales.
- La validación de entradas usa la fecha del sistema.

### 2.4 Suposiciones
- Las cajas cuentan con un lector USB de códigos QR que funciona como teclado; si no lo hay, el código se puede
  escribir o se puede cargar la imagen/PDF del QR.
- La base de datos se crea con el script `database/MAHN_DB.sql`.

## 3. Requerimientos funcionales

### 3.1 Módulo de mantenimientos (Interfaz No. 1)

Todos los mantenimientos muestran una tabla con los registros, un filtro de texto (`TextField`), un filtro `ComboBox`
cargado desde la base de datos y los botones **ELIMINAR**, **EDITAR** e **INSERTAR**. Insertar y editar abren un
formulario modal que valida los datos antes de guardar; eliminar pide confirmación.

| ID | Requerimiento | Datos | Filtros (texto / ComboBox) |
|---|---|---|---|
| RF-01 | Mantenimiento de museos | Nombre, tipo (Arte, Historia, Musical, Militar), ubicación, fecha de fundación, director, sitio web | Nombre, director o ubicación / tipo de museo |
| RF-02 | Mantenimiento de salas | Museo, área, nombre, descripción | Nombre, descripción o área / museo |
| RF-03 | Mantenimiento de colecciones | Sala, nombre, siglo (romano, a. C. o d. C.), descripción | Nombre o descripción / sala |
| RF-04 | Mantenimiento de especies | Colección, nombre científico, nombre común, fecha de extinción, época, peso, tamaño, características, imagen | Nombre o época / colección |
| RF-05 | Mantenimiento de temáticas | Sala, nombre, características, época, imagen | Nombre, época o características / sala |
| RF-06 | Mantenimiento de precios | Sala, precio de lunes a sábado, precio de domingo | Sala / museo |
| RF-07 | Mantenimiento de comisiones de tarjetas | Tipo de tarjeta (VISA, Mastercard, American Express, Diners Club, Union Pay), porcentaje de comisión, estado | Nombre / tipo de tarjeta |
| RF-08 | Código QR de sala | Desde el mantenimiento de salas se genera la imagen JPG del QR de una sala o de todas las listadas | — |

**Validaciones:** campos obligatorios y longitudes máximas; nombres de persona solo con letras; sitio web con
`http://` o `https://`; fechas de fundación no futuras; montos y porcentajes numéricos dentro de rango (precio ≥ 0,
comisión entre 0 y 100, peso y tamaño > 0); siglo distinto de cero; no se permiten nombres duplicados; no se puede
eliminar un registro con información asociada (se muestra qué información lo impide).

### 3.2 Módulo de entradas

| ID | Requerimiento | Descripción |
|---|---|---|
| RF-09 | Vender entrada (Interfaz No. 2) | El cajero ingresa el nombre del visitante, el tipo de tarjeta y el museo; marca una o varias salas y elige el día en un calendario (no permite días pasados ni más de un año después). AGREGAR crea una línea por sala y día con el precio de la base de datos: lunes a sábado o domingo. Se muestran subtotal, IVA (13 %) y total. VENDER pide confirmación y guarda la venta en una transacción. |
| RF-10 | Generar código QR | Al vender se genera un código único de 12 caracteres, la imagen **JPG** del QR y un comprobante **PDF** con el QR, las salas, las fechas y los totales. |
| RF-11 | Autorizar entrada (Interfaz No. 3) | Al leer el QR (lector, texto, imagen JPG/PNG o PDF) se busca la entrada y se muestran las salas a las que puede ingresar **hoy** según la fecha del sistema, además de sus otras fechas. Si no hay salas para hoy, se indica la próxima fecha válida. |
| RF-12 | Ocultar elementos hasta leer | En las interfaces 3 y 4 solo se ven el título y el campo de lectura hasta que se lee un código válido. |
| RF-13 | Historial de entradas | Lista de entradas vendidas filtrable por visitante o código y por tarjeta; permite volver a generar el QR y el comprobante. |

### 3.3 Registro de comisiones

| ID | Requerimiento | Descripción |
|---|---|---|
| RF-14 | Registro automático | Cada vez que se vende una entrada, el disparador `tg_entradas_ai` guarda en `comisiones_cobradas` la tarjeta, el porcentaje vigente, el monto cobrado y la comisión (`total × % / 100`), sin intervención del usuario. |

### 3.4 Módulo de valoración de salas (Interfaz No. 4)

| ID | Requerimiento | Descripción |
|---|---|---|
| RF-15 | Ficha de la sala | Al leer el QR de una sala se muestra su nombre, museo, área y descripción, con un carrusel de imágenes de sus especies (o de sus temáticas) y los datos de cada una. |
| RF-16 | Valoración de la sala | El visitante elige de 1 a 5 estrellas y puede escribir una observación de hasta 500 caracteres. Es anónima. |
| RF-17 | Promedio de valoración | La ficha muestra el promedio en estrellas y en número, la cantidad de valoraciones y los comentarios recientes. |

### 3.5 Módulo de reportes

| ID | Requerimiento | Descripción |
|---|---|---|
| RF-18 | Comisiones por rango de fechas | Total de comisión a pagar a cada marca de tarjeta entre dos fechas (inclusive), con transacciones, monto procesado, totales y gráfico de barras. Se exporta a PDF. |
| RF-19 | Valoración de las salas | Salas mejor valoradas de un museo (o de todos); se puede invertir para obtener las diez peores. Se exporta a PDF. |

## 4. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | **Validaciones y mensajes:** todos los errores se manejan con excepciones (`ValidacionException`, `DatosException`) y se muestran mensajes explicativos en español; los errores de MySQL (duplicados, llaves foráneas, `CHECK`, conexión) se traducen. |
| RNF-02 | **Integridad:** la venta se guarda en una transacción; las restricciones `CHECK`, `UNIQUE` y de llave foránea garantizan datos consistentes (por ejemplo, `total = subtotal + IVA`). |
| RNF-03 | **Seguridad:** todas las consultas usan sentencias preparadas; la contraseña de MySQL se guarda en `config/db.properties`, que no se sube al repositorio. |
| RNF-04 | **Usabilidad:** interfaz en español, con el menú lateral de las interfaces del enunciado, mensajes de confirmación y formularios que no se cierran si hay errores. |
| RNF-05 | **Portabilidad:** se ejecuta desde NetBeans (`javafx:run`) o con el JAR `target/MAHN-3.0.jar` en Windows, Linux o macOS. |
| RNF-06 | **Mantenibilidad:** arquitectura por capas (modelo, datos/DAO, servicios, controladores, vistas FXML) y pruebas automáticas con JUnit 5. |

## 5. Interfaces

| Interfaz | Pantalla |
|---|---|
| No. 1 Mantenimientos | `Mantenimiento.fxml` (compartida por los 7 mantenimientos) |
| No. 2 Venta de entrada | `VentaEntrada.fxml` |
| No. 3 Validar entrada | `ValidarEntrada.fxml` |
| No. 4 Valoración de salas | `Valoracion.fxml` |
| Reportes | `ReporteComisiones.fxml`, `ReporteValoraciones.fxml` |
| Adicionales | `Inicio.fxml` (pantalla principal con indicadores), `HistorialEntradas.fxml` |

Las capturas de pantalla están en [docs/capturas](capturas).

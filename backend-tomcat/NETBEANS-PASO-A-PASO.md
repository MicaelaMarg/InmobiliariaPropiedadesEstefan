# NetBeans – Paso a paso (desde cero)

Guía para abrir el backend de la inmobiliaria en NetBeans, configurar Tomcat y que todo funcione.

---

## Antes de empezar

- **Java 17** instalado.
- **Apache Tomcat 10** (o 10.1.x) descargado y descomprimido en tu Mac (por ejemplo en Descargas o Documentos). La carpeta se llama algo como `apache-tomcat-10.1.49`.
- **NetBeans** con soporte para Maven y Java EE/Web (Apache NetBeans 12 o posterior).

---

## Paso 1 – Abrir solo el backend en NetBeans

1. Abrí **NetBeans**.
2. Menú **File** → **Open Project** (o en la pantalla de inicio, **Open Project**).
3. Navegá hasta la carpeta de tu proyecto, por ejemplo:  
   `Documents/inmobiliaria`
4. **Importante:** no abras la carpeta `inmobiliaria`. Abrí **solo** la carpeta **`backend-tomcat`** (la que tiene el archivo `pom.xml` dentro).
5. Clic en **Open Project**.
6. En el panel **Projects** (izquierda) deberías ver un solo proyecto, por ejemplo **inmobiliaria-api**, con un icono de globo (proyecto Maven web).

---

## Paso 2 – Agregar el servidor Apache Tomcat

1. Abrí la pestaña **Services** (al lado de **Projects** y **Files**).
2. Expandí **Servers** (si está colapsado).
3. Clic derecho en **Servers** → **Add Server**.
4. En la lista elegí **Apache Tomcat** (o **Apache Tomcat or TomEE**) → **Next**.
5. En **Server Location (Catalina Home)**:
   - **No** pongas la carpeta de tu proyecto (`inmobiliaria`).
   - Tenés que poner la carpeta **donde está instalado Tomcat** en tu Mac (donde descomprimiste el .zip o .tar.gz). Por ejemplo:  
     `/Users/tu-usuario/Downloads/apache-tomcat-10.1.49`  
   - Podés usar **Browse...** para elegir esa carpeta. Dentro tiene que haber carpetas como `bin`, `lib`, `conf`, `webapps`.
6. Si te dice *"Tomcat with the same Catalina Home folder is already registered"*, significa que ya lo tenés: dale **Cancel** y en el Paso 3 usá ese Tomcat que ya aparece en **Servers**.
7. Si todo va bien: **Next** → (usuario/contraseña si te pide, podés dejarlos en blanco o completar) → **Finish**.

---

## Paso 3 – Asignar Tomcat al proyecto

1. Volvé a la pestaña **Projects**.
2. Clic derecho en el proyecto **inmobiliaria-api** → **Properties**.
3. En el panel izquierdo elegí **Run**.
4. En **Server** (o "Deployment server") elegí tu **Apache Tomcat** de la lista (no "Embedded Lightweight" ni "Internal Webserver").
5. **OK**.

Si al dar **Run** más adelante te sale el cuadro **"Select deployment server"**:

- En **Server:** elegí **Apache Tomcat** (o Apache Tomcat or TomEE).
- Marcá **"Remember Permanently"**.
- **OK**.

---

## Paso 4 – Compilar y desplegar

1. Clic derecho en **inmobiliaria-api** → **Clean and Build**.
2. Esperá a que termine (abajo en **Output** debería decir "BUILD SUCCESS").
3. Clic derecho en **inmobiliaria-api** → **Run** (o el botón verde ▶ o F6).
4. NetBeans va a iniciar Tomcat y desplegar la aplicación. En la pestaña **Output** vas a ver los mensajes de Tomcat.
5. Si no aparece ningún error en rojo, la API está disponible en:  
   **http://localhost:8080/inmobiliaria-api/api**

---

## Paso 5 – Comprobar que el backend responda

1. Abrí el navegador.
2. Entrá a: **http://localhost:8080/inmobiliaria-api/api/properties**
3. Deberías ver un JSON (por ejemplo una lista `[]` o con una propiedad de prueba). Eso significa que el backend está bien.
4. Si ves **404**: la aplicación no arrancó. Mirá la pestaña **Output** en NetBeans; si aparece "No se pudo inicializar la base de datos" o "Column count does not match", revisá el archivo `PropertyRepository.java` (ver README, sección "Si recibís 404").

---

## Paso 6 – Ver la web de la inmobiliaria (frontend)

El backend solo es la API. La página que ves (catálogo, admin) es el **frontend** (Vue).

1. Abrí una **terminal** (fuera de NetBeans).
2. Entrá a la carpeta **raíz** del proyecto:  
   `cd /Users/micaelamargaritamattiucci/Documents/inmobiliaria`
3. Ejecutá: `npm run dev`
4. En el navegador abrí: **http://localhost:5173**
5. Ahí ves la inmobiliaria. Lo que el admin sube se guarda porque el frontend usa la API del backend (gracias al archivo `.env` en la raíz).

---

## Resumen rápido

| Paso | Qué hacer |
|------|-----------|
| 1 | Abrir en NetBeans **solo** la carpeta **backend-tomcat**. |
| 2 | **Services** → **Servers** → **Add Server** → Apache Tomcat → elegir la carpeta de Tomcat (no la del proyecto). |
| 3 | **Properties** del proyecto → **Run** → **Server** = Apache Tomcat. |
| 4 | **Clean and Build** → **Run**. |
| 5 | Probar en el navegador: **http://localhost:8080/inmobiliaria-api/api/properties** |
| 6 | Para ver la web: en terminal `npm run dev` y abrir **http://localhost:5173** |

Si algo no coincide con tu NetBeans (por ejemplo no tenés "Run" en Properties), decime qué versión de NetBeans usás y lo adaptamos.

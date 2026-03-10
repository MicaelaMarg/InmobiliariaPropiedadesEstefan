# Backend Tomcat

Backend Java para persistir propiedades en una base H2 embebida y desplegar como WAR en Apache Tomcat.

## Requisitos

- Java 17
- Maven 3.9+
- Apache Tomcat 10+
- NetBeans con soporte Maven/Java Web

## Abrir y ejecutar en NetBeans

**Guía detallada:** para hacer todo desde cero (abrir proyecto, agregar Tomcat, asignar servidor, compilar y desplegar), seguí el archivo **[NETBEANS-PASO-A-PASO.md](NETBEANS-PASO-A-PASO.md)**.

**Importante:** en NetBeans tenés que abrir la carpeta **`backend-tomcat`** (la que contiene este `pom.xml`), no la raíz del repositorio.

### Paso 1 – Abrir el proyecto

1. En NetBeans: **File → Open Project** (o en la pantalla de inicio, **Open Project**).
2. Navegá hasta la carpeta de tu proyecto (por ejemplo `Documents/inmobiliaria`).
3. Elegí **solo la carpeta `backend-tomcat`** y dale **Open Project**.
4. En el árbol **Projects** deberías ver el proyecto (por ejemplo **inmobiliaria-api**).

### Paso 2 – Configurar Apache Tomcat

1. Abrí la ventana **Services** (Window → Services, o pestaña **Services**).
2. Clic derecho en **Servers** → **Add Server**.
3. Elegí **Apache Tomcat** (o Apache Tomcat EE si usás esa versión) → **Next**.
4. En **Server Location** indicá la carpeta donde tenés Tomcat instalado en tu Mac (donde descomprimiste el .tar.gz o donde lo instalaste).
5. Completá el asistente (puerto 8080 está bien) y **Finish**.

### Paso 3 – Asignar Tomcat al proyecto

1. Clic derecho en el proyecto **inmobiliaria-api** (en Projects) → **Properties**.
2. En el panel izquierdo elegí **Run**.
3. En **Server** seleccioná el **Apache Tomcat** que agregaste.
4. **OK**.

### Paso 4 – Compilar y ejecutar

1. Clic derecho en el proyecto → **Clean and Build** (para compilar el WAR).
2. Luego **Run** (o el botón Play verde, o F6).
3. NetBeans despliega la aplicación en Tomcat y lo inicia. En la pestaña **Output** deberías ver que Tomcat está corriendo.
4. La API queda disponible en: **http://localhost:8080/inmobiliaria-api/api**

### Ver la web de la inmobiliaria

El backend solo expone la API. Para ver el catálogo y el panel admin:

1. En una terminal, en la **raíz del proyecto** (carpeta `inmobiliaria`, no `backend-tomcat`):  
   `npm install` y luego `npm run dev`.
2. En el navegador abrí **http://localhost:5173** (el frontend Vue usa la API del backend si existe el archivo `.env` en la raíz con `VITE_API_URL=http://localhost:8080/inmobiliaria-api/api`).

## Base de datos

### H2 (por defecto)

- Si **no** definís `MYSQL_DATABASE`, el backend usa H2 (archivo local).
- La base se crea en `TOMCAT_BASE/data/inmobiliaria-db/inmobiliaria.mv.db`.
- Podés cambiar la ruta con la propiedad Java `-Dinmobiliaria.db.path=/ruta/a/inmobiliaria`.
- No se crea ningún admin por defecto: la primera cuenta se crea desde **/admin/setup** en el navegador.

### MySQL

Si querés usar **MySQL** (por ejemplo para ver los datos desde phpMyAdmin u otro cliente):

1. **Creá la base en MySQL** (opcional: el backend puede crearla si tiene permisos):
   - En phpMyAdmin o desde la consola: crear base de datos `inmobiliaria` (o el nombre que elijas).

2. **Configurá las variables de entorno** en el servidor (NetBeans → Properties → Run, o en Tomcat):
   - `MYSQL_DATABASE` = nombre de la base (ej. `inmobiliaria`) — **obligatorio** para activar MySQL.
   - `MYSQL_HOST` = host (por defecto `localhost`).
   - `MYSQL_PORT` = puerto (por defecto `3306`).
   - `MYSQL_USER` = usuario MySQL (por defecto `root`).
   - `MYSQL_PASSWORD` = contraseña de ese usuario.

3. **Reiniciá el backend.** Al arrancar, crea las tablas en MySQL si no existen. **No** se crea ningún admin por defecto.

4. **Creá tu cuenta desde el navegador:**  
   Abrí la app (frontend) → **Admin** → en el login hacé clic en **"¿Primera vez? Crear cuenta de administrador"** (o entrá directo a `/admin/setup`). Completá email y contraseña (mínimo 8 caracteres, mayúscula, número y símbolo) y creá la cuenta. Después usá ese email y contraseña para iniciar sesión.

- Podés abrir MySQL desde el navegador (phpMyAdmin) o con cualquier cliente y ver las tablas: `admin_users`, `properties`, `property_images`, `password_reset_tokens`, `login_attempts`.

## Endpoints

- `GET /inmobiliaria-api/api/properties`
- `GET /inmobiliaria-api/api/properties?featured=1`
- `GET /inmobiliaria-api/api/properties/by-slug/{slug}`
- `GET /inmobiliaria-api/api/admin/properties`
- `GET /inmobiliaria-api/api/admin/properties/{id}`
- `POST /inmobiliaria-api/api/admin/properties`
- `PUT /inmobiliaria-api/api/admin/properties/{id}`
- `DELETE /inmobiliaria-api/api/admin/properties/{id}`

## Login real (admin)

El admin usa **login real**: usuario y contraseña se guardan en la base (tabla `admin_users`) con contraseña hasheada con BCrypt. El backend devuelve un **JWT** que el frontend envía en `Authorization: Bearer ...` en cada petición a `/api/admin/*`; el filtro valida firma y expiración del token.

- **Primer uso:** no hay email ni contraseña por defecto. Creá tu cuenta en **http://localhost:5173/admin/setup** (o desde el login, "¿Primera vez? Crear cuenta de administrador"). Después usá ese correo y contraseña para iniciar sesión.
- **Producción:** definí `JWT_SECRET` (clave para firmar el JWT, mínimo 32 caracteres) en el entorno del servidor. Ejemplo: `JWT_SECRET=tu-clave-secreta-muy-larga-y-aleatoria`
- **Endpoint de login:** `POST /api/auth/login` con body `{ "email": "...", "password": "..." }`. Respuesta: `{ "token": "<jwt>", "user": { "email": "..." } }`

## Recuperar contraseña (Gmail)

Para que "¿Olvidaste tu contraseña?" envíe el correo por Gmail, configurá estas variables de entorno (o parámetros de contexto) **en el servidor donde corre Tomcat**:

| Variable | Descripción |
|----------|-------------|
| `SMTP_USER` | Tu dirección Gmail (ej. mattiuccimicaelammm@gmail.com). |
| `SMTP_APP_PASSWORD` | Contraseña de aplicación de Gmail (no la contraseña normal). Creala en: Cuenta Google → Seguridad → Verificación en 2 pasos (activada) → Contraseñas de aplicaciones. |
| `FRONTEND_URL` | URL base del frontend para armar el enlace del correo. En local: `http://localhost:5173`. En producción: `https://tudominio.com`. |

Sin estas variables, al solicitar recuperar contraseña verás un error tipo "El envío de correo no está configurado".

**Si "no pasa nada" (no llega el correo):**
1. **Email correcto:** El admin por defecto (primera vez) es `mattiuccimicaelammm@gmail.com`. Si arrancaste la app antes con otro seed, el admin puede ser `admin@inmobiliaria.com` — probá con ese.
2. **Pestaña Output de NetBeans:** Si ves `Forgot password: no hay admin con email ...` el email que pusiste no está en la base. Si ves `correo no configurado` faltan las variables de entorno.
3. **Variables en Tomcat/NetBeans:** En las propiedades del proyecto → Run, o en la configuración del servidor, agregá las variables de entorno `SMTP_USER`, `SMTP_APP_PASSWORD`, `FRONTEND_URL`. Reiniciá el servidor después.
4. **Gmail:** Usá una *contraseña de aplicación* (Cuenta Google → Seguridad → Verificación en 2 pasos → Contraseñas de aplicaciones), no la contraseña normal.
5. **Spam:** Revisá la carpeta de spam del correo.

## Seguridad

- **Contraseñas:** mínimo 8 caracteres, al menos una mayúscula, un número y un símbolo (validado en login, recuperación y restablecimiento).
- **Fuerza bruta:** tras 5 intentos fallidos de login (por email + IP) la cuenta queda bloqueada 15 minutos; el backend responde 429 con mensaje claro.
- **Cabeceras de seguridad:** el backend envía `X-Frame-Options`, `X-Content-Type-Options`, `X-XSS-Protection` y `Content-Security-Policy`.
- **CAPTCHA (opcional):** Cloudflare Turnstile. Si no lo configurás, el login funciona igual.
  - Backend: variable de entorno `TURNSTILE_SECRET_KEY` (clave secreta de Cloudflare). Si está definida, el login exige un token Turnstile válido.
  - Frontend: en la raíz del proyecto (donde está el frontend Vue), en `.env`: `VITE_TURNSTILE_SITE_KEY=tu-site-key`. Si está definida, se muestra el widget en la pantalla de login.

## Si recibís 404 (Not Found) al usar el frontend

El mensaje `POST http://localhost:8080/inmobiliaria-api/api/admin/properties 404` suele significar que **la aplicación no llegó a arrancar** en Tomcat (por ejemplo por un error al inicializar la base de datos).

1. **Recompilar y desplegar de nuevo**
   - En NetBeans: clic derecho en **inmobiliaria-api** → **Clean and Build**.
   - Luego **Run** (F6) para desplegar otra vez en Tomcat.

2. **Revisar la pestaña Output**
   - Si aparece `No se pudo inicializar la base de datos` o `Column count does not match`, la base no arrancó. En `PropertyRepository.java`, en el `insert into properties`, la línea `values (?, ?, ...)` debe tener **exactamente 28** signos `?` (uno por cada columna). Si hay 29, borrá uno.

3. **Comprobar que el backend responda**
   - En el navegador abrí: **http://localhost:8080/inmobiliaria-api/api/properties**
   - Si responde con un JSON (aunque sea `[]`), el backend está bien y la URL del frontend es correcta.
   - Si da 404, la app no está desplegada o Tomcat no está corriendo.

4. **Si la app no arranca por error de base de datos:** borrá la base H2 antigua para que se cree de nuevo. La carpeta suele estar en:
   - Dentro de la instalación de Tomcat: `tomcat/data/inmobiliaria-db/`
   - O en tu usuario: `~/data/inmobiliaria-db/`
   Eliminá la carpeta **inmobiliaria-db** (o los archivos `inmobiliaria.mv.db` e `inmobiliaria.trace.db`), reiniciá Tomcat y volvé a desplegar.

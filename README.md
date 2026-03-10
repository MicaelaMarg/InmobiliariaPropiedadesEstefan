# Sistema Inmobiliario - Vue 3

Aplicación web moderna para una inmobiliaria: sitio público (catálogo, detalle, contacto) y panel administrador (CRUD propiedades, imágenes, dashboard).

## Stack

- Vue 3 (Composition API)
- Vue Router 4
- Pinia
- Tailwind CSS
- Vite

## Cómo ejecutar

```bash
npm install
npm run dev
```

Abrí [http://localhost:5173](http://localhost:5173).

## Backend real con Tomcat

El repo incluye un backend Java en **`backend-tomcat`** para persistir propiedades en una base H2.

**Para NetBeans:** abrí **solo la carpeta `backend-tomcat`** (File → Open Project → elegir la carpeta `backend-tomcat`). La guía paso a paso está en **`backend-tomcat/README.md`**.

1. Abrí `backend-tomcat` en NetBeans como proyecto Maven (ver guía detallada en `backend-tomcat/README.md`).
2. Configurá Apache Tomcat 10+ en NetBeans (Services → Servers → Add Server).
3. En Properties → Run del proyecto, asigná el servidor Tomcat.
4. Hacé **Clean and Build** y luego **Run** (F6) para desplegar el WAR.
5. El archivo `.env` en la raíz ya está configurado con `VITE_API_URL=http://localhost:8080/inmobiliaria-api/api`. Levantá el frontend con `npm run dev` para ver la web en http://localhost:5173.

## Rutas

- **Sitio público:** `/` (home), `/catalogo`, `/propiedad/:slug`, `/contacto`
- **Admin (login):** `/admin/login`
- **Panel admin:** `/admin` (dashboard), `/admin/propiedades`, `/admin/propiedades/nueva`, `/admin/propiedades/:id/editar`, `/admin/usuarios` (administradores), `/admin/configuracion`

## Credenciales de admin (login real)

Con el backend desplegado y **MySQL** (o sin usuarios previos), el primer administrador se crea desde el navegador en **`/admin/setup`**: ingresá email y contraseña (mín. 8 caracteres, mayúscula, número y símbolo). Los administradores adicionales se crean desde el panel → **Administradores**.

**Importante:** en producción configurá **JWT_SECRET** (variable de entorno) para firmar los tokens y usá **HTTPS** en el servidor (ver `backend-tomcat/README.md`).

## Datos mock

Las propiedades y el listado público usan datos en memoria definidos en `src/data/mockProperties.js`. Para usar una API real:

1. Definí `VITE_API_URL` en `.env` (ver `.env.example`).
2. Usá el backend de `backend-tomcat` o conectá otro backend compatible.

## Estructura principal

- `src/views/public/` – Páginas del sitio público
- `src/views/admin/` – Páginas del panel admin
- `src/components/` – Componentes reutilizables (property, contact, layout, ui, admin)
- `src/stores/` – Pinia (auth, app)
- `src/services/` – Llamadas a API / mock
- `src/router/` – Rutas y guard de autenticación

## Build para producción

```bash
npm run build
```

Los archivos quedan en `dist/`.

## Deploy rápido del frontend

El frontend es una SPA de Vite. Eso significa que el hosting debe reenviar las rutas del navegador a `index.html` para que funcionen URLs como `/catalogo`, `/propiedad/casa-centro` o `/admin/login`.

### Vercel

- Importá el repositorio desde GitHub.
- Framework preset: **Vite**
- Build command: `npm run build`
- Output directory: `dist`
- Variable de entorno: `VITE_API_URL=https://tu-backend/api`
- El archivo `vercel.json` ya deja configurado el rewrite de rutas.

### Netlify

- Importá el repositorio desde GitHub.
- Build command: `npm run build`
- Publish directory: `dist`
- Variable de entorno: `VITE_API_URL=https://tu-backend/api`
- El archivo `public/_redirects` ya deja configurado el fallback SPA.

### Importante sobre el backend

- **Vercel y Netlify sirven para el frontend**, no para el backend Java/Tomcat de este repo.
- Para la API necesitás un servidor aparte con **Java 17 + Tomcat 10+**.
- En producción conviene usar **MySQL** y definir `JWT_SECRET`, `FRONTEND_URL`, y si corresponde `SMTP_USER` / `SMTP_APP_PASSWORD`.

## Subir a la red (que lo use el usuario final)

Sí, con esto podés subir la app a internet y que los usuarios entren al sitio y los admins usen el panel con normalidad.

### 1. Frontend (sitio público + panel admin)

- Ejecutá **`npm run build`**. Se genera la carpeta **`dist/`** con el sitio listo para servir.
- Subí el contenido de **`dist/`** a tu hosting:
  - **Vercel / Netlify:** conectá el repo y usá `npm run build` como comando de build; la raíz de publicación es `dist`.
  - **Servidor propio (Apache/Nginx):** copiá todo lo que hay en `dist/` a la carpeta que sirve el sitio (ej. `public_html` o `/var/www/html`).

Antes del build, creá un **`.env.production`** en la raíz (o definí las variables en el panel de tu hosting) con la URL real de tu API:

```env
VITE_API_URL=https://tu-dominio.com/api
```

O si el backend va en un subdominio:

```env
VITE_API_URL=https://api.tu-dominio.com/api
```

Volvé a hacer **`npm run build`** después de cambiar `.env.production` para que la URL quede fija en el build.

### 2. Backend (API Java en Tomcat)

- En el servidor donde corra Tomcat, desplegá el **WAR** que generás con el backend (`backend-tomcat` → Clean and Build → archivo `target/inmobiliaria-api.war`).
- Configurá **MySQL** en producción (variables de entorno o `context.xml`) y las variables que indica `backend-tomcat/README.md`:
  - **JWT_SECRET:** clave secreta para firmar los tokens (elegí una larga y aleatoria).
  - **FRONTEND_URL:** URL pública del frontend, ej. `https://tu-dominio.com` (para los enlaces de “olvidé mi contraseña”).
  - Si usás envío de emails (recuperar contraseña), configurá SMTP en el backend.
- La primera vez, un admin debe crearse desde **`https://tu-dominio.com/admin/setup`** (no hay usuario por defecto con MySQL).

### 3. Resumen para el usuario

- **Usuario de la página (público):** entra a tu dominio, ve el catálogo, detalle de propiedades y contacto. No necesita cuenta.
- **Administrador:** entra a **`/admin/login`**, inicia sesión (o crea la primera cuenta en `/admin/setup`), y usa el panel (propiedades, administradores, configuración) con su usuario y contraseña.

Con el frontend desplegado, el backend en marcha y la URL de la API bien configurada en el build, la app está lista para que la use bien el usuario en la red.

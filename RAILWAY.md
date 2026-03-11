# Desplegar en Railway (frontend + backend + MySQL)

## Lo que ya tenés

- **Frontend** desplegado desde el repo (Vue). URL pública ya generada.
- **Backend** listo para desplegar con el Dockerfile en `backend-tomcat/`.

## 1. Agregar MySQL al proyecto

1. En el proyecto de Railway, clic en **"+ Agregar"** (o **"New"**).
2. Elegí **"Database"** → **"Add MySQL"**.
3. Railway crea la base y te muestra variables como:
   - `MYSQL_URL` (cadena completa), o
   - `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`

## 2. Agregar el servicio del backend

1. De nuevo **"+ Agregar"** → **"Empty Service"** (o "GitHub Repo" si preferís reconectar).
2. Si usás el mismo repo:
   - **Settings** del nuevo servicio → **Source**:
     - **Root Directory:** `backend-tomcat`
   - **Settings** → **Build**:
     - **Builder:** Dockerfile (o "Dockerfile" si aparece la opción).
     - Dejá que use el Dockerfile que está en `backend-tomcat/`.
3. **Variables de entorno** del servicio backend:
   - El backend espera: `MYSQL_DATABASE`, `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_USER`, `MYSQL_PASSWORD`. Si el plugin MySQL de Railway usa otros nombres (ej. `MYSQLHOST`), creá igual las variables `MYSQL_*` con esos valores:
     - `MYSQL_DATABASE` = nombre de la base (en Railway suele ser `MYSQLDATABASE` o `railway`).
     - `MYSQL_HOST` = host (a veces `MYSQLHOST` en Railway).
     - `MYSQL_PORT` = `3306` o el valor de `MYSQLPORT`.
     - `MYSQL_USER` = usuario (a veces `MYSQLUSER`).
     - `MYSQL_PASSWORD` = contraseña (a veces `MYSQLPASSWORD`).
   - Agregar también:
     - `JWT_SECRET` = una frase o string largo y aleatorio (para firmar los tokens de login).
4. **Networking** del backend:
   - **Generate Domain** y anotá la URL (ej. `https://inmobiliariapropiedadesestefan-backend-production.up.railway.app`).
5. La API queda en: **`https://TU-BACKEND-URL/api`** (sin barra final o con `/api` según cómo lo uses en el front).

## 3. Conectar el frontend al backend

1. Entrá al **servicio del frontend** (el que ya estaba).
2. **Variables** → agregar:
   - **Nombre:** `VITE_API_URL`
   - **Valor:** la URL del backend + `/api`, por ejemplo:  
     `https://inmobiliariapropiedadesestefan-backend-production.up.railway.app/api`
3. Guardar y hacer un **nuevo deploy** del frontend (Redeploy o push a `main`), porque `VITE_API_URL` se usa en tiempo de build.

## 4. Probar

- **Sitio público:** tu URL del frontend (ej. `https://inmobiliariapropiedadesestefan-production.up.railway.app`).
- **Admin:** `https://TU-FRONT-URL/admin/login` o `/admin/setup` si es la primera vez (crear cuenta ahí).

## Resumen de URLs

| Qué        | Dónde |
|-----------|--------|
| Frontend  | URL del servicio Vue (ya la tenés). |
| API       | URL del servicio backend + `/api` (ej. `https://xxx.up.railway.app/api`). |
| `VITE_API_URL` | Esa misma URL de la API (con `/api` al final). |

Si el backend no arranca, revisá los **logs** del servicio backend en Railway (errores de MySQL, variables mal nombradas, etc.).

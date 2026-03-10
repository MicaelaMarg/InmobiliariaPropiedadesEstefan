# Checklist: por qué no funciona y qué hacer

Si "está todo" pero sigue sin funcionar, seguí estos pasos **en orden**. En cada uno, comprobá antes de pasar al siguiente.

---

## 1. Backend en NetBeans (que la API responda)

- [ ] En NetBeans tenés abierto **solo** el proyecto **inmobiliaria-api** (carpeta `backend-tomcat`).
- [ ] En **Properties** → **Run** del proyecto, el **Server** está en **Apache Tomcat** (no "Embedded" ni "Internal").
- [ ] **Clean and Build**: clic derecho en el proyecto → **Clean and Build**. Esperá a que diga BUILD SUCCESS.
- [ ] **Run**: clic derecho en el proyecto → **Run** (o F6). Dejá que arranque Tomcat.
- [ ] En la pestaña **Output** de NetBeans **no** debería aparecer en rojo "No se pudo inicializar la base de datos" ni un stack trace. Si aparece algo en rojo, copiá el mensaje.
- [ ] En el **navegador** abrí: **http://localhost:8080/inmobiliaria-api/api/properties**
  - Si ves **JSON** (por ejemplo `[]` o una lista): el backend **funciona**. Pasá al punto 2.
  - Si ves **404**: la app no se desplegó. Volvé a **Clean and Build** y **Run**. Si sigue 404, borrá la carpeta de la base de datos (ver README, "Si recibís 404", punto 4) y volvé a Run.

---

## 2. Frontend (que la web use el backend)

- [ ] El archivo **.env** está en la **raíz** del proyecto (carpeta `inmobiliaria`, al mismo nivel que `package.json`). Debe contener:
  ```
  VITE_API_URL=http://localhost:8080/inmobiliaria-api/api
  ```
- [ ] El frontend se levanta **después** de que el backend esté corriendo. En una **terminal** (fuera de NetBeans):
  ```bash
  cd /Users/micaelamargaritamattiucci/Documents/inmobiliaria
  npm run dev
  ```
- [ ] En el navegador abrí **http://localhost:5173** (no el 8080).
- [ ] Para probar que guarda: entrá a **Admin** (login: admin@inmobiliaria.com / admin123), creá o editá una propiedad y guardá. Si no da error y ves la propiedad en el listado, está usando el backend.

---

## 3. Si sigue sin funcionar

- **404 en http://localhost:8080/inmobiliaria-api/api/properties**  
  La aplicación Java no está arrancando en Tomcat. Revisá la pestaña **Output** en NetBeans al dar Run: el primer error en rojo suele decir la causa (base de datos, falta una clase, etc.). Borrar la carpeta **inmobiliaria-db** (donde Tomcat guarda la base H2) y volver a Run a veces lo soluciona.

- **La web carga pero "no guarda" / sigue con datos viejos**  
  El frontend no está usando el backend. Comprobá que existe el **.env** en la raíz con `VITE_API_URL=http://localhost:8080/inmobiliaria-api/api` y que reiniciaste `npm run dev` después de crear o cambiar el .env.

- **"POST .../api/admin/properties 404"**  
  Mismo problema que el primer 404: la app no está desplegada o no arrancó. Solución: Clean and Build, Run, y si hace falta borrar la base (inmobiliaria-db) y volver a Run.

---

## Resumen rápido

1. NetBeans: abrir **backend-tomcat** → **Clean and Build** → **Run**.
2. Navegador: **http://localhost:8080/inmobiliaria-api/api/properties** → tiene que devolver JSON.
3. Terminal: `npm run dev` en la carpeta **inmobiliaria**.
4. Navegador: **http://localhost:5173** → ahí usás la web; lo que el admin sube se guarda en el backend.

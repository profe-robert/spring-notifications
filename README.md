```markdown
# 📋 Proyecto Notificaciones

> Spring Boot + HATEOAS + CSR — Proyecto educativo

---

## 🖥️ Cliente (CSR)

El navegador renderiza la interfaz. El frontend consume la API REST del backend.

**React / Angular / Vue** → `localhost:5173`

---

## ⚙️ Servidor (Spring Boot)

### Capas

| Capa | Responsabilidad |
|------|----------------|
| **🛡️ Controller** | Recibe peticiones HTTP, define endpoints REST, ensambla HATEOAS |
| **💼 Service** | Lógica de negocio: obtener, guardar, marcar como leída |
| **💾 Repository** | Extiende `JpaRepository`, JPA genera el SQL automáticamente |

### Infraestructura

| Componente | Función |
|------------|---------|
| **🔄 DataLoader** | Genera 50 registros falsos con DataFaker (`@Profile dev`) |
| **🔗 HATEOAS** | `EntityModel` para un recurso, `CollectionModel` para colecciones |
| **📖 Swagger** | Documentación automática de la API |
| **🧪 Tests** | `@WebMvcTest` + MockMvc + Mockito |

---

## 🗄️ Base de Datos

**Tabla: `notificaciones`**

| Campo | Tipo | Ejemplo |
|-------|------|---------|
| id | Long (PK) | 1 |
| mensaje | String | "Tiene una nueva solicitud..." |
| perfil_receptor | String | "Operador AGE" |
| fecha_plazo | Date | 2025-02-15 |
| leida | Boolean | false |

---

## 🔗 HATEOAS en acción

### Recurso individual — `GET /api/v1/notificaciones/1`

```json
{
  "id": 1,
  "mensaje": "Tiene una nueva solicitud...",
  "leida": false,
  "_links": {
    "self": { "href": "/notificaciones/1" },
    "marcar-leida": { "href": "/notificaciones/1/leer" }
  }
}
```

> ⚡ **Dinámico:** Si `leida = true`, el enlace `marcar-leida` desaparece.

### Colección — `GET /api/v1/notificaciones`

```json
{
  "_embedded": {
    "notificacionList": [ "..." ]
  },
  "_links": {
    "self": { "href": "/notificaciones" }
  }
}
```

---

## 📊 Endpoints

| Método | Ruta | Respuesta |
|--------|------|-----------|
| GET | `/notificaciones` | 200 OK ó 204 No Content |
| GET | `/notificaciones/{id}` | 200 OK ó 404 Not Found |
| POST | `/notificaciones` | 201 Created + Location |
| PUT | `/notificaciones/{id}/leer` | 200 OK |

---

## 🧪 Estrategia de Tests

**Nivel controlador** — `@WebMvcTest`

- **MockMvc** simula peticiones HTTP sin servidor real
- **@MockBean** reemplaza el Service por un doble
- Se prueban: códigos HTTP, estructura JSON, enlaces HATEOAS
- NO se prueba: lógica de negocio ni acceso a BD

### Patrón AAAV

```
ARRANGE → Configurar mocks con when()
ACT     → Ejecutar mockMvc.perform()
ASSERT  → Verificar respuesta con .andExpect()
VERIFY  → Verificar interacciones con verify()
```

---

## 📦 Dependencias clave

- **spring-boot-starter-data-jpa** → JPA + Hibernate
- **spring-boot-starter-web** → Controller REST
- **spring-boot-starter-hateoas** → EntityModel, CollectionModel
- **springdoc-openapi** → Swagger UI
- **lombok** → @Data, @Builder, @RequiredArgsConstructor
- **datafaker** → Datos falsos para DataLoader
- **spring-boot-starter-test** → MockMvc, JUnit 5
- **mockito-core** → Mocks para tests
```

---

> Puedes copiar esto directamente en tu `README.md` de GitHub y se renderizará correctamente.

# spring-notifications
API REST con Spring Boot para gestionar notificaciones por perfil de usuario. Implementa HATEOAS con enlaces dinámicos, arquitectura CSR y un DataLoader con DataFaker que genera 50 registros de prueba. Incluye pruebas unitarias con MockMvc y Mockito. 

╔══════════════════════════════════════════════════════════════════════════════╗
║                     📋 PROYECTO NOTIFICACIONES                             ║
║                       Spring Boot + HATEOAS + CSR                          ║
╚══════════════════════════════════════════════════════════════════════════════╝



┌─────────────────────────────────────────────────────────────────────────────┐
│  🖥️  CLIENTE  (CSR - Client Side Rendering)                                │
│                                                                             │
│     React / Angular / Vue                                                   │
│     localhost:5173                                                          │
│                                                                             │
│     El navegador se encarga de renderizar la UI.                           │
│     El frontend consume la API REST del backend.                           │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 │  HTTP / JSON
                                 │  GET  /api/v1/notificaciones
                                 │  POST /api/v1/notificaciones
                                 │  GET  /api/v1/notificaciones/{id}
                                 │  PUT  /api/v1/notificaciones/{id}/leer
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  ⚙️  SERVIDOR  (Spring Boot)                                               │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────┐     │
│  │  🛡️ CONTROLLER  (NotificacionController)                         │     │
│  │                                                                   │     │
│  │  • Recibe peticiones HTTP                                         │     │
│  │  • Define endpoints REST                                          │     │
│  │  • Ensambla modelos HATEOAS                                       │     │
│  │  • Delega la lógica al Service                                    │     │
│  └────────────────────────────┬──────────────────────────────────────┘     │
│                               │                                             │
│                               ▼                                             │
│  ┌───────────────────────────────────────────────────────────────────┐     │
│  │  💼 SERVICE  (NotificacionService)                                │     │
│  │                                                                   │     │
│  │  • Lógica de negocio                                              │     │
│  │  • Obtener todas / por ID                                        │     │
│  │  • Guardar notificación                                           │     │
│  │  • Marcar como leída                                              │     │
│  └────────────────────────────┬──────────────────────────────────────┘     │
│                               │                                             │
│                               ▼                                             │
│  ┌───────────────────────────────────────────────────────────────────┐     │
│  │  💾 REPOSITORY  (NotificacionRepository)                          │     │
│  │                                                                   │     │
│  │  • Extiende JpaRepository                                        │     │
│  │  • findAll(), findById(), save(), deleteAll()                    │     │
│  │  • JPA se encarga del SQL automáticamente                        │     │
│  └────────────────────────────┬──────────────────────────────────────┘     │
│                               │                                             │
│  ┌────────────────────────────┴──────────────────────────────────────┐     │
│  │  🔧 INFRAESTRUCTURA                                               │     │
│  │                                                                   │     │
│  │  🔄 DataLoader    → Genera 50 registros falsos (@Profile dev)    │     │
│  │  🔗 HATEOAS       → EntityModel (1 recurso)                      │     │
│  │                    CollectionModel (lista de recursos)            │     │
│  │  📖 Swagger       → Documentación automática de la API           │     │
│  │  🧪 Tests         → @WebMvcTest + MockMvc + Mockito              │     │
│  └───────────────────────────────────────────────────────────────────┘     │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  🗄️  BASE DE DATOS                                                         │
│                                                                             │
│     ┌─────────────────────────────────────────────────────┐                │
│     │  TABLA: notificaciones                               │                │
│     ├──────────┬───────────────────┬───────────┬─────────┤                │
│     │  id 🔑   │  mensaje          │ perfil    │ leida   │                │
│     ├──────────┼───────────────────┼───────────┼─────────┤                │
│     │  1       │  Tiene una nueva..│ Operador  │ false   │                │
│     │  2       │  Documento apr... │ Registrad │ true    │                │
│     │  ...     │  ...              │ ...       │ ...     │                │
│     │  50      │  Revisión pend... │ Analista  │ false   │                │
│     └──────────┴───────────────────┴───────────┴─────────┘                │
│                                                                             │
│     + fecha_plazo (DATE)                                                    │
└─────────────────────────────────────────────────────────────────────────────┘



┌─────────────────────────────────────────────────────────────────────────────┐
│  🔗 ¿CÓMO FUNCIONA HATEOAS EN ESTE PROYECTO?                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Recurso individual (GET /api/v1/notificaciones/1)                         │
│  ┌─────────────────────────────────────────────────────────┐               │
│  │  {                                                       │               │
│  │    "id": 1,                                              │               │
│  │    "mensaje": "Tiene una nueva solicitud...",             │               │
│  │    "leida": false,                                       │               │
│  │    "_links": {                                           │               │
│  │      "self":         → GET  /notificaciones/1            │               │
│  │      "marcar-leida": → PUT  /notificaciones/1/leer  ✅   │               │
│  │    }                                                    │               │
│  │  }                                                      │               │
│  └─────────────────────────────────────────────────────────┘               │
│                                                                             │
│  ⚡ DINÁMICO: Si leida = true → el link "marcar-leida" DESAPARECE          │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Colección (GET /api/v1/notificaciones)                                    │
│  ┌─────────────────────────────────────────────────────────┐               │
│  │  {                                                       │               │
│  │    "_embedded": {                                        │               │
│  │      "notificacionList": [ ... ]                         │               │
│  │    },                                                   │               │
│  │    "_links": {                                           │               │
│  │      "self": → GET /notificaciones  (la colección)      │               │
│  │    }                                                    │               │
│  │  }                                                      │               │
│  └─────────────────────────────────────────────────────────┘               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘



┌─────────────────────────────────────────────────────────────────────────────┐
│  📊 ENDPOINTS DE LA API                                                     │
├──────────────┬────────────────┬─────────────────────────────────────────────┤
│  MÉTODO      │  RUTA          │  DESCRIPCIÓN                                 │
├──────────────┼────────────────┼─────────────────────────────────────────────┤
│  GET         │  /notificaciones         │  Lista todas (200 o 204)           │
│  GET         │  /notificaciones/{id}    │  Busca por ID (200 o 404)          │
│  POST        │  /notificaciones         │  Crea nueva (201 + Location)       │
│  PUT         │  /notificaciones/{id}/leer│  Marca como leída (200)           │
└──────────────┴────────────────┴─────────────────────────────────────────────┘



┌─────────────────────────────────────────────────────────────────────────────┐
│  🧪 ESTRATEGIA DE TESTS                                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Nivel Controlador (@WebMvcTest)                                           │
│  ├── MockMvc simula peticiones HTTP sin servidor real                      │
│  ├── @MockBean reemplaza el Service por un doble                           │
│  ├── Se prueban: códigos HTTP, estructura JSON, enlaces HATEOAS            │
│  └── NO se prueba: lógica de negocio ni acceso a BD                       │
│                                                                             │
│  Patrón ARRANGE - ACT - ASSERT - VERIFY                                    │
│  ├── ARRANGE → Configurar mocks con when()                                │
│  ├── ACT     → Ejecutar mockMvc.perform()                                 │
│  ├── ASSERT  → Verificar respuesta con .andExpect()                       │
│  └── VERIFY  → Verificar interacciones con verify()                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘



┌─────────────────────────────────────────────────────────────────────────────┐
│  📦 DEPENDENCIAS CLAVE                                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  spring-boot-starter-data-jpa   → JPA + Hibernate                         │
│  spring-boot-starter-web        → Controller REST                          │
│  spring-boot-starter-hateoas    → EntityModel, CollectionModel             │
│  springdoc-openapi              → Swagger UI                               │
│  lombok                         → @Data, @Builder, @RequiredArgsConstructor│
│  datafaker                      → Datos falsos para DataLoader             │
│  spring-boot-starter-test       → MockMvc, JUnit 5                         │
│  mockito-core                   → Mocks para tests                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

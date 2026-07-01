package com.example.notificaciones.controller;

import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas a nivel de Controlador para NotificacionController.
 * 
 * ¿Qué se prueba aquí?
 * - Que los endpoints retornan los códigos HTTP correctos
 * - Que la estructura del JSON response es la esperada
 * - Que los enlaces HATEOAS están presentes
 * 
 * ¿Qué NO se prueba aquí?
 * - Lógica de negocio (eso va en ServiceTest)
 * - Acceso a datos (eso va en RepositoryTest)
 * 
 * @WebMvcTest: Carga solo la capa web (controller, filtros, interceptores)
 *              NO carga toda la aplicación, es rápido y aislado.
 */
@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    // Permite simular peticiones HTTP sin levantar un servidor real
    @Autowired
    private MockMvc mockMvc;

    // Convierte objetos Java a JSON y viceversa (igual que en la app real)
    @Autowired
    private ObjectMapper objectMapper;

    // Reemplaza el servicio real por un "doble de prueba"
    // Spring lo inyecta automáticamente en el controller
    @MockBean
    private NotificacionService service;

    // ========================================================================
    // DATOS DE PRUEBA REUTILIZABLES
    // ========================================================================

    private Notificacion crearNotificacionNoLeida() {
        return Notificacion.builder()
                .id(1L)
                .mensaje("Tiene una nueva solicitud pendiente de revisión")
                .perfilReceptor("Operador AGE")
                .fechaPlazo(LocalDate.of(2025, 2, 15))
                .leida(false)
                .build();
    }

    private Notificacion crearNotificacionLeida() {
        return Notificacion.builder()
                .id(2L)
                .mensaje("Documento aprobado correctamente")
                .perfilReceptor("Registrador")
                .fechaPlazo(LocalDate.of(2025, 3, 1))
                .leida(true)
                .build();
    }

    // ========================================================================
    // GET /api/v1/notificaciones - Obtener todas
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/notificaciones - Obtener todas las notificaciones")
    class ObtenerTodasTest {

        @Test
        @DisplayName("Debería retornar 200 OK con lista de notificaciones y enlaces HATEOAS")
        void deberiaRetornar200ConLista() throws Exception {
            // ARRANGE: Preparamos los datos que el servicio mockeará
            List<Notificacion> notificaciones = List.of(
                    crearNotificacionNoLeida(),
                    crearNotificacionLeida()
            );
            when(service.obtenerTodas()).thenReturn(notificaciones);

            // ACT & ASSERT: Ejecutamos la petición y verificamos la respuesta
            mockMvc.perform(get("/api/v1/notificaciones")
                            .accept(MediaTypes.HAL_JSON_VALUE))
                    .andExpect(status().isOk())                          // 200 OK
                    .andExpect(jsonPath("$._embedded.notificacionList").exists())  // Lista presente
                    .andExpect(jsonPath("$._embedded.notificacionList.length()").value(2)) // 2 elementos
                    .andExpect(jsonPath("$._links.self").exists())       // Enlace self de la colección
                    .andExpect(jsonPath("$._embedded.notificacionList[0]._links.self").exists()) // HATEOAS individual
                    .andExpect(jsonPath("$._embedded.notificacionList[0]._links.marcar-leida").exists()); // Link dinámico (no leída)

            // VERIFY: Verificamos que el servicio fue llamado exactamente 1 vez
            verify(service, times(1)).obtenerTodas();
        }

        @Test
        @DisplayName("Debería retornar 204 No Content cuando no hay notificaciones")
        void deberiaRetornar204CuandoListaVacia() throws Exception {
            // ARRANGE: Retornamos lista vacía
            when(service.obtenerTodas()).thenReturn(Collections.emptyList());

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/notificaciones")
                            .accept(MediaTypes.HAL_JSON_VALUE))
                    .andExpect(status().isNoContent());  // 204 No Content

            verify(service, times(1)).obtenerTodas();
        }
    }

    // ========================================================================
    // GET /api/v1/notificaciones/{id} - Obtener por ID
    // ========================================================================

    @Nested
    @DisplayName("GET /api/v1/notificaciones/{id} - Obtener notificación por ID")
    class ObtenerPorIdTest {

        @Test
        @DisplayName("Debería retornar 200 OK con la notificación encontrada")
        void deberiaRetornar200CuandoExiste() throws Exception {
            // ARRANGE
            Notificacion notificacion = crearNotificacionNoLeida();
            when(service.obtenerPorId(1L)).thenReturn(Optional.of(notificacion));

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/notificaciones/1")
                            .accept(MediaTypes.HAL_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.mensaje").value("Tiene una nueva solicitud pendiente de revisión"))
                    .andExpect(jsonPath("$.perfilReceptor").value("Operador AGE"))
                    .andExpect(jsonPath("$.leida").value(false))
                    .andExpect(jsonPath("$._links.self").exists())
                    .andExpect(jsonPath("$._links.marcar-leida").exists()); // Porque no está leída

            verify(service, times(1)).obtenerPorId(1L);
        }

        @Test
        @DisplayName("Debería retornar 404 Not Found cuando la notificación no existe")
        void deberiaRetornar404CuandoNoExiste() throws Exception {
            // ARRANGE
            when(service.obtenerPorId(999L)).thenReturn(Optional.empty());

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/notificaciones/999")
                            .accept(MediaTypes.HAL_JSON_VALUE))
                    .andExpect(status().isNotFound());  // 404 Not Found

            verify(service, times(1)).obtenerPorId(999L);
        }

        @Test
        @DisplayName("Debería NO incluir enlace 'marcar-leida' cuando la notificación ya fue leída")
        void noDeberiaIncluirLinkMarcarLeidaCuandoYaFueLeida() throws Exception {
            // ARRANGE
            Notificacion notificacionLeida = crearNotificacionLeida();
            when(service.obtenerPorId(2L)).thenReturn(Optional.of(notificacionLeida));

            // ACT & ASSERT
            mockMvc.perform(get("/api/v1/notificaciones/2")
                            .accept(MediaTypes.HAL_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._links.self").exists())
                    .andExpect(jsonPath("$._links.marcar-leida").doesNotExist()); // NO debe existir

            verify(service, times(1)).obtenerPorId(2L);
        }
    }

    // ========================================================================
    // POST /api/v1/notificaciones - Crear notificación
    // ========================================================================

    @Nested
    @DisplayName("POST /api/v1/notificaciones - Crear nueva notificación")
    class CrearTest {

        @Test
        @DisplayName("Debería retornar 201 Created al crear una nueva notificación")
        void deberiaRetornar201AlCrearNuevaNotificacion() throws Exception {
            // ARRANGE: Preparamos la notificación a enviar en el body
            Notificacion nueva = Notificacion.builder()
                    .mensaje("Nueva notificación de prueba")
                    .perfilReceptor("Supervisor")
                    .fechaPlazo(LocalDate.of(2025, 4, 10))
                    .leida(false)
                    .build();

            // Simulamos que el servicio guarda y le asigna un ID
            Notificacion guardada = Notificacion.builder()
                    .id(10L)
                    .mensaje("Nueva notificación de prueba")
                    .perfilReceptor("Supervisor")
                    .fechaPlazo(LocalDate.of(2025, 4, 10))
                    .leida(false)
                    .build();

            when(service.guardar(any(Notificacion.class))).thenReturn(guardada);

            // ACT & ASSERT: Ejecutamos el POST y verificamos solo el estado 201
            mockMvc.perform(post("/api/v1/notificaciones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nueva)))
                    .andExpect(status().isCreated());

            // VERIFY: Confirmamos que el servicio fue llamado
            verify(service, times(1)).guardar(any(Notificacion.class));
        }
    }

    // ========================================================================
    // PUT /api/v1/notificaciones/{id}/leer - Marcar como leída
    // ========================================================================

    @Nested
    @DisplayName("PUT /api/v1/notificaciones/{id}/leer - Marcar como leída")
    class MarcarComoLeidaTest {

        @Test
        @DisplayName("Debería retornar 200 OK con la notificación marcada como leída")
        void deberiaRetornar200ConNotificacionLeida() throws Exception {
            // ARRANGE
            Notificacion leida = Notificacion.builder()
                    .id(1L)
                    .mensaje("Tiene una nueva solicitud pendiente de revisión")
                    .perfilReceptor("Operador AGE")
                    .fechaPlazo(LocalDate.of(2025, 2, 15))
                    .leida(true)  // Ahora está leída
                    .build();

            when(service.marcarComoLeida(1L)).thenReturn(leida);

            // ACT & ASSERT
            mockMvc.perform(put("/api/v1/notificaciones/1/leer")
                            .accept(MediaTypes.HAL_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.leida").value(true))
                    .andExpect(jsonPath("$._links.self").exists())
                    .andExpect(jsonPath("$._links.marcar-leida").doesNotExist()); // Ya no debe aparecer

            verify(service, times(1)).marcarComoLeida(1L);
        }
    }
}
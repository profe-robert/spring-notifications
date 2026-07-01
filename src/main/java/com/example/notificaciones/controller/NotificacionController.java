package com.example.notificaciones.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.service.NotificacionService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "API para la gestión de notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear nueva notificación")
    public ResponseEntity<EntityModel<Notificacion>> crear(@RequestBody Notificacion notificacion) {
        Notificacion guardada = service.guardar(notificacion);
        return ResponseEntity.created(linkTo(methodOn(NotificacionController.class).obtenerPorId(guardada.getId())).toUri())
                .body(ensamblarModelo(guardada));
    }
    
    @GetMapping
    @Operation(
        summary = "Obtener todas las notificaciones con enlaces HATEOAS",
        description = "Retorna una colección de notificaciones. Cada notificación incluye sus propios enlaces HATEOAS, y la colección incluye un enlace 'self' hacia sí misma."
    )
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> obtenerTodas() {

        // 1. Obtenemos la lista plana de notificaciones desde el servicio
        List<Notificacion> notificaciones = service.obtenerTodas();

        // 2. Convertimos cada Notificacion en un EntityModel<Notificacion>
        //    Esto es necesario porque HATEOAS requiere que CADA recurso
        //    individual lleve sus propios enlaces (self, update, delete, etc.)
        //    Usamos el mismo método ensamblarModelo() que ya tienes en tu controller
        List<EntityModel<Notificacion>> notificacionesModel = notificaciones.stream()
                .map(this::ensamblarModelo)
                .collect(Collectors.toList());

        // 3. Si la lista está vacía, respondemos con 204 No Content
        //    - 404 Not Found NO es correcto aquí porque el SÍ existe, simplemente está vacío
        //    - 204 indica "la petición fue exitosa pero no hay contenido que devolver"
        if (notificacionesModel.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // 4. Envolver la lista en un CollectionModel
        //    CollectionModel es el equivalente HATEOAS para colecciones:
        //    - Contiene la lista de EntityModel individuales
        //    - Agrega un enlace "self" que apunta a la colección misma (GET /notificaciones)
        CollectionModel<EntityModel<Notificacion>> collectionModel = CollectionModel.of(
                notificacionesModel,
                // Este enlace permite al cliente descubrir cómo volver a consultar esta colección
                linkTo(methodOn(NotificacionController.class).obtenerTodas()).withSelfRel()
        );

        // 5. Retornamos 200 OK con el CollectionModel completo
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una notificación por ID con enlaces HATEOAS")
    public ResponseEntity<EntityModel<Notificacion>> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(notificacion -> ResponseEntity.ok(ensamblarModelo(notificacion)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/leer")
    @Operation(summary = "Cambiar el estado de la notificación a 'leída'")
    public ResponseEntity<EntityModel<Notificacion>> marcarComoLeida(@PathVariable Long id) {
        Notificacion actualizada = service.marcarComoLeida(id);
        return ResponseEntity.ok(ensamblarModelo(actualizada));
    }

    // Método auxiliar para construir el HATEOAS - Reemplaza Carpeta Assemblers
    private EntityModel<Notificacion> ensamblarModelo(Notificacion notificacion) {
        EntityModel<Notificacion> modelo = EntityModel.of(notificacion,
                linkTo(methodOn(NotificacionController.class).obtenerPorId(notificacion.getId())).withSelfRel()
        );

        // HATEOAS Dinámico: Cambio de estado
        if (!notificacion.isLeida()) {
            modelo.add(linkTo(methodOn(NotificacionController.class).marcarComoLeida(notificacion.getId())).withRel("marcar-leida"));
        }

        return modelo;
    }
}
package com.example.notificaciones.assemblers;

import org.springframework.hateoas.EntityModel;

import com.example.notificaciones.controller.NotificacionController;
import com.example.notificaciones.model.Notificacion;

public class NotificacionAssembler {
    // Método auxiliar para construir el HATEOAS
    private EntityModel<Notificacion> ensamblarNotificacion(Notificacion notificacion) {
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

package com.example.notificaciones.service;

import org.springframework.stereotype.Service;

import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.repository.NotificacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacionService {

    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    public Notificacion guardar(Notificacion notificacion) {
        return repository.save(notificacion);
    }

    public Optional<Notificacion> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public List<Notificacion> obtenerTodas() {
        return repository.findAll();
    }

    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.setLeida(true);
        return repository.save(notificacion);
    }
}

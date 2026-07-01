package com.example.notificaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.notificaciones.model.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {}
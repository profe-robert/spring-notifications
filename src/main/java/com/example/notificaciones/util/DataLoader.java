package com.example.notificaciones.util;

import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Profile("test")
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final NotificacionRepository notificacionRepository;

    @Override
    public void run(String... args) throws Exception {

        Faker faker = new Faker();

        String[] perfiles = {
            "Operador",
            "Registrador",
            "Administrador",
            "Supervisor",
            "Analista"
        };

        // Limpiar datos existentes en cada ejecución
        notificacionRepository.deleteAll();

        for (int i = 0; i < 50; i++) {

            Notificacion notificacion = Notificacion.builder()
                .mensaje(faker.lorem().sentence(10, 20))
                .perfilReceptor(perfiles[ThreadLocalRandom.current().nextInt(perfiles.length)])
                .fechaPlazo(generateRandomDate())
                .leida(faker.random().nextBoolean())
                .build();

            notificacionRepository.save(notificacion);

        }

    }

    private LocalDate generateRandomDate() {
        long minDay = LocalDate.now().plusDays(1).toEpochDay();
        long maxDay = LocalDate.now().plusDays(90).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }
}
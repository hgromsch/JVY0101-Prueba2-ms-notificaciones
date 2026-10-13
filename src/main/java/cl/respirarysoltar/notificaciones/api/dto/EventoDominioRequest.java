package cl.respirarysoltar.notificaciones.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Evento de dominio publicado por otro microservicio. En AWS este payload llega
 * desde la cola SQS (agenda-notificaciones-queue / talleres-notificaciones-queue);
 * localmente se expone por REST para simular el evento en la demostración.
 */
public record EventoDominioRequest(
        @NotBlank(message = "El nombre del evento es obligatorio")
        @Size(max = 60)
        String evento,

        @NotBlank(message = "El destinatario es obligatorio")
        @Size(max = 160)
        String destinatario,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 500)
        String mensaje
) {
}

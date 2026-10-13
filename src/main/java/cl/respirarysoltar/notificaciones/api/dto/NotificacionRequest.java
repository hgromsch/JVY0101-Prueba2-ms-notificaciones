package cl.respirarysoltar.notificaciones.api.dto;

import cl.respirarysoltar.notificaciones.domain.CanalNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos de entrada para enviar una notificación directa (RF-10). */
public record NotificacionRequest(
        @NotBlank(message = "El destinatario es obligatorio")
        @Size(max = 160)
        String destinatario,

        @NotNull(message = "El canal es obligatorio (EMAIL, SMS o PUSH)")
        CanalNotificacion canal,

        @NotBlank(message = "El tipo es obligatorio")
        @Size(max = 60)
        String tipo,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 500)
        String mensaje
) {
}

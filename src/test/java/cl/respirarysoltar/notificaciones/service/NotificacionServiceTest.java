package cl.respirarysoltar.notificaciones.service;

import cl.respirarysoltar.notificaciones.api.dto.EventoDominioRequest;
import cl.respirarysoltar.notificaciones.api.dto.NotificacionRequest;
import cl.respirarysoltar.notificaciones.domain.CanalNotificacion;
import cl.respirarysoltar.notificaciones.domain.EstadoNotificacion;
import cl.respirarysoltar.notificaciones.domain.Notificacion;
import cl.respirarysoltar.notificaciones.exception.ReglaNegocioException;
import cl.respirarysoltar.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    @InjectMocks
    private NotificacionService service;

    @Test
    void enviarDejaLaNotificacionComoEnviada() {
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion creada = service.enviar(new NotificacionRequest(
                "paciente@correo.cl", CanalNotificacion.EMAIL, "cita_confirmada", "Su cita quedó agendada"));

        assertEquals(EstadoNotificacion.ENVIADA, creada.getEstado());
        assertNotNull(creada.getEnviadaEn());
    }

    @Test
    void procesarEventoCreaNotificacionPorEmail() {
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion creada = service.procesarEvento(new EventoDominioRequest(
                "inscripcion_confirmada", "paciente@correo.cl", "Se confirmó su cupo en el taller"));

        assertEquals(CanalNotificacion.EMAIL, creada.getCanal());
        assertEquals("inscripcion_confirmada", creada.getTipo());
    }

    @Test
    void reenviarUnaNotificacionYaEnviadaLanzaConflicto() {
        Notificacion notificacion = new Notificacion("a@correo.cl", CanalNotificacion.EMAIL, "tipo", "mensaje");
        notificacion.marcarEnviada();
        when(repository.findById(any())).thenReturn(Optional.of(notificacion));

        assertThrows(ReglaNegocioException.class, () -> service.reenviar(UUID.randomUUID()));
    }
}

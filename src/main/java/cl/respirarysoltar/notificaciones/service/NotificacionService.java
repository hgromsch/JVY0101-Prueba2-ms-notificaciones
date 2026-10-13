package cl.respirarysoltar.notificaciones.service;

import cl.respirarysoltar.notificaciones.api.dto.EventoDominioRequest;
import cl.respirarysoltar.notificaciones.api.dto.NotificacionRequest;
import cl.respirarysoltar.notificaciones.domain.CanalNotificacion;
import cl.respirarysoltar.notificaciones.domain.EstadoNotificacion;
import cl.respirarysoltar.notificaciones.domain.Notificacion;
import cl.respirarysoltar.notificaciones.exception.RecursoNoEncontradoException;
import cl.respirarysoltar.notificaciones.exception.ReglaNegocioException;
import cl.respirarysoltar.notificaciones.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Envío y trazabilidad de notificaciones (RF-10). Servicio stateless respecto del
 * resto del sistema: en AWS se gatilla consumiendo eventos de SQS y despacha por
 * SES (email) / SNS (SMS/push). Localmente el "envío" se simula con un log y queda
 * persistido para la demostración.
 */
@Service
@Transactional
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    /** Crea y despacha una notificación directa. */
    public Notificacion enviar(NotificacionRequest request) {
        Notificacion notificacion = new Notificacion(request.destinatario(), request.canal(),
                request.tipo(), request.mensaje());
        return despachar(notificacion);
    }

    /**
     * Punto de entrada de eventos de dominio (cita_confirmada, inscripcion_confirmada, etc.).
     * En AWS este método lo invoca un listener de SQS; aquí se expone por REST para la demo.
     */
    public Notificacion procesarEvento(EventoDominioRequest evento) {
        Notificacion notificacion = new Notificacion(evento.destinatario(), CanalNotificacion.EMAIL,
                evento.evento(), evento.mensaje());
        return despachar(notificacion);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listar(String destinatario, EstadoNotificacion estado) {
        if (destinatario != null && !destinatario.isBlank()) {
            return repository.findByDestinatario(destinatario);
        }
        if (estado != null) {
            return repository.findByEstado(estado);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Notificacion obtener(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada: " + id));
    }

    /** Reintenta el envío de una notificación pendiente o fallida. */
    public Notificacion reenviar(UUID id) {
        Notificacion notificacion = obtener(id);
        if (notificacion.getEstado() == EstadoNotificacion.ENVIADA) {
            throw new ReglaNegocioException("La notificación ya fue enviada");
        }
        return despachar(notificacion);
    }

    public void eliminar(UUID id) {
        repository.delete(obtener(id));
    }

    /** Simula el despacho por el canal configurado (SES/SNS en AWS). */
    private Notificacion despachar(Notificacion notificacion) {
        try {
            // TODO (AWS): enviar por SES (EMAIL) o SNS (SMS/PUSH) según el canal.
            log.info("Enviando notificación [{}] por {} a {}", notificacion.getTipo(),
                    notificacion.getCanal(), notificacion.getDestinatario());
            notificacion.marcarEnviada();
        } catch (RuntimeException e) {
            notificacion.marcarFallida();
        }
        return repository.save(notificacion);
    }
}

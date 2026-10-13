package cl.respirarysoltar.notificaciones.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notificación enviada a un usuario de la plataforma (RF-10): confirmaciones de
 * cita/taller y recordatorios. En AWS se gatilla consumiendo eventos de SQS y se
 * despacha por SES/SNS; localmente se expone vía REST para la demostración.
 */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue
    private UUID id;

    /** Email o teléfono del destinatario. */
    @Column(nullable = false, length = 160)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalNotificacion canal;

    /** Tipo del evento origen: cita_confirmada, cita_cancelada, recordatorio, etc. */
    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoNotificacion estado;

    private LocalDateTime enviadaEn;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    protected Notificacion() {
        // Constructor requerido por JPA
    }

    public Notificacion(String destinatario, CanalNotificacion canal, String tipo, String mensaje) {
        this.destinatario = destinatario;
        this.canal = canal;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
    }

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    public void marcarEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
        this.enviadaEn = LocalDateTime.now();
    }

    public void marcarFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }

    public UUID getId() {
        return id;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public LocalDateTime getEnviadaEn() {
        return enviadaEn;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}

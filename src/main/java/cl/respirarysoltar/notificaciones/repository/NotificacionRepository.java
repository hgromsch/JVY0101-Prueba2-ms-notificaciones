package cl.respirarysoltar.notificaciones.repository;

import cl.respirarysoltar.notificaciones.domain.EstadoNotificacion;
import cl.respirarysoltar.notificaciones.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

    List<Notificacion> findByDestinatario(String destinatario);

    List<Notificacion> findByEstado(EstadoNotificacion estado);
}

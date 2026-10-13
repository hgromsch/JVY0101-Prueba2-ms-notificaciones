package cl.respirarysoltar.notificaciones.api;

import cl.respirarysoltar.notificaciones.api.dto.EventoDominioRequest;
import cl.respirarysoltar.notificaciones.api.dto.NotificacionRequest;
import cl.respirarysoltar.notificaciones.domain.EstadoNotificacion;
import cl.respirarysoltar.notificaciones.domain.Notificacion;
import cl.respirarysoltar.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * API REST de notificaciones (RF-10). El endpoint /eventos simula localmente la
 * llegada de un evento de dominio que en AWS consume desde SQS.
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Notificacion> enviar(@Valid @RequestBody NotificacionRequest request) {
        Notificacion creada = service.enviar(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** Simula el evento de dominio que en AWS llega por la cola SQS del servicio. */
    @PostMapping("/eventos")
    public ResponseEntity<Notificacion> recibirEvento(@Valid @RequestBody EventoDominioRequest request) {
        Notificacion creada = service.procesarEvento(request);
        return ResponseEntity.accepted().body(creada);
    }

    @GetMapping
    public List<Notificacion> listar(@RequestParam(required = false) String destinatario,
                                     @RequestParam(required = false) EstadoNotificacion estado) {
        return service.listar(destinatario, estado);
    }

    @GetMapping("/{id}")
    public Notificacion obtener(@PathVariable UUID id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/reenviar")
    public Notificacion reenviar(@PathVariable UUID id) {
        return service.reenviar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

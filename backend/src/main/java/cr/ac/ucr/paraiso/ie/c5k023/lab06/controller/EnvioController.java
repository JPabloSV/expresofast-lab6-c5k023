package cr.ac.ucr.paraiso.ie.c5k023.lab06.controller;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public List<EnvioResponseDTO> obtenerOptimizados() {
        return envioService.obtenerEnviosOptimizados();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrar(@Valid @RequestBody EnvioRequestDTO request) {
        EnvioResponseDTO creado = envioService.registrarEnvio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstado(@PathVariable Integer id,
                                                             @Valid @RequestBody CambioEstadoDTO cambio) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, cambio));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<EnvioResponseDTO> cancelar(@PathVariable Integer id,
                                                     @RequestBody(required = false) CambioEstadoDTO cambio) {
        String observaciones = (cambio != null) ? cambio.getObservaciones() : null;
        return ResponseEntity.ok(envioService.cancelarEnvio(id, observaciones));
    }

    @GetMapping("/{id}/bitacora")
    public List<BitacoraResponseDTO> obtenerBitacora(@PathVariable Integer id) {
        return envioService.obtenerBitacora(id);
    }
}

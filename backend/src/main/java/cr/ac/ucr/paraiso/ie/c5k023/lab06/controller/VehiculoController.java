package cr.ac.ucr.paraiso.ie.c5k023.lab06.controller;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.business.VehiculoService;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Vehiculo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public List<Vehiculo> listar() {
        return vehiculoService.listar();
    }

    @GetMapping("/{id}")
    public Vehiculo obtener(@PathVariable Integer id) {
        return vehiculoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Vehiculo> crear(@RequestBody Vehiculo vehiculo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.registrarVehiculo(vehiculo));
    }

    @PutMapping("/{id}")
    public Vehiculo actualizar(@PathVariable Integer id, @RequestBody Vehiculo cambios) {
        return vehiculoService.actualizar(id, cambios);
    }

    @PatchMapping("/{id}/conductor/{conductorId}")
    public Vehiculo asignarConductor(@PathVariable Integer id, @PathVariable Integer conductorId) {
        return vehiculoService.asignarConductor(id, conductorId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

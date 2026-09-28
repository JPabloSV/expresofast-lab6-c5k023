package cr.ac.ucr.paraiso.ie.c5k023.lab06.controller;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.dto.EnvioDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/envios")
@CrossOrigin(origins = "*")
public class EnvioPaginadoController {

    private final EnvioService envioService;

    public EnvioPaginadoController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public Page<EnvioDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) {
        return envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado);
    }

    @GetMapping("/procedimiento/{estado}")
    public List<EnvioDTO> listarPorProcedimiento(@PathVariable String estado) {
        return envioService.listarViaStoredProcedure(estado);
    }
}
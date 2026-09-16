package cr.ac.ucr.paraiso.ie.c5k023.lab06.business;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5k023.lab06.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio para la gestion de empresas logisticas asociadas
 * a la plataforma ExpresoFast.
 */
@Service
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository empresaRepository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaLogistica> listar() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EmpresaLogistica obtenerPorId(Integer id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id " + id));
    }

    /**
     * Registra una empresa validando que la cedula juridica sea unica y
     * asignando la fecha de registro cuando no viene informada.
     *
     * @throws DuplicateResourceException si la cedula juridica ya existe
     */
    @Transactional
    public EmpresaLogistica registrarEmpresa(EmpresaLogistica empresa) {
        if (empresaRepository.existsByCedulaJuridica(empresa.getCedulaJuridica())) {
            throw new DuplicateResourceException(
                    "Ya existe una empresa registrada con la cedula juridica "
                            + empresa.getCedulaJuridica());
        }

        if (empresa.getFechaRegistro() == null) {
            empresa.setFechaRegistro(LocalDateTime.now());
        }

        return empresaRepository.save(empresa);
    }

    @Transactional
    public EmpresaLogistica actualizar(Integer id, EmpresaLogistica cambios) {
        EmpresaLogistica empresa = obtenerPorId(id);
        empresa.setNombre(cambios.getNombre());
        empresa.setTelefono(cambios.getTelefono());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa no encontrada con id " + id);
        }
        empresaRepository.deleteById(id);
    }
}

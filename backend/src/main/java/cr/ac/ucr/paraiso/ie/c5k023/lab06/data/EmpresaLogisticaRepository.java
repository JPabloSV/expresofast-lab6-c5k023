package cr.ac.ucr.paraiso.ie.c5k023.lab06.data;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.EmpresaLogistica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaLogisticaRepository extends JpaRepository<EmpresaLogistica, Integer> {
}
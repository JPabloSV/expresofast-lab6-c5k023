package cr.ac.ucr.paraiso.ie.c5k023.lab06.data;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    /** Permite validar placas duplicadas antes de registrar un vehiculo. */
    boolean existsByPlaca(String placa);
}

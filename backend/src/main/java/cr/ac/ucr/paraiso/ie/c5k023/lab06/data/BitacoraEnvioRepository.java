package cr.ac.ucr.paraiso.ie.c5k023.lab06.data;

import cr.ac.ucr.paraiso.ie.c5k023.lab06.domain.BitacoraEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {
    List<BitacoraEnvio> findByEnvioIdOrderByFechaCambioDesc(Integer envioId);
}
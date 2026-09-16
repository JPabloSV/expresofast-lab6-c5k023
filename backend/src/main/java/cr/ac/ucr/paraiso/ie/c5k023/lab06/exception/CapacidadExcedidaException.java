package cr.ac.ucr.paraiso.ie.c5k023.lab06.exception;

/**
 * Se lanza cuando el peso de un envio supera la capacidad maxima
 * permitida del vehiculo al que se intenta asignar.
 */
public class CapacidadExcedidaException extends RuntimeException {
    public CapacidadExcedidaException(String message) {
        super(message);
    }
}

package cr.ac.ucr.paraiso.ie.c5k023.lab06.exception;

/**
 * Se lanza cuando se intenta registrar un recurso cuyo identificador
 * unico de negocio ya existe (placa de vehiculo, cedula juridica, etc.).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

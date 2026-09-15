package cr.ac.ucr.paraiso.ie.c5k023.lab06.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
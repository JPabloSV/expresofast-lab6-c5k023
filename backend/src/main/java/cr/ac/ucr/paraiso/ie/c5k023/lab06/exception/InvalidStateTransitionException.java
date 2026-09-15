package cr.ac.ucr.paraiso.ie.c5k023.lab06.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
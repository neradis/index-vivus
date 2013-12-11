package de.fusionfactory.index_vivus.persistence;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class ORMError extends RuntimeException {

    public ORMError(String message) {
        super(message);
    }

    public ORMError(String message, Throwable cause) {
        super(message, cause);
    }
}

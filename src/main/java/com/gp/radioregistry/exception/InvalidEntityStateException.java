package com.gp.radioregistry.exception;

/**
 * Thrown when an entity is in an invalid state that violates a business/database
 * invariant (e.g. an XOR constraint) before it is persisted.
 */
public class InvalidEntityStateException extends RuntimeException {
    public InvalidEntityStateException(String message) {
        super(message);
    }
}


package com.bitlab.main_service.exception;

/**
 * An IllegalArgumentException thrown when an entity with the requested id does not exist. Mapped to HTTP 404.
 */
public class ResourceNotFoundException extends IllegalArgumentException {

    public ResourceNotFoundException(String entityName, Long id) {
        super(entityName + " with id " + id + " not found");
    }
}
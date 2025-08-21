package dev.twme.blocket.exceptions;

/**
 * Chunk processing exception
 * Thrown when an error occurs during chunk data processing
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkProcessingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor
     *
     * @param message Exception message
     */
    public ChunkProcessingException(String message) {
        super(message);
    }
    
    /**
     * Constructor
     *
     * @param message Exception message
     * @param cause Cause exception
     */
    public ChunkProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor
     *
     * @param cause Cause exception
     */
    public ChunkProcessingException(Throwable cause) {
        super(cause);
    }
}
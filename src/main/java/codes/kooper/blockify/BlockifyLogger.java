package codes.kooper.blockify;

/**
 * Logger interface for the Blockify library.
 * This allows the library to be used with different logging implementations.
 */
public interface BlockifyLogger {
    
    /**
     * Log an info message.
     * 
     * @param message The message to log
     */
    void info(String message);
    
    /**
     * Log a warning message.
     * 
     * @param message The message to log
     */
    void warning(String message);
    
    /**
     * Log an error message.
     * 
     * @param message The message to log
     */
    void error(String message);
    
    /**
     * Log a debug message.
     * 
     * @param message The message to log
     */
    void debug(String message);
}
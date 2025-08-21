package dev.twme.blocket.exceptions;

/**
 * 區塊處理異常
 * 當區塊數據處理過程中發生錯誤時拋出此異常
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkProcessingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 建構子
     * 
     * @param message 異常訊息
     */
    public ChunkProcessingException(String message) {
        super(message);
    }
    
    /**
     * 建構子
     * 
     * @param message 異常訊息
     * @param cause 原因異常
     */
    public ChunkProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 建構子
     * 
     * @param cause 原因異常
     */
    public ChunkProcessingException(Throwable cause) {
        super(cause);
    }
}
package org.seratic.enterprise.tgestiona.web.filter;
/**
 * Excepion lanzada al momento de hacer el proceso de validacion del JWT
 * @author VICTORAL
 */
public class NotAuthorizedException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public NotAuthorizedException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public NotAuthorizedException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public NotAuthorizedException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }


}


package org.seratic.enterprise.tgestiona.web.filter;

import javax.servlet.http.HttpServletRequest;

/**
 * Declaracion de interface con el metodo para el control de acceso haciendo uso de JWT 
 * @author VICTORAL
 */
public interface  SecurityGuard {
    HttpServletRequest isAuthorized(HttpServletRequest request)throws NotAuthorizedException;
}

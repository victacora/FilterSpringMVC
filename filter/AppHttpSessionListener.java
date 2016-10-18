/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seratic.enterprise.tgestiona.web.filter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author VICTORAL
 */
public class AppHttpSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
//No hacer nada
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Log log = LogFactory.getLog("Aplicacion");
        HttpSession session = se.getSession();
        long now = new java.util.Date().getTime();
        boolean timeout = (now - session.getLastAccessedTime()) >= ((long) session.getMaxInactiveInterval() * 1000L);
        if (timeout) {
            long duration = new Date().getTime() - session.getCreationTime();
            long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
            SimpleDateFormat formatFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            log.info("Sesiones-> Finalizacion automatica de sesion");
            log.info("Sesiones-> Id Sesion: " + session.getId());
            log.info("Sesiones-> Fecha creacion sesion: " + formatFechaHora.format(new Date(session.getCreationTime())));
            log.info("Sesiones-> Tiempo conexion sesion, " + diffInHours + " Horas " + diffInMinutes + " Minutos " + diffInSeconds + " Segundos.");
            log.info("Sesiones-> Fecha ultima peticion: " + formatFechaHora.format(new Date(session.getLastAccessedTime())));
            log.info("Sesiones-> Fecha sesion timeout: " + formatFechaHora.format(new Date(session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000)));
        }
    }
}

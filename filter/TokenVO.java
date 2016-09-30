/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seratic.enterprise.tgestiona.web.vo;

/**
 *
 * @author VICTORAL
 */
public class TokenVO {
    
    private String token;
    private long tiempoRenovacion;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTiempoRenovacion() {
        return tiempoRenovacion;
    }

    public void setTiempoRenovacion(long tiempoRenovacion) {
        this.tiempoRenovacion = tiempoRenovacion;
    }
    
}

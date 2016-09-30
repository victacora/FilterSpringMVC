package org.seratic.enterprise.tgestiona.web.vo;

import java.util.List;

public class UsuarioAutenticacionVO {

    private int codigo;
    private String nombre;
    private List<Byte> idPermisos;
    private String nomPais;
    private String codDistribuidora;
    private String nomDistribuidora;
    private List<String> codDistribuidoras;
    private short codPerfil;
    private String perfil;
    private String token;
    private long tiempoRenovacion;

    public long getTiempoRenovacion() {
        return tiempoRenovacion;
    }

    public void setTiempoRenovacion(long tiempoRenovacion) {
        this.tiempoRenovacion = tiempoRenovacion;
    }
    
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Byte> getIdPermisos() {
        return idPermisos;
    }

    public void setIdPermisos(List<Byte> idPermisos) {
        this.idPermisos = idPermisos;
    }

    public String getNomPais() {
        return nomPais;
    }

    public void setNomPais(String nomPais) {
        this.nomPais = nomPais;
    }

    public String getCodDistribuidora() {
        return codDistribuidora;
    }

    public void setCodDistribuidora(String codDistribuidora) {
        this.codDistribuidora = codDistribuidora;
    }

    public String getNomDistribuidora() {
        return nomDistribuidora;
    }

    public void setNomDistribuidora(String nomDistribuidora) {
        this.nomDistribuidora = nomDistribuidora;
    }

    public List<String> getCodDistribuidoras() {
        return codDistribuidoras;
    }

    public void setCodDistribuidoras(List<String> codDistribuidoras) {
        this.codDistribuidoras = codDistribuidoras;
    }

    public short getCodPerfil() {
        return codPerfil;
    }

    public void setCodPerfil(short codPerfil) {
        this.codPerfil = codPerfil;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

}

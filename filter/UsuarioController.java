package org.seratic.enterprise.tgestiona.web.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seratic.enterprise.tgestiona.constantes.Constantes;
import org.seratic.enterprise.tgestiona.web.facade.UsuarioFacade;
import org.seratic.enterprise.tgestiona.web.vo.CambiarContrasenaVO;
import org.seratic.enterprise.tgestiona.web.vo.IdNombreAsignadoAlmacenUsuarioVO;
import org.seratic.enterprise.tgestiona.web.vo.IdNombreAsignadoNegocioUsuarioVO;
import org.seratic.enterprise.tgestiona.web.vo.IdNombreAsignadoTransportistaUsuarioVO;
import org.seratic.enterprise.tgestiona.web.vo.TokenVO;
import org.seratic.enterprise.tgestiona.web.vo.UsuarioAutenticacionVO;
import org.seratic.enterprise.tgestiona.web.vo.UsuarioVO;
import org.seratic.util.ErrorWebException;
import org.seratic.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/usuario")
public class UsuarioController {

    private static final Log log = LogFactory.getLog("Aplicacion");

    @Autowired
    private UsuarioFacade usuarioFacade;

    @RequestMapping(value = "/filtrar")
    public @ResponseBody
    Map<String, Object> filtrar(@RequestParam int start, @RequestParam int limit, @RequestParam(required = false) String codPais, @RequestParam(required = false) String filtro) {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.countUsuario(codPais, StringUtil.split(filtro)), usuarioFacade.filtrar(start, limit, codPais, StringUtil.split(filtro)));
        } catch (Exception e) {
            log.error("filtrar fallo", e);
            return ExtJSReturn.mapError("Error filtrando los usuarios");
        }
    }

    @RequestMapping(value = "/errorAutenticacion")
    public @ResponseBody
    Map<String, Object> errorAutenticacion() {
        return ExtJSReturn.mapError("Error al realizar la petición, el token para validar la sesión no es válido o ha vencido.", 666);
    }

    @RequestMapping(value = "/listar")
    public @ResponseBody
    Map<String, Object> listar(@RequestParam(required = false) String filtro) {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.listar(StringUtil.split(filtro)));
        } catch (Exception e) {
            log.error("litar fallo", e);
            return ExtJSReturn.mapError("Error listando los usuarios");
        }
    }

    @RequestMapping(value = "/crear")
    public @ResponseBody
    Map<String, Object> crear(@RequestBody List usuarios, int idUsuarioAuditoria) throws Exception {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.crear(ExtJSContentRequest.mapObjects(usuarios, UsuarioVO.class), idUsuarioAuditoria));
        } catch (Exception e) {
            log.error("crear usuario falló.", e);
            return ExtJSReturn.mapError("Error creando el usuario");
        }
    }

    @RequestMapping(value = "actualizar")
    public @ResponseBody
    Map<String, Object> update(@RequestBody List usuarios, @RequestParam int idUsuarioAuditoria) throws Exception {
        try {
            usuarioFacade.actualizar(ExtJSContentRequest.mapObjects(usuarios, UsuarioVO.class), idUsuarioAuditoria);
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.error("actualizar usuario falló", e);
            return ExtJSReturn.mapError("Error actualizando el usuario");
        }
    }

    @RequestMapping(value = "/cambiarContrasena")
    public @ResponseBody
    Map<String, Object> cambiarContrasena(@RequestBody List usuarios) throws Exception {
        try {
            List id = usuarioFacade.CambiarContrasena(ExtJSContentRequest.mapObjects(usuarios, CambiarContrasenaVO.class));
            return ExtJSReturn.mapOK(id);
        } catch (Exception e) {
            log.error("cambiarContrasena falló.", e);
            return ExtJSReturn.mapError("Error cambiarContrasena ");
        }
    }

    @RequestMapping(value = "borrar")
    public @ResponseBody
    Map<String, Object> delete(@RequestBody List usuarios) throws Exception {
        try {
            usuarioFacade.borrar(ExtJSContentRequest.mapAtributes(usuarios, UsuarioVO.class, "id"));
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.error("borrar usuario falló", e);
            return ExtJSReturn.mapError("Error eliminando el usuario");
        }
    }

    @RequestMapping(value = "/validar")
    public @ResponseBody
    Map<String, Object> validar(@RequestParam String usuario, @RequestParam String clave, HttpServletRequest httpServletRequest) {
        try {
            UsuarioAutenticacionVO u = usuarioFacade.validar(usuario, clave);
            if (u != null && Constantes.VALIDAR_TOKEN == 1) {
                HttpSession httpSession = httpServletRequest.getSession();
                httpSession.setAttribute("usuario", u);
                log.info("Sesiones-> Sesion creada correctamente.");
                log.info("Sesiones-> Id Sesion: " + httpSession.getId());
            }
            return ExtJSReturn.mapOK(u);
        } catch (ErrorWebException e) {
            return ExtJSReturn.mapError(e.getMessage(), 10);
        } catch (Exception e) {
            log.error("validar fallo", e);
            return ExtJSReturn.mapError("Error validando usuario");
        }
    }

    /**
     *
     * @param idUsuario
     * @return
     */
    @RequestMapping(value = "/renovarToken")
    public @ResponseBody
    Map<String, Object> renovarToken(HttpServletRequest httpServletRequest) {
        try {
            SimpleDateFormat formatFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String codigo = (String) httpServletRequest.getAttribute("idUsuario");
            int idUsuario = codigo != null && !codigo.equals("") ? Integer.parseInt(codigo) : -1;
            HttpSession session = httpServletRequest.getSession();
            UsuarioAutenticacionVO u = (UsuarioAutenticacionVO) session.getAttribute("usuario");
            if (u != null && idUsuario != -1 && u.getCodigo() == idUsuario) {
                TokenVO token = new TokenVO();
                String tokenNuevo = Jwts.builder().setSubject(String.valueOf(idUsuario)).signWith(SignatureAlgorithm.HS256, Constantes.KEY).setIssuedAt(new Date()).setExpiration(new Date(Calendar.getInstance().getTimeInMillis() + Constantes.TIEMPO_EXPIRACION_TOKEN)).compact();
                token.setToken("Bearer " + tokenNuevo);
                token.setTiempoRenovacion(Constantes.TIEMPO_RENOVACION_TOKEN);
                log.info("Sesiones-> El usuario, Codigo: " + u.getCodigo() + ", Nombre: " + u.getNombre() + " ha renovado el token.");
                log.info("Sesiones-> Fecha renovacion token: " + formatFechaHora.format(new Date(Calendar.getInstance().getTimeInMillis() + Constantes.TIEMPO_RENOVACION_TOKEN)));
                log.info("Sesiones-> Fecha vencimiento token: " + formatFechaHora.format(new Date(Calendar.getInstance().getTimeInMillis() + Constantes.TIEMPO_EXPIRACION_TOKEN)));
                log.info("Sesiones-> Id Sesion: " + session.getId());
                log.info("Sesiones-> Fecha ultima peticion: " + formatFechaHora.format(new Date(session.getLastAccessedTime())));
                log.info("Sesiones-> Fecha sesion timeout: " + formatFechaHora.format(new Date(session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000)));
                return ExtJSReturn.mapOK(token);
            } else {
                log.info("Sesiones-> Error al renovar token, no se pudo validar la sesion correctamente.");
                log.info("Sesiones-> Id Sesion: " + session.getId());
                log.info("Sesiones-> Id Usuario token: " + idUsuario);
                log.info("Sesiones-> Datos usuario sesion: " + (u == null ? "NULL" : u.getCodigo()));
                log.info("Sesiones-> Fecha ultima peticion: " + formatFechaHora.format(new Date(session.getLastAccessedTime())));
                log.info("Sesiones-> Fecha sesion timeout: " + formatFechaHora.format(new Date(session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000)));
                return ExtJSReturn.mapError("Error al renovar token, no se pudo validar la sesion correctamente.", 666);
            }
        } catch (Exception e) {
            log.info("Sesiones-> Error al renovar token, no se pudo validar la sesion correctamente.");
            log.info("Sesiones-> Sesion: NULL ");
            log.info("Sesiones-> renovarToken fallo", e);
            return ExtJSReturn.mapError("Error al renovar token, no se pudo validar la sesion correctamente.", 666);
        }
    }

    /**
     *
     * @param idUsuario
     * @return
     */
    @RequestMapping(value = "/cerrarSesion")
    public @ResponseBody
    Map<String, Object> cerrarSesion(@RequestParam int idUsuario, HttpServletRequest httpServletRequest) {
        try {
            SimpleDateFormat formatFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            if (Constantes.VALIDAR_TOKEN == 1) {
                HttpSession session = httpServletRequest.getSession(false);
                if (session != null) {
                    UsuarioAutenticacionVO u = (UsuarioAutenticacionVO) session.getAttribute("usuario");
                    if (u != null && u.getCodigo() == idUsuario) {
                        long duration = new Date().getTime() - session.getCreationTime();
                        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
                        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
                        log.info("Sesiones-> El usuario, Codigo: " + u.getCodigo() + ", Nombre: " + u.getNombre() + " ha cerrado sesion");
                        log.info("Sesiones-> Id Sesion: " + session.getId());
                        log.info("Sesiones-> Fecha creacion sesion: " + formatFechaHora.format(new Date(session.getCreationTime())));
                        log.info("Sesiones-> Tiempo conexion sesion, " + diffInHours + " Horas " + diffInMinutes + " Minutos " + diffInSeconds + " Segundos.");
                        log.info("Sesiones-> Fecha ultima peticion: " + formatFechaHora.format(new Date(session.getLastAccessedTime())));
                        log.info("Sesiones-> Fecha sesion timeout: " + formatFechaHora.format(new Date(session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000)));
                        session.invalidate();
                        return ExtJSReturn.mapOK();
                    } else {
                        log.info("Sesiones-> Error al al cerrar sesion.");
                        log.info("Sesiones-> Id Sesion: " + session.getId());
                        log.info("Sesiones-> Id Usuario token: " + idUsuario);
                        log.info("Sesiones-> Datos usuario sesion: " + (u == null ? "NULL" : u.getCodigo()));
                        return ExtJSReturn.mapError("Error al al cerrar sesion.", 666);
                    }
                }
                log.info("Sesiones-> Error al al cerrar sesion.");
                log.info("Sesiones-> Sesion NULL");
                return ExtJSReturn.mapError("Error al cerrar sesion.", 666);
            }
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.info("Sesiones-> Error al al cerrar sesion.");
            log.error("Sesiones-> cerrarSesion fallo", e);
            return ExtJSReturn.mapError("Error al cerrar sesion.", 666);
        }
    }

    @RequestMapping(value = "/filtrarDistribuidoras")
    public @ResponseBody
    Map<String, Object> filtrarDistribuidoras(@RequestParam String codPais, @RequestParam int idUsuario) {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.filtrarDistribuidoras(codPais, idUsuario));
        } catch (Exception e) {
            log.error("filtrarDistribuidoras fallo", e);
            return ExtJSReturn.mapError("Error filtrando las distribuidoras para un usuario");
        }
    }

    @RequestMapping(value = "/filtrarPermisosNegocios")
    public @ResponseBody
    Map<String, Object> filtrarPermisosNegocios(@RequestParam int start, @RequestParam int limit, @RequestParam int idUsuario) {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.countPermisosNegocio(idUsuario), usuarioFacade.filtrarPermisosNegocio(start, limit, idUsuario));
        } catch (Exception e) {
            log.error("filtrarPermisosNegocios fallo", e);
            return ExtJSReturn.mapError("Error filtrando las permisos de negocios para un usuario");
        }
    }

    @RequestMapping(value = "/filtrarPermisosAlmacen")
    public @ResponseBody
    Map<String, Object> filtrarPermisosAlmacen(@RequestParam int start, @RequestParam int limit, @RequestParam int idUsuario) {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.countPermisosAlmacen(idUsuario), usuarioFacade.filtrarPermisosAlmacen(start, limit, idUsuario));
        } catch (Exception e) {
            log.error("filtrarPermisosAlmacen fallo", e);
            return ExtJSReturn.mapError("Error filtrando las permisos de almacenes para un usuario");
        }
    }

    @RequestMapping(value = "/filtrarPermisosTransportista")
    public @ResponseBody
    Map<String, Object> filtrarPermisosTransportista(@RequestParam int start, @RequestParam int limit, @RequestParam int idUsuario) {
        try {
            return ExtJSReturn.mapOK(usuarioFacade.countPermisosTransportista(idUsuario), usuarioFacade.filtrarPermisosTransportista(start, limit, idUsuario));
        } catch (Exception e) {
            log.error("filtrarPermisosTransportista fallo", e);
            return ExtJSReturn.mapError("Error filtrando las permisos de transportista para un usuario");
        }
    }

    @RequestMapping(value = "/asignarPermisoNegocio")
    public @ResponseBody
    Map<String, Object> asignarPermisoNegocio(@RequestParam int idUsuario, @RequestBody List equiposAsignados, @RequestParam int idUsuarioAuditoria) {
        try {
            usuarioFacade.asignarPermisoNegocio(idUsuario, ExtJSContentRequest.mapObjects(equiposAsignados, IdNombreAsignadoNegocioUsuarioVO.class), idUsuarioAuditoria);
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.error("asignarToPerfil fallo", e);
            return ExtJSReturn.mapError("Error inesperado al asignar un permiso a un perfil.");
        }
    }

    @RequestMapping(value = "/asignarPermisoTransportista")
    public @ResponseBody
    Map<String, Object> asignarPermisoTransportista(@RequestParam int idUsuario, @RequestBody List equiposAsignados, @RequestParam int idUsuarioAuditoria) {
        try {
            usuarioFacade.asignarPermisoTransportista(idUsuario, ExtJSContentRequest.mapObjects(equiposAsignados, IdNombreAsignadoTransportistaUsuarioVO.class), idUsuarioAuditoria);
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.error("asignarToPerfil fallo", e);
            return ExtJSReturn.mapError("Error inesperado al asignar un permiso a un perfil.");
        }
    }

    @RequestMapping(value = "/asignarPermisoAlmacen")
    public @ResponseBody
    Map<String, Object> asignarPermisoAlmacen(@RequestParam int idUsuario, @RequestBody List equiposAsignados, @RequestParam int idUsuarioAuditoria) {
        try {
            usuarioFacade.asignarPermisoAlmacen(idUsuario, ExtJSContentRequest.mapObjects(equiposAsignados, IdNombreAsignadoAlmacenUsuarioVO.class), idUsuarioAuditoria);
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.error("asignarToPerfil fallo", e);
            return ExtJSReturn.mapError("Error inesperado al asignar un permiso a un perfil.");
        }
    }

    @RequestMapping(value = "/asignarAllPermisos")
    public @ResponseBody
    Map<String, Object> asignarAllPermiso(@RequestParam int idUsuario, @RequestParam int tipo, @RequestParam boolean asignado, @RequestParam int idUsuarioAuditoria) {
        try {
            usuarioFacade.asignarAllPermisoUsuarios(idUsuario, tipo, asignado, idUsuarioAuditoria);
            return ExtJSReturn.mapOK();
        } catch (Exception e) {
            log.error("asignar permisos fallo", e);
            return ExtJSReturn.mapError("Error inesperado al asignar un permiso.");
        }
    }
}

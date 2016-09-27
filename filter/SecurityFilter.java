package org.seratic.enterprise.tgestiona.web.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seratic.enterprise.tgestiona.constantes.Constantes;

/**
 * Implementacion de filtro para control de acceso haciendo uso de JWT
 *
 * @author VICTORAL
 */
public class SecurityFilter implements Filter {

    Log log = LogFactory.getLog("Aplicacion");

    private SecurityGuard methodGetPostGuard;
    private SecurityGuard methodMultiPartGuard;

    @Override
    public void destroy() {
        //
    }

    /**
     * Metodo sobrecargado que filtra todas las peticiones
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
        } else {
            chain.doFilter(request, response);
        }

    }

    /**
     * Este metodo realiza el control de acceso verificando el JWT enviado por
     * el cliente. El flujo normal que deberia seguir toda peticion entrante es
     * el siguiente: Primero se determina el tipo de peticion, esto es imporante
     * porque al momento de obtener el token que ha sido enviado dentro de los
     * parametros de la peticion, no se obtienen de la misma manera cuando el
     * tipo de peticion es MultiPart. Una vez determinada como se debe manejar
     * la peticion, se procede a obtener el token JWT. Si el token esta presente
     * se determina si es un token valido, si lo es la peticion es atendida
     * normalmente. De lo contrario puede presentarse las siguientes
     * situaciones: 1) El token JWT no esta presente como cuando se estan
     * descargando archivos y recursos, como: imagenes, documentos html,js, css,
     * etc. o cuando se autentica un usuario en la aplicacion. 2) El token JWT
     * es invalido o ha expirado. Con cualquiera de estas situaciones se lanza
     * una excepcion. Si la peticion se encuntra dentro de las excepciones
     * configuradas en verificarRutasExcluida, se atiende la peticion, en caos
     * contrario dependiendo del tipo de peticion si es una llamado AJAX se
     * retornar un objeto JSON que contien un codigo de error y un mensaje, o se
     * redirecciona al inciar sesion. Dependiendo
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    protected void doFilter(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        //se obtiene la URL para controlar que debe ser excluido del proceso de validacion
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI().substring(contextPath.length());

        try {
            //Aqui se definie como debe ser manejada la peticion entrante. Debido a que existen un manejo 
            // diferente para poder obtener los 
            SecurityGuard guard = getGuard(request);
            try {
                httpRequest = guard.isAuthorized(request);
            } catch (NotAuthorizedException e) {
                if (verificarRutasExcluida(requestURI)) {
                    httpResponse.sendRedirect(isAjaxRequest(httpRequest) ? contextPath + "/usuario/errorAutenticacion.action" : contextPath);
                }
            }
            chain.doFilter(httpRequest, httpResponse);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.methodGetPostGuard = new GetPostSecurityGuard();
        this.methodMultiPartGuard = new MultiPartMimeSecurityGuard();
    }

    /**
     * Determina si la peticion es de tipo MultipartContent o no, y retorna un
     * objeto en cargado de controlar la atencion de la peticion.
     *
     * @param request
     * @return
     */
    private SecurityGuard getGuard(HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            return this.methodMultiPartGuard;
        } else {
            return this.methodGetPostGuard;
        }

    }

    /**
     * Esta clase implementa el control de acceso por JWT, cuando el tipo de la
     * peticion no es MultiPart. Los parametros son obtenidos normalmente del
     * objeto request por medio del metodo getParameter.
     */
    class GetPostSecurityGuard implements SecurityGuard {

        @Override
        public HttpServletRequest isAuthorized(HttpServletRequest request) throws NotAuthorizedException {
            //obtencion del token JWT enviado dentro de los parametros de la peticion
            String token = request.getParameter("token") != null ? request.getParameter("token").substring(7) : "";
            try {
                //verificacion del token JWT
                final Claims claims = Jwts.parser().setSigningKey(Constantes.KEY)
                        .parseClaimsJws(token).getBody();
                request.setAttribute("claims", claims);
            } catch (ExpiredJwtException e) {
                throw new NotAuthorizedException("Error al validar token. El token ha expirado.");
            } catch (UnsupportedJwtException e) {
                throw new NotAuthorizedException("Error al validar token. Excepcion no controlada.");
            } catch (MalformedJwtException e) {
                throw new NotAuthorizedException("Error al validar token. Token mal formado.");
            } catch (SignatureException e) {
                throw new NotAuthorizedException("Error al validar token. No se encuntra debidamente firmado.");
            } catch (IllegalArgumentException e) {
                throw new NotAuthorizedException("Error al validar token. Argumentos invalidos.");
            }
            return request;

        }

    }

    /**
     * Esta clase implementa el control de acceso por JWT, cuando el tipo de la
     * peticion es MultiPart. Los parametros son obtenidos de una manera
     * diferente a cuando se hace otro tipo de llamados. Para poder obtener
     * estos se hace necesario el uso de la clase ServletFileUpload, la cual
     * realizada un parseo de la peticion obteniedo los archivos enviados, y los
     * parametros adicionales. Antes de ejecutar esta accion se debe crear una
     * copia del objeto HttpServletRequest request, debido a que una vez
     * ejecutada la peticion es modificada y genera inconvenenites al momento de
     * continuar con la atencion de esta. Para lograr esto se debe crear un
     * objeto de la clase InputStreamTeeRequestWrapper que recibe como parametro
     * la peticion, esta clase permite obtener la peticion como un array de
     * bytes, que posteriormente sera utilizado por MPRequestWrapper, clase
     * envolvente que toma las mismas propiedades de la peticion original.
     * Ejemplo de peticiones: Cuando se suben o se descargan archivos del
     * servidor.
     */
    class MultiPartMimeSecurityGuard implements SecurityGuard {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        public MultiPartMimeSecurityGuard() {
            super();
        }

        @Override
        public HttpServletRequest isAuthorized(HttpServletRequest request)
                throws NotAuthorizedException {
            InputStreamTeeRequestWrapper mpwrapper = new InputStreamTeeRequestWrapper(request);
            Map map = new HashMap();
            try {
                List items = upload.parseRequest(mpwrapper);
                for (Iterator itor = items.iterator(); itor.hasNext();) {
                    FileItem item = (FileItem) itor.next();
                    if (item.isFormField()) {
                        String name = item.getFieldName();
                        if (!map.containsKey(name)) {
                            String value = item.getString();
                            map.put(name, value);
                        }
                    }
                    item.delete();
                }

            } catch (FileUploadException e) {
                e.printStackTrace();
            }
               //obtencion del token JWT enviado dentro de los parametros de la peticion
            String token = map.get("token") != null ? map.get("token").toString().substring(7) : "";
            InputStream in = new ByteArrayInputStream(mpwrapper.toBytes());
            MPRequestWrapper wrapper = new MPRequestWrapper(request, new MyServletInputStream(in));
            try {
                //verificacion del token JWT
                final Claims claims = Jwts.parser().setSigningKey(Constantes.KEY)
                        .parseClaimsJws(token).getBody();
                wrapper.setAttribute("claims", claims);
            } catch (ExpiredJwtException e) {
                throw new NotAuthorizedException("Error al validar token. El token ha expirado.");
            } catch (UnsupportedJwtException e) {
                throw new NotAuthorizedException("Error al validar token. Excepcion no controlada.");
            } catch (MalformedJwtException e) {
                throw new NotAuthorizedException("Error al validar token. Token mal formado.");
            } catch (SignatureException e) {
                throw new NotAuthorizedException("Error al validar token. No se encuntra debidamente firmado.");
            } catch (IllegalArgumentException e) {
                throw new NotAuthorizedException("Error al validar token. Argumentos invalidos.");
            }

            return wrapper;
        }
    }

    /**
     * Verifica si la peticion es realizada por ajax.
     *
     * @param request
     * @return
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("x-requested-with");
        return header != null && header.equals("XMLHttpRequest");
    }

    /**
     * Este metodo permite excluir directorios, archivos o acciones de ser
     * validados, debido a la imposibilidad de configurar esta accion desde el
     * archivo web.xml en la configuracion de los filters. Que deberia ser
     * excluido: al cargar la aplicacion se deben traer todos los archivos,
     * como: imagenes, estilos, documentos html, etc. Tambien es importante
     * reclacar la importancia de excluir las acciones que autentican el
     * usuario, evento en el que se genera el token que es enviado hacia el
     * cliente y el cual este debe almacenar para posteriormente poder ejecutar
     * peticiones al servidor y estas puedan ser atendidas. Para el caso de las
     * peticiones que llegan por ajax, se debe implementar una accion que en
     * caso de ocurrir un error pueda retornar una respuesta en json y se de el
     * manejo adecuado.
     *
     * @param requestURI
     * @return
     */
    private boolean verificarRutasExcluida(String requestURI) {
        return !requestURI.equals("/")
                && !requestURI.contains("/js/")
                && !requestURI.contains("/css/")
                && !requestURI.contains("/images/")
                && !requestURI.contains("/modulos/")
                && !requestURI.contains("/res/")
                && !requestURI.contains("/stylesheets/")
                && !requestURI.contains("/ux/")
                && !requestURI.endsWith(".js")
                && !requestURI.endsWith(".css")
                && !requestURI.endsWith(".ico")
                && !requestURI.endsWith(".png")
                && !requestURI.endsWith(".jpg")
                && !requestURI.endsWith(".jpeg")
                && !requestURI.equals("/usuario/validar.action")
                && !requestURI.equals("/usuario/errorAutenticacion.action")
                && !requestURI.equals("/Comunicacion");
    }

}

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

public class SecurityFilter implements Filter {

    Log log = LogFactory.getLog(this.getClass());
    private SecurityGuard methodGetPostGuard;
    private SecurityGuard methodMultiPartGuard;

    @Override
    public void destroy() {
        //
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            doFilter((HttpServletRequest) request,
                    (HttpServletResponse) response, chain);
        } else {
            chain.doFilter(request, response);
        }

    }

    protected void doFilter(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        SecurityGuard guard = getGuard(request);

        try {
            httpRequest = guard.isAuthorized(request);
        } catch (NotAuthorizedException e) {
            if (verificarRutasExcluida(requestURI)) {
                httpResponse.sendRedirect(isAjaxRequest(httpRequest) ? "/sittlog/usuario/errorAutenticacion.action" : "/sittlog");
            }
        }
        chain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.methodGetPostGuard = new GetPostSecurityGuard();
        this.methodMultiPartGuard = new MultiPartMimeSecurityGuard();
    }

    private SecurityGuard getGuard(HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            return this.methodMultiPartGuard;
        } else {
            return this.methodGetPostGuard;
        }

    }

    class GetPostSecurityGuard implements SecurityGuard {

        @Override
        public HttpServletRequest isAuthorized(HttpServletRequest request) throws NotAuthorizedException {
            String token = request.getParameter("token") != null ? request.getParameter("token").substring(7) : "";
            try {
                final Claims claims = Jwts.parser().setSigningKey(Constantes.KEY)
                        .parseClaimsJws(token).getBody();
                  request.setAttribute("claims", claims);
            } catch (ExpiredJwtException e) {
                throw new NotAuthorizedException();
            } catch (UnsupportedJwtException e) {
                throw new NotAuthorizedException();
            } catch (MalformedJwtException e) {
                throw new NotAuthorizedException();
            } catch (SignatureException e) {
                throw new NotAuthorizedException();
            } catch (IllegalArgumentException e) {
                throw new NotAuthorizedException();
            }
            return request;

        }

    }

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

            String token = map.get("token") != null ? map.get("token").toString().substring(7) : "";
            InputStream in = new ByteArrayInputStream(mpwrapper.toBytes());
            MPRequestWrapper wrapper = new MPRequestWrapper(request, new MyServletInputStream(in));
            try {
                final Claims claims = Jwts.parser().setSigningKey(Constantes.KEY)
                        .parseClaimsJws(token).getBody();
                wrapper.setAttribute("claims", claims);
            } catch (ExpiredJwtException e) {
                throw new NotAuthorizedException();
            } catch (UnsupportedJwtException e) {
                throw new NotAuthorizedException();
            } catch (MalformedJwtException e) {
                throw new NotAuthorizedException();
            } catch (SignatureException e) {
                throw new NotAuthorizedException();
            } catch (IllegalArgumentException e) {
                throw new NotAuthorizedException();
            }
            
            
            return wrapper;
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("x-requested-with");
        return header != null && header.equals("XMLHttpRequest");
    }

    private boolean verificarRutasExcluida(String requestURI) {
        return !requestURI.equals("/sittlog/")
                && !requestURI.contains("/sittlog/js/")
                && !requestURI.contains("/sittlog/css/")
                && !requestURI.contains("/sittlog/images/")
                && !requestURI.contains("/sittlog/modulos/")
                && !requestURI.contains("/sittlog/res/")
                && !requestURI.contains("/sittlog/stylesheets/")
                && !requestURI.contains("/sittlog/ux/")
                && !requestURI.endsWith(".js")
                && !requestURI.endsWith(".css")
                && !requestURI.equals("/sittlog/usuario/validar.action")
                && !requestURI.equals("/sittlog/usuario/errorAutenticacion.action")
                && !requestURI.equals("/sittlog/Comunicacion");
    }

}

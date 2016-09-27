/**
 * 
 */
package org.seratic.enterprise.tgestiona.web.filter;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ReadListener;

import javax.servlet.ServletInputStream;

class MyServletInputStream extends ServletInputStream {
    private final InputStream in;

    /**
     * @param in
     */
    public MyServletInputStream(final InputStream in) {
        super();
        this.in = in;
    }

    public int read() throws IOException {
        return in.read();
    }

    @Override
    public boolean isFinished() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
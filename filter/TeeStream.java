/**
 *
 */
package org.seratic.enterprise.tgestiona.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ReadListener;

import javax.servlet.ServletInputStream;

class TeeStream extends ServletInputStream {

    private final ServletInputStream delegate;

    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    public byte[] toByteArray() {
        return bytes.toByteArray();
    }

    /**
     * @param delegate
     * @param wrapper TODO
     */
    public TeeStream(final ServletInputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        int r = delegate.read();
        if (r != -1) {
            bytes.write(r);
        }
        return r;
    }

    /**
     * @return @throws IOException
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        delegate.close();
        bytes.close();
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @return @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @param readlimit
     * @see java.io.InputStream#mark(int)
     */
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    /**
     * @return @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return delegate.markSupported();
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#reset()
     */
    public void reset() throws IOException {
        delegate.reset();
        bytes.reset();
    }

    /**
     * @param n
     * @return
     * @throws IOException
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    /**
     * @return @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean isFinished() {
        return delegate.isFinished();
    }

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        delegate.setReadListener(readListener);
    }

}

package com.google.appspot.resultsin;


import org.junit.Assert;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tom on 14/03/2017.
 */
public class MutableServletInputStream extends ServletInputStream {

    private final InputStream sourceStream;


    /**
     * Create a DelegatingServletInputStream for the given source stream.
     * @param sourceStream the source stream (never <code>null</code>)
     */
    public MutableServletInputStream(InputStream sourceStream) {
        Assert.assertNotNull("Source InputStream must not be null", sourceStream);
        this.sourceStream = sourceStream;
    }

    /**
     * Return the underlying source stream (never <code>null</code>).
     */
    public final InputStream getSourceStream() {
        return this.sourceStream;
    }


    public int read() throws IOException {
        return this.sourceStream.read();
    }

    public void close() throws IOException {
        super.close();
        this.sourceStream.close();
    }


    }


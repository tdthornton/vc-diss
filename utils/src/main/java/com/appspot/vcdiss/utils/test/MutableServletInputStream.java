package com.appspot.vcdiss.utils.test;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper to allow InputStreams to masquerade as HttpServletInputStreams.
 */
public class MutableServletInputStream extends ServletInputStream {

    private final InputStream sourceStream;


    /**
     * Create a DelegatingServletInputStream for the given source stream.
     * @param sourceStream the source stream (never <code>null</code>)
     */
    public MutableServletInputStream(InputStream sourceStream) {
        if (sourceStream==null) {
            throw new IllegalArgumentException("InputStream should not be null.");
        }
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


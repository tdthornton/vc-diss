package com.google.appspot.vcdiss.ops;

/**
 * Created by Tom on 21/04/2017.
 */
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

class MutableServletInputStream extends ServletInputStream {
    InputStream inputStream;

    MutableServletInputStream(String string) throws IOException {
        this.inputStream = IOUtils.toInputStream(string, "utf-8");
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }


    }

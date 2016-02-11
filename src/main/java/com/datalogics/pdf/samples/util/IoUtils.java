/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.RandomAccessFileByteWriter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

/**
 * A utility class that contains some commonly used I/O methods.
 */
public final class IoUtils {
    private IoUtils() {}

    /**
     * Creates a ByteWriter using an outputUrl. If the file the URL is pointing to exists, it gets deleted.
     *
     * @param outputUrl The URL used to create a ByteWriter
     * @return A ByteWriter created using the outputUrl
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static ByteWriter getByteWriterFromFile(final URL outputUrl) throws IOException {
        File file = null;
        try {
            file = new File(outputUrl.toURI());
        } catch (final URISyntaxException e) {
            throw new IOException(e);
        }

        if (file.exists()) {
            Files.delete(file.toPath());
        }
        final RandomAccessFile outputPdfFile = new RandomAccessFile(file, "rw");
        return new RandomAccessFileByteWriter(outputPdfFile);
    }
}

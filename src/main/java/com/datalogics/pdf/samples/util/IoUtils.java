/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.RandomAccessFileByteWriter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

/**
 * A utility class to handle common IO operations.
 */
public final class IoUtils {

    /**
     * This is a utility class, and won't be instantiated.
     */
    private IoUtils() {}

    /**
     * Create a ByteWriter from a path to an output file.
     *
     * @param outputPath The path ByteWriter should write to
     * @return A ByteWrite for the otputPath
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static ByteWriter getByteWriterFromFile(final String outputPath) throws IOException {
        RandomAccessFile outputPdfFile = null;

        final File file = new File(outputPath);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        outputPdfFile = new RandomAccessFile(file, "rw");
        return new RandomAccessFileByteWriter(outputPdfFile);
    }

}

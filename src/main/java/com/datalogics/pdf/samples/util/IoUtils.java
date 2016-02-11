/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.RandomAccessFileByteWriter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
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

    /**
     * Parses the input URL and returns the file extension in a String format.
     *
     * @param fileUrl The URL of the file
     * @return The file extension in a string format
     * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as a URI
     *         reference.
     */
    public static String getFileExtensionFromUrl(final URL fileUrl) throws URISyntaxException {
        final String stringPath = fileUrl.toString();

        return FilenameUtils.getExtension(stringPath);
    }

    /**
     * Creates a URL representation of a String.
     *
     * @param inputString A string used to create an URL
     * @return An URL created from the inputString
     * @throws MalformedURLException Thrown to indicate that a malformed URL has occurred. Either no legal protocol
     *         could be found in a specification string or the string could not be parsed.
     */
    public static URL createUrlFromString(final String inputString) throws MalformedURLException {
        // TODO Auto-generated method stub
        return new URL(inputString);
    }
}

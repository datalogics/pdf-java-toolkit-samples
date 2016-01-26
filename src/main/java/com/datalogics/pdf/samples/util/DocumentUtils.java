/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class that contains some commonly used document methods.
 */
public final class DocumentUtils {

    /**
     * This is a utility class, and won't be instantiated.
     */
    private DocumentUtils() {}

    /**
     * Opens a PDF document from a path.
     *
     * @param inputPath The path to the PDF document to open
     * @return A PDF document opened from the inputPath
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static PDFDocument openPdfDocument(final String inputPath)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {
        // return openPdfDocument(inputPath, null);
        ByteReader reader = null;
        InputStream inputStream = null;

        inputStream = getInputStreamFromPath(inputPath);
        reader = new InputStreamByteReader(inputStream);
        final PDFDocument document = PDFDocument.newInstance(reader, PDFOpenOptions.newInstance());

        return document;
    }

    /**
     * Get an InputStream of a file from a path.
     *
     * @param inputPath The path to the file to open
     * @return A new InputStream containing the resource
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     */
    private static InputStream getInputStreamFromPath(final String inputPath)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {
        final File file = new File(inputPath);
        final InputStream inputStream = new FileInputStream(file);

        return inputStream;
    }
}

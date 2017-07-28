/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.internal.io.ByteWriter;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidXMLException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.IOException;
import java.net.URL;

/**
 * This sample demonstrates how to remove metadata from a PDF.
 */
public final class RemoveMetadata {
    public static final String OUTPUT_PDF_PATH = "MetadataRemoved.pdf";
    public static final String INPUT_PDF_PATH = "pdfjavatoolkit-ds.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private RemoveMetadata() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args)
                    throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        URL inputUrl = null;
        URL outputUrl = null;
        if (args.length > 0) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            outputUrl = IoUtils.createUrlFromPath(args[1]);
        } else {
            inputUrl = RemoveMetadata.class.getResource(INPUT_PDF_PATH);
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PDF_PATH);
        }

        removeMetadata(inputUrl, outputUrl);
    }

    /**
     * Removes metadata from the supplied pdf and replaces it with metadata as if the pdf was just created. Doing a full
     * save prevents forensic discovery.
     *
     * @param inputUrl the path to read the input PDF from
     * @param outputUrl the path to output the pdf
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException some general io error occurred
     * @throws PDFInvalidXMLException there was a problem processing an XML file
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     */
    public static void removeMetadata(final URL inputUrl, final URL outputUrl)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException,
                    PDFInvalidXMLException, PDFUnableToCompleteOperationException, PDFInvalidParameterException {
        final PDFDocument document = DocumentUtils.openPdfDocument(inputUrl);

        document.removeMetadata();

        final ByteWriter writer = IoUtils.newByteWriter(outputUrl);
        document.save(writer, PDFSaveFullOptions.newInstance());
        writer.close();
        document.close();
    }
}

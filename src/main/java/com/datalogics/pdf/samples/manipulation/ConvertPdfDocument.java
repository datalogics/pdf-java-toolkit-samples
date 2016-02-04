/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;


import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;

import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.ConvertPdfA1Util;
import com.datalogics.pdf.samples.util.DocumentUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Sample that demonstrates how to convert a PDF to PDF/A-1b.
 */
public final class ConvertPdfDocument {

    private static final String INPUT_UNCONVERTED_PDF_PATH = "UnConvertedPdf.pdf";
    private static final String OUTPUT_CONVERTED_PDF_PATH = "ConvertedPdfa-1b.pdf";


    /**
     * This is a utility class, and won't be instantiated.
     */
    private ConvertPdfDocument() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        URL inputUrl = null;
        URL outputUrl = null;
        if (args.length > 0) {
            inputUrl = new URL(args[0]);
            outputUrl = new URL(args[1]);
        } else {
            inputUrl = ConvertPdfDocument.class.getResource(INPUT_UNCONVERTED_PDF_PATH);
            outputUrl = new File(OUTPUT_CONVERTED_PDF_PATH).toURI().toURL();
        }

        convertToPdfA1(inputUrl, outputUrl);
    }

    /**
     * Takes a an URL to a PDF file and converts it to PDF/A-1.
     *
     * @param inputUrl The URL to the input PDF file
     * @param outputUrl The URL to the output PDF file
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     */
    public static void convertToPdfA1(final URL inputUrl, final URL outputUrl)
                    throws IOException, PDFFontException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException, PDFUnableToCompleteOperationException {
        PDFDocument pdfDoc = null;
        try {
            // attach font set to PDF
            final PDFFontSet pdfaFontSet = FontSetLoader.newInstance().getFontSet();
            final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
            openOptions.setFontSet(pdfaFontSet);

            pdfDoc = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);

            // Note: Transparency is not handled by PDF Java Toolkit
            ConvertPdfA1Util.convertPdfA1(pdfDoc, outputUrl.toURI().getPath());
        } catch (final URISyntaxException e) {
            throw new PDFIOException(e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }

    }
}

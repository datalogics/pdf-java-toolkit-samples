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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;


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
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            path = OUTPUT_CONVERTED_PDF_PATH;
        }

        convertToPdfA1(path);
    }

    private static void convertToPdfA1(final String outputPath)
                    throws IOException, PDFFontException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException, PDFUnableToCompleteOperationException,
                    URISyntaxException {
        // attach font set to PDF
        final PDFFontSet pdfaFontSet = FontSetLoader.newInstance().getFontSet();
        final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
        openOptions.setFontSet(pdfaFontSet);

        final InputStream inputStream = ConvertPdfDocument.class.getResourceAsStream(INPUT_UNCONVERTED_PDF_PATH);
        final PDFDocument pdfDoc = DocumentUtils.openPdfDocumentWithStream(inputStream);

        // Note: Transparency is not handled by PDF Java Toolkit
        ConvertPdfA1Util.convertPdfA1(pdfDoc, outputPath);

    }
}

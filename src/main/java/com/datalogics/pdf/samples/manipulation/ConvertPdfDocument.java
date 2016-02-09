/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;


import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.internal.io.RandomAccessFileByteWriter;
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
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveOptions;
import com.adobe.pdfjt.services.pdfa.PDFA1bConfiguredConversionHandler;
import com.adobe.pdfjt.services.pdfa.PDFAConformanceLevel;
import com.adobe.pdfjt.services.pdfa.PDFAConversionOptions;
import com.adobe.pdfjt.services.pdfa.PDFAConversionOptionsFactory;
import com.adobe.pdfjt.services.pdfa.PDFAService;

import com.datalogics.pdf.document.FontSetLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Logger;


/**
 * Sample that demonstrates how to convert a PDF to PDF/A-1b.
 * <p>
 * Note: Transparency is not handled by PDF Java Toolkit.
 * </p>
 */

public final class ConvertPdfDocument {
    private static final Logger LOGGER = Logger.getLogger(ConvertPdfDocument.class.getName());

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

        convertToPdfA1B(path);
    }

    private static void convertToPdfA1B(final String outputPath)
                    throws IOException, PDFFontException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException, PDFUnableToCompleteOperationException {

        PDFDocument pdfDoc = null;
        ByteReader reader = null;
        ByteWriter writer = null;

        final InputStream inputStream = ConvertPdfDocument.class.getResourceAsStream(INPUT_UNCONVERTED_PDF_PATH);
        reader = new InputStreamByteReader(inputStream);

        // attach font set to PDF
        final PDFFontSet pdfaFontSet = FontSetLoader.newInstance().getFontSet();
        final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
        openOptions.setFontSet(pdfaFontSet);

        pdfDoc = PDFDocument.newInstance(reader, openOptions);

        final PDFA1bConfiguredConversionHandler handler = new PDFA1bConfiguredConversionHandler();
        final PDFAConversionOptions options = PDFAConversionOptionsFactory.getConfiguredPdfA1bInstance(pdfDoc);

        try {
            // Attempt to convert the PDF to PDF/A-1b
            if (PDFAService.convert(pdfDoc, PDFAConformanceLevel.Level_1b, options, handler)) {
                final PDFSaveOptions saveOpt = PDFSaveFullOptions.newInstance();

                // if the pdf contains compressed object streams, we should
                // decompress these so that the pdf can be converted to PDF/A-1b
                if (handler.requiresObjectDecompression()) {
                    saveOpt.setObjectCompressionMode(PDFSaveOptions.OBJECT_COMPRESSION_NONE);
                }

                final RandomAccessFile outputPdf = new RandomAccessFile(outputPath, "rw");
                writer = new RandomAccessFileByteWriter(outputPdf);

                pdfDoc.save(writer, saveOpt);

                final String successMsg = "\nConverted output written to: " + outputPath;
                LOGGER.info(successMsg);
            } else {
                LOGGER.info("Errors encountered when converting document.");
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }
}

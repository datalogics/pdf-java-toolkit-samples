/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.internal.io.ByteWriter;
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
import com.adobe.pdfjt.services.pdfa2.PDFA2ConformanceLevel;
import com.adobe.pdfjt.services.pdfa2.PDFA2ConversionOptions;
import com.adobe.pdfjt.services.pdfa2.PDFA2ConversionOptionsFactory;
import com.adobe.pdfjt.services.pdfa2.PDFA2DefaultConversionHandler;
import com.adobe.pdfjt.services.pdfa2.PDFA2DefaultValidationHandler;
import com.adobe.pdfjt.services.pdfa2.PDFA2Service;
import com.adobe.pdfjt.services.pdfa2.PDFA2ValidationOptions;

import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;

/**
 * This sample demonstrates how to convert a PDF to a PDF/A-2 archive file. (PDF/A-2b by default)
 */
public final class ConvertToPdfA2 {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String INPUT_PDF_PATH = "UnconvertedPdfA2.pdf";
    public static final String OUTPUT_PDF_PATH = "ConvertedToPdfA2.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private ConvertToPdfA2() {}

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
        String conformanceLevelInput = null;
        PDFA2ConformanceLevel conformanceLevel = PDFA2ConformanceLevel.Level_2b;
        if (args.length == 3) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            outputUrl = IoUtils.createUrlFromPath(args[1]);
            conformanceLevelInput = args[2];
            if ("a".equals(conformanceLevelInput)) {
                conformanceLevel = PDFA2ConformanceLevel.Level_2a;
            } else if ("b".equals(conformanceLevelInput)) {
                // this isn't necessary since it defaults to 2b, for clarity though it is here
                conformanceLevel = PDFA2ConformanceLevel.Level_2b;
            } else if ("u".equals(conformanceLevelInput)) {
                conformanceLevel = PDFA2ConformanceLevel.Level_2u;
            } else {
                throw new IllegalArgumentException("Valid 'a', 'b', or 'u' PDF/A2 conformance level not specified.");
            }
        } else {
            inputUrl = ConvertPdfDocument.class.getResource(INPUT_PDF_PATH);
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PDF_PATH);
        }

        convertToPdfA2(inputUrl, outputUrl, conformanceLevel);
        validate(outputUrl, conformanceLevel);
    }

    /**
     * Converts an input PDF document to the PDF/A-2 standard with the specified conformance level.
     *
     * @param inputUrl The URL of the document to be converted
     * @param outputUrl The URL of the converted document
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     */
    public static void convertToPdfA2(final URL inputUrl, final URL outputUrl,
                                      final PDFA2ConformanceLevel conformanceLevel)
                    throws IOException, PDFFontException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException, PDFUnableToCompleteOperationException {
        ByteWriter writer = null;
        // Attach font set to PDF
        final PDFFontSet pdfaFontSet = FontSetLoader.newInstance().getFontSet();
        final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
        openOptions.setFontSet(pdfaFontSet);

        final PDFDocument pdfDoc = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);

        final PdfA2ConversionHandler handler = new PdfA2ConversionHandler();
        final PDFA2ConversionOptions options = PDFA2ConversionOptionsFactory.getConfiguredPdfA2bInstance(pdfDoc);

        try {
            // Attempt to convert the PDF to the supplied PDF/A2 conformance level
            if (PDFA2Service.convert(pdfDoc, conformanceLevel, options, handler)) {
                final PDFSaveOptions saveOpt = PDFSaveFullOptions.newInstance();

                writer = IoUtils.newByteWriter(outputUrl);
                pdfDoc.save(writer, saveOpt);

                final String successMsg = "\nConverted output written to: " + outputUrl.toString();
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

    /**
     * Validates an input PDF document for PDF/A-2 specification with whatever conformance level was supplied.
     *
     * @param inputUrl The URL of the document to be validated
     * @param conformanceLevel The conformance level of the PDF/A-2 spec the document should conform to
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     */
    public static boolean validate(final URL inputUrl, final PDFA2ConformanceLevel conformanceLevel)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException,
                    PDFUnableToCompleteOperationException, PDFInvalidParameterException {
        final PDFDocument doc = DocumentUtils.openPdfDocument(inputUrl);
        final PdfA2ValidationHandler validationHandler = new PdfA2ValidationHandler();
        final boolean validity = PDFA2Service.validate(doc, conformanceLevel, new PDFA2ValidationOptions(),
                                                       validationHandler);

        doc.close();

        if (validity) {
            LOGGER.info("Validation succeeded.");
        } else {
            LOGGER.info("Validation failed");
        }
        return validity;
    }

    private static final class PdfA2ConversionHandler extends PDFA2DefaultConversionHandler {
        @Override
        public boolean conversionSummary(final boolean fixesApplied, final boolean unfixableFound) {
            // continue processing unless errors were found
            return !unfixableFound;
        }
    }

    private static final class PdfA2ValidationHandler extends PDFA2DefaultValidationHandler {
        @Override
        public boolean endDocumentScan(final boolean errorsFound) {
            // continue processing unless errors were found
            return !errorsFound;
        }
    }
}

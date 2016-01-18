/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.exceptions.PDFUnsupportedFeatureException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationEnum;
import com.adobe.pdfjt.services.ap.spi.APContext;
import com.adobe.pdfjt.services.ap.spi.APResources;
import com.adobe.pdfjt.services.formflattener.FormFlattener;

import com.datalogics.pdf.document.DocumentHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

/**
 * This sample demonstrates how to use the flattening functions to flatten the interactive elements of a PDF. The
 * flattening functionality currently handles forms and annotations. All flattening is done through the FormFlattener's
 * flattenDocument() method, and specific element types can be flattened or ignored by specifying them in the
 * setAnnotationsToBeProcessed() method of the APContext passed in. Note that PDFJT does not currently flatten
 * transparency.
 */
public final class FlattenPdf {

    private static final String OUTPUT_FLATTENED_PDF_PATH = "Flattened.pdf";
    private static final String INPUT_PDF_PATH = "Acroform.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private FlattenPdf() {}

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
        String inputPath = null;
        String outputPath = null;

        if (args.length > 1) {
            inputPath = args[0];
            outputPath = args[1];
        } else {
            inputPath = INPUT_PDF_PATH;
            outputPath = OUTPUT_FLATTENED_PDF_PATH;
        }
        flattenPdf(inputPath, outputPath);
    }

    /**
     * Flatten a PDF.
     *
     * @param inputPath the path to read the input PDF from
     * @param outputPath the path to output the pdf
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFUnableToCompleteOperationException one of the requested operations could not be completed
     * @throws PDFInvalidParameterException one of the parameters was invalid
     * @throws PDFConfigurationException the configuration of the PDF or one of its parts was invalid
     * @throws PDFUnsupportedFeatureException the requested feature is not currently supported
     * @throws PDFFontException there was an error using one of the required fonts
     */
    private static void flattenPdf(final String inputPath, final String outputPath)
                    throws IOException, PDFIOException, PDFSecurityException, PDFInvalidDocumentException,
                    PDFFontException, PDFUnsupportedFeatureException, PDFConfigurationException,
                    PDFInvalidParameterException, PDFUnableToCompleteOperationException {

        final APResources apResources = new APResources(null, null, null);
        final APContext apContext = new APContext(apResources, true, null);

        // This sample flattens all elements in the given PDF. However, specific elements can be included or excluded
        // by setting the enum of the desired element rather than PDFAnnotationEnum.class.
        apContext.setAnnotationsToBeProcessed(EnumSet.allOf(PDFAnnotationEnum.class));

        PDFDocument pdfDoc = null;
        ByteReader byteReader = null;
        // Get the PDF file.
        try (final InputStream inputStream = FlattenPdf.class.getResourceAsStream(inputPath);) {
            if (inputStream == null) {
                byteReader = new InputStreamByteReader(new FileInputStream(inputPath));
            } else {
                byteReader = new InputStreamByteReader(inputStream);
            }
        }
        pdfDoc = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());

        // Flatten all the pages of the given input PDF Document, with the
        // default text formatter.
        FormFlattener.flattenDocument(apContext, pdfDoc, null);

        // Save the flattened file to an output PDF file
        DocumentHelper.saveFullAndClose(pdfDoc, outputPath);
    }

}

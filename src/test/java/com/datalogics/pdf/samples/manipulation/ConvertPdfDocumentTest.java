/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.services.pdfa.PDFAConformanceLevel;
import com.adobe.pdfjt.services.pdfa.PDFADefaultValidationHandler;
import com.adobe.pdfjt.services.pdfa.PDFAService;
import com.adobe.pdfjt.services.pdfa.PDFAValidationOptions;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.net.URL;


/**
 * Test the ConvertPdfDocument sample.
 */
public class ConvertPdfDocumentTest extends SampleTest {

    private static final String INPUT_UNCONVERTED_PDF_PATH = "UnConvertedPdf.pdf";
    private static final String OUTPUT_FILE_NAME = "ConvertedPdfa-1b.pdf";

    @Test
    public void testMain() throws Exception {
        final URL inputUrl = ConvertPdfDocument.class.getResource(INPUT_UNCONVERTED_PDF_PATH);
        final File file = newOutputFileWithDelete(OUTPUT_FILE_NAME);
        final URL outputUrl = file.toURI().toURL();

        ConvertPdfDocument.convertToPdfA1(inputUrl, outputUrl);
        // Make sure the Output file exists.
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = openPdfDocument(file.getCanonicalPath());
        try {
            /*
             * Let's validate the converted file to make sure that it's PDFa-1b complaint.
             */
            final PDFADefaultValidationHandler validationHandler = new PDFADefaultValidationHandler();
            final boolean validity = PDFAService.validate(doc, PDFAConformanceLevel.Level_1b,
                                                          new PDFAValidationOptions(), validationHandler);

            assertTrue(file.getPath() + " must be pdfa1-b complaint", validity);

        } finally {
            doc.close();
        }
    }
}

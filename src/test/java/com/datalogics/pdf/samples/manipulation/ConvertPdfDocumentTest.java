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
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.net.URL;


/**
 * Test the ConvertPdfDocument sample.
 */
public class ConvertPdfDocumentTest extends SampleTest {

    private static final String FILE_NAME = "ConvertedPdfa-1b.pdf";

    @Test
    public void testMain() throws Exception {
        final URL inputUrl = ConvertPdfDocument.class.getResource(ConvertPdfDocument.INPUT_UNCONVERTED_PDF_PATH);
        final File outputFile = newOutputFileWithDelete(FILE_NAME);
        final URL outputUrl = outputFile.toURI().toURL();

        ConvertPdfDocument.convertToPdfA1B(inputUrl, outputUrl);
        // Make sure the Output file exists.
        assertTrue(outputFile.getPath() + " must exist after run", outputFile.exists());

        final PDFDocument doc = DocumentUtils.openPdfDocument(outputUrl);
        try {
            /*
             * Let's validate the converted file to make sure that it's PDFa-1b complaint.
             */
            final PDFADefaultValidationHandler validationHandler = new PDFADefaultValidationHandler();
            final boolean validity = PDFAService.validate(doc, PDFAConformanceLevel.Level_1b,
                                                          new PDFAValidationOptions(), validationHandler);

            assertTrue(outputFile.getPath() + " must be pdfa1-b complaint", validity);

        } finally {
            doc.close();
        }
    }
}

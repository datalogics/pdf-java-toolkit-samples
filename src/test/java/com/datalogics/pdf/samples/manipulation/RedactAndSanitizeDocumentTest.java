/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmarkRoot;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

/**
 * Tests the RedactAndSanitizeDocuments sample.
 */
public class RedactAndSanitizeDocumentTest extends SampleTest {
    private static final String SEARCH_STRING = "Reader";
    private static final String OUTPUT_PDF_PATH = "RedactAndSanitizeTest.pdf";
    private static final String INPUT_PDF_PATH = "pdfjavatoolkit-ds.pdf";
    private static final String INPUT_PDF_PATH_WITH_SIGNATURE = "pdfjavatoolkit-ds-signature.pdf";
    private static final String OUTPUT_PDF_PATH_NOT_SANITIZED = "NotSanitized.pdf";

    @Test
    public void testMain() throws Exception {
        final URL inputUrl = RedactAndSanitizeDocumentTest.class.getResource(INPUT_PDF_PATH);

        final File file = newOutputFile(OUTPUT_PDF_PATH);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        final URL outputUrl = file.toURI().toURL();

        RedactAndSanitizeDocument.redactAndSanitize(inputUrl, outputUrl, SEARCH_STRING);
        assertTrue(file.getPath() + " must exist after run", file.exists());
        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(outputUrl);

            // Test redaction
            for (int i = 0; i < 2; i++) {
                final String contentsAsString = pageContentsAsString(document, i);
                final String resourceName = String.format("pdfjavatoolkit-ds.pdf.page%d.txt", i);

                assertEquals(contentsOfResource(resourceName), contentsAsString);
            }

            // Test sanitization
            final PDFCatalog catalog = document.requireCatalog();
            final PDFBookmarkRoot bmRoot = catalog.getBookmarkRoot();
            assertNull("The Outlines entry in the catalog should not exist", bmRoot);

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    @Test
    public void testCannotSanitizeDocument() throws Exception {
        final URL inputUrl = RedactAndSanitizeDocumentTest.class.getResource(INPUT_PDF_PATH_WITH_SIGNATURE);

        final File file = newOutputFile(OUTPUT_PDF_PATH_NOT_SANITIZED);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        final URL outputUrl = file.toURI().toURL();

        PDFDocument inputDocument = null;

        try {
            inputDocument = DocumentUtils.openPdfDocument(inputUrl);
            RedactAndSanitizeDocument.sanitizeDocument(inputDocument, outputUrl);
            assertFalse(file.getPath() + " must not exist after run", file.exists());

        } finally {
            if (inputDocument != null) {
                inputDocument.close();
            }
        }
    }
}

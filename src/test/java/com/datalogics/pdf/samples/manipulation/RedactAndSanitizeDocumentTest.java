/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmarkRoot;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
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
        final File file = newOutputFile(OUTPUT_PDF_PATH);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        final String inputPath = RedactAndSanitizeDocumentTest.class.getResource(INPUT_PDF_PATH).getPath();
        RedactAndSanitizeDocument.main(inputPath, file.getCanonicalPath(), SEARCH_STRING);
        assertTrue(file.getPath() + " must exist after run", file.exists());
        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(file.getCanonicalPath());

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
    public void testCantSanitizeDocument() throws Exception {
        final File file = newOutputFile(OUTPUT_PDF_PATH_NOT_SANITIZED);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        final String inputPath = RedactAndSanitizeDocumentTest.class.getResource(INPUT_PDF_PATH_WITH_SIGNATURE)
                                                                    .getPath();
        RedactAndSanitizeDocument.main(inputPath, file.getCanonicalPath(), SEARCH_STRING);
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument document = DocumentUtils.openPdfDocument(file.getCanonicalPath());

        // // Test sanitization
        final PDFCatalog catalog = document.requireCatalog();
        final PDFBookmarkRoot bmRoot = catalog.getBookmarkRoot();
        assertNotNull("The Outlines entry in the catalog should exist, and bookmarks should not be removed.", bmRoot);
    }
}

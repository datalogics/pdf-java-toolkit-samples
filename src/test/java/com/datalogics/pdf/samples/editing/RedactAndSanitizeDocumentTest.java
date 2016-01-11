/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.editing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmarkRoot;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests the RedactAndSanitizeDocuments sample.
 */
public class RedactAndSanitizeDocumentTest extends SampleTest {
    private static final String searchString = "Reader";
    private static final String outputPDFPath = "pdfjavatoolkit-ds-out.pdf";

    @Test
    public void testMain() throws Exception {
        final File file = newOutputFile(outputPDFPath);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        RedactAndSanitizeDocument.main(file.getCanonicalPath(), searchString);
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument document = openPdfDocument(file.getCanonicalPath());

        // Test redaction
        for (int i = 0; i < 2; i++) {
            final String contentsAsString = pageContentsAsString(document, i);
            final String resourceName = String.format("pdfjavatoolkit-ds.pdf.page%d.txt", i);

            assertEquals(contentsOfResource(resourceName), contentsAsString);
        }

        // // Test sanitization
        final PDFCatalog catalog = document.requireCatalog();
        final PDFBookmarkRoot bmRoot = catalog.getBookmarkRoot();
        assertNull("The Outlines entry in the catalog should not exist", bmRoot);
    }
}

/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

/**
 * Tests the TextCharacterBoxes sample.
 */
public class TextCharacterBoxesTest extends SampleTest {
    private static final String OUTPUT_PDF_PATH = TextCharacterBoxesTest.class.getSimpleName() + ".pdf";

    @Test
    public void testCharacterBoxes() throws Exception {
        final URL inputUrl = TextCharacterBoxes.class.getResource(TextCharacterBoxes.INPUT_PDF_PATH);

        final File file = newOutputFile(OUTPUT_PDF_PATH);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        final URL outputUrl = file.toURI().toURL();

        TextCharacterBoxes.drawCharacterBoxes(inputUrl, outputUrl);

        assertTrue(file.getPath() + " must exist after run", file.exists());
        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(outputUrl);

            // Test contents
            for (int i = 0; i < 2; i++) {
                final String contentsAsString = pageContentsAsString(document, i);
                final String resourceName = String.format(OUTPUT_PDF_PATH + ".page%d.txt", i);

                assertEquals(String.format("page %d contents", i), contentsOfResource(resourceName),
                             contentsAsString);
            }

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}

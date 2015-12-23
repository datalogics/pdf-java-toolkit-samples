/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.graphics.font.PDFFont;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests the MakeWhiteFangBook sample.
 */
public class MakeWhiteFangBookTest extends SampleTest {
    static final String FILE_NAME = "WhiteFang.pdf";

    @Test
    public void testMain() throws Exception {
        final File file = newOutputFile(FILE_NAME);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        MakeWhiteFangBook.main(file.getCanonicalPath());
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = openPdfDocument(file.getCanonicalPath());

        try {
            // Verify the resources
            final PDFResources resources = pageResources(doc, 1);

            final PDFFont f0 = resources.getFont(ASName.create("F0"));
            assertEquals(ASName.k_Times_Bold, f0.getBaseFont());
            assertNiceSimpleFont(f0);

            final PDFFont f1 = resources.getFont(ASName.create("F1"));
            assertEquals(ASName.k_Times_Roman, f1.getBaseFont());
            assertNiceSimpleFont(f1);

            // Verify the contents
            for (int i = 1; i < 7; i++) {
                final String contentsAsString = pageContentsAsString(doc, i);
                final String resourceName = String.format("WhiteFang.pdf.page%d.txt", i);

                assertEquals(contentsOfResource(resourceName), contentsAsString);
            }

        } finally {
            doc.close();
        }
    }
}

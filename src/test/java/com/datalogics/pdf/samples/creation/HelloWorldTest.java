/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests the HelloWorld sample.
 */
public class HelloWorldTest extends SampleTest {
    static final String FILE_NAME = "HelloWorld.pdf";

    @Test
    public void testMain() throws Exception {
        final File file = newOutputFile(FILE_NAME);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        HelloWorld.main(file.getCanonicalPath());
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = openPdfDocument(file.getCanonicalPath());

        try {

            final String contentsAsString = pageContentsAsString(doc, 0);
            final String resourceName = "HelloWorld.pdf.page1.txt";

            assertEquals(contentsOfResource(resourceName), contentsAsString);

        } finally {
            doc.close();
        }
    }
}

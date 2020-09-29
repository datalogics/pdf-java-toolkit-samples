/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;

import com.datalogics.pdf.samples.SampleTestBase;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests the HelloWorld sample.
 */
public class HelloWorldTest extends SampleTestBase {
    static final String FILE_NAME = "HelloWorld.pdf";

    @Test
    public void testHelloWorld() throws Exception {
        final File file = newOutputFile(FILE_NAME);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        HelloWorld.helloWorld(file.toURI().toURL());
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = DocumentUtils.openPdfDocument(file.toURI().toURL());

        try {

            final String contentsAsString = pageContentsAsString(doc, 0);
            final String resourceName = "HelloWorld.pdf.page1.txt";

            assertEquals(contentsOfResource(resourceName), contentsAsString);

        } finally {
            doc.close();
        }
    }
}

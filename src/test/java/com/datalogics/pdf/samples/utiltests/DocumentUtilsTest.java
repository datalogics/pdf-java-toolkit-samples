/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.utiltests;

import static org.junit.Assert.assertNotNull;

import com.adobe.pdfjt.pdf.document.PDFDocument;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.net.URI;

/**
 * Tests the DocumentUtils utility class.
 */
public class DocumentUtilsTest extends SampleTest {
    static final String FILE_NAME = "pdfjavatoolkit ds.pdf";

    @Test
    public void testMain() throws Exception {
        final URI uri = new URI(DocumentUtilsTest.class.getResource(FILE_NAME).toString());
        final String inputPath = uri.getPath();
        final PDFDocument document = DocumentUtils.openPdfDocument(inputPath);

        assertNotNull("PDF", document);
    }
}

/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests the FlattenPdf sample.
 */
public class FlattenPdfTest extends SampleTest {

    private static final String OUTPUT_FLATTENED_PDF_PATH = "Flattened.pdf";
    private static final String INPUT_FORM_PDF_PATH = "Acroform.pdf";

    @Test
    public void testFlattenForm() throws Exception {
        final File file = newOutputFile(OUTPUT_FLATTENED_PDF_PATH);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        FlattenPdf.main(INPUT_FORM_PDF_PATH, file.getCanonicalPath());
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument document = openPdfDocument(file.getCanonicalPath());

        assertEquals("There should not be an Acroform dictionary in a flattened form document",
                     document.getInteractiveForm(), null);
    }

    @Test
    public void testFlattenAnnotations() throws Exception {

    }

    @Test
    public void testFlattenLayers() {

    }
}

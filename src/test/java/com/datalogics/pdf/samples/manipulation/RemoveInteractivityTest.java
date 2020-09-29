/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationList;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;

import com.datalogics.pdf.samples.SampleTestBase;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Tests the RemoveInteractivity sample.
 */
public class RemoveInteractivityTest extends SampleTestBase {

    private static final String OUTPUT_NONINTERACTIVE_FORM_PDF_PATH = "NonInteractiveForm.pdf";
    private static final String OUTPUT_NONINTERACTIVE_ANNOTATION_PDF_PATH = "NonInteractiveAnnotation.pdf";
    private static final String INPUT_FORM_PDF_PATH = "FormDocument.pdf";
    private static final String INPUT_ANNOTATION_PDF_PATH = "annotations.pdf";

    @Test
    public void testFlattenForm() throws Exception {
        final URL inputUrl = ConvertPdfDocument.class.getResource(INPUT_FORM_PDF_PATH);
        final File file = newOutputFileWithDelete(OUTPUT_NONINTERACTIVE_FORM_PDF_PATH);
        final URL outputUrl = file.toURI().toURL();

        RemoveInteractivity.removeInteractivity(inputUrl, outputUrl);
        assertTrue(file.getPath() + " must exist after run", file.exists());

        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(file.toURI().toURL());

            assertEquals("There should not be an Acroform dictionary in a noninteractive form document",
                     document.getInteractiveForm(), null);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    @Test
    public void testFlattenAnnotations() throws Exception {
        final URL inputUrl = ConvertPdfDocument.class.getResource(INPUT_ANNOTATION_PDF_PATH);
        final File file = newOutputFileWithDelete(OUTPUT_NONINTERACTIVE_ANNOTATION_PDF_PATH);
        final URL outputUrl = file.toURI().toURL();

        RemoveInteractivity.removeInteractivity(inputUrl, outputUrl);
        assertTrue(file.getPath() + " must exist after run", file.exists());

        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(file.toURI().toURL());

            final PDFPageTree pgTree = document.requirePages();
            final PDFPage currentPage = pgTree.getPage(0);

            final PDFAnnotationList list = currentPage.getAnnotationList();

            assertEquals("Properly noninteractive annotations should not appear in the annotation list", list.size(),
                         0);
        } finally {
            if (document != null) {
                document.close();
            }
        }

    }
}

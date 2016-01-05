/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.forms.PDFField;
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmarkNode;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * Tests the Merge Documents sample.
 */
public class MergeDocumentsTest extends SampleTest {
    static final String FILE_NAME = "MergedDocument.pdf";

    @Test
    public void testMain() throws Exception {
        // Delete the file if it exists, run the sample, and confirm we got output
        final File file = newOutputFile(FILE_NAME);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        MergeDocuments.main(file.getCanonicalPath());
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = openPdfDocument(file.getCanonicalPath());

        // Confirm that we have all the pages we think we should have
        assertEquals("The merged document should contain pages equal to the sum of the source documents",
                     26, doc.requirePages().getCount());

        final PDFInteractiveForm pdfForm = doc.getInteractiveForm();
        final Iterator<PDFField> fieldIterator = pdfForm.iterator();

        // Make sure all our fields are still there
        int counter = 0;
        while (fieldIterator.hasNext()) {
            fieldIterator.next();
            counter++;
        }
        assertEquals("All form fields should remain intact", 10, counter);

        final PDFCatalog catalog = doc.requireCatalog();
        final PDFBookmarkNode.Iterator bookmarkIterator = catalog.getBookmarkRoot().iterator();

        // Make sure all our bookmarks are still there
        counter = 0;
        while (bookmarkIterator.hasNext()) {
            bookmarkIterator.next();
            counter++;
        }
        assertEquals("All bookmarks should remain intact", 35, counter);
    }
}

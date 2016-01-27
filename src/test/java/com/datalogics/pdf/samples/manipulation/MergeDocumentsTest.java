/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.interactive.forms.PDFField;
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmarkNode;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.io.InputStream;
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

        final PDFDocument doc = DocumentUtils.openPdfDocument(file.getCanonicalPath());

        // Confirm that we have all the pages we think we should have
        checkPages(doc);

        // Make sure all our fields are still there
        checkForms(doc);

        // Make sure all our bookmarks are still there
        checkBookmarks(doc);
    }

    // Compare the output page count to the sum of the input file page counts
    private void checkPages(final PDFDocument outputDoc) throws Exception {
        final int pagesFirst = getPdfFromResource(MergeDocuments.FIRST_DOCUMENT).requirePages().getCount();
        final int pagesSecond = getPdfFromResource(MergeDocuments.SECOND_DOCUMENT).requirePages().getCount();
        assertEquals("The merged document should contain pages equal to the sum of the source documents",
                     pagesFirst + pagesSecond, outputDoc.requirePages().getCount());
    }

    // Compare the number of fields in the output to the sum of the number of fields in the input files
    private void checkForms(final PDFDocument outputDoc) throws Exception {
        // Check the output doc
        PDFInteractiveForm pdfForm = outputDoc.getInteractiveForm();
        Iterator<PDFField> fieldIterator = pdfForm.iterator();
        int outputCount = 0;
        while (fieldIterator.hasNext()) {
            fieldIterator.next();
            outputCount++;
        }

        // Check the first input doc
        PDFDocument inputDoc = getPdfFromResource(MergeDocuments.FIRST_DOCUMENT);
        pdfForm = inputDoc.getInteractiveForm();
        fieldIterator = pdfForm.iterator();
        int inputCountFirst = 0;
        while (fieldIterator.hasNext()) {
            fieldIterator.next();
            inputCountFirst++;
        }

        // Check the second input doc
        inputDoc = getPdfFromResource(MergeDocuments.SECOND_DOCUMENT);
        pdfForm = inputDoc.getInteractiveForm();
        fieldIterator = pdfForm.iterator();
        int inputCountSecond = 0;
        while (fieldIterator.hasNext()) {
            fieldIterator.next();
            inputCountSecond++;
        }

        assertEquals("All form fields should remain intact", inputCountFirst + inputCountSecond, outputCount);
    }

    // Compare the number of bookmarks in the output with the sum of the bookmarks in the inputs (plus one per input)
    private void checkBookmarks(final PDFDocument outputDoc) throws Exception {
        // Check the output doc
        PDFCatalog catalog = outputDoc.requireCatalog();
        PDFBookmarkNode.Iterator bookmarkIterator = catalog.getBookmarkRoot().iterator();
        int outputCount = 0;
        while (bookmarkIterator.hasNext()) {
            bookmarkIterator.next();
            outputCount++;
        }

        // Check the first input doc
        PDFDocument inputDoc = getPdfFromResource(MergeDocuments.FIRST_DOCUMENT);
        catalog = inputDoc.requireCatalog();
        bookmarkIterator = catalog.getBookmarkRoot().iterator();
        // Start at 1 because the merge adds a bookmark to the start of this document
        int inputCountFirst = 1;
        while (bookmarkIterator.hasNext()) {
            bookmarkIterator.next();
            inputCountFirst++;
        }

        // Check the second input doc
        inputDoc = getPdfFromResource(MergeDocuments.SECOND_DOCUMENT);
        catalog = inputDoc.requireCatalog();
        bookmarkIterator = catalog.getBookmarkRoot().iterator();
        // Start at 1 because the merge adds a bookmark to the start of this document
        int inputCountSecond = 1;
        while (bookmarkIterator.hasNext()) {
            bookmarkIterator.next();
            inputCountSecond++;
        }

        assertEquals("All bookmarks should remain intact", inputCountFirst + inputCountSecond, outputCount);
    }

    // Given a resource name, create and return a PDFDocument object
    private PDFDocument getPdfFromResource(final String resourceName) throws Exception {
        ByteReader byteReader = null;
        PDFDocument pdfDoc = null;

        try (final InputStream is = MergeDocuments.class.getResourceAsStream(resourceName)) {
            byteReader = new InputStreamByteReader(is);
            pdfDoc = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
        } finally {
            if (byteReader != null) {
                byteReader.close();
            }
        }
        return pdfDoc;
    }
}

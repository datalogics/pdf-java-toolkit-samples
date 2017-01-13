/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.graphics.PDFRectangle;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.LogRecordListCollector;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Tests the TextExtract sample.
 */
public class TextExtractTest extends SampleTest {

    private static final String OUTPUT_FILE_PATH = "TextExtractTest.txt";
    private static final String INPUT_PDF_PATH = "/com/datalogics/pdf/samples/pdfjavatoolkit-ds.pdf";
    private static final String EXTRACTED_DOCUMENT_NAME = "TextExtractTest-ReadingOrder.txt";
    private static final String EMPTY_PDF_FILE_PATH = "Empty.pdf";
    private static final String EMPTY_TEXT_FILE_PATH = "Empty.txt";

    @Test
    public void testExtractTextReadingOrder() throws Exception {
        final File file = newOutputFile(OUTPUT_FILE_PATH);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        final URL inputUrl = TextExtract.class.getResource(INPUT_PDF_PATH);
        final URL outputUrl = file.toURI().toURL();

        TextExtract.extractTextReadingOrder(inputUrl, outputUrl);;
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final String extractedText = contentsOfTextFile(file);
        assertEquals(contentsOfResource(EXTRACTED_DOCUMENT_NAME), extractedText);
    }

    @Test
    public void testDocumentWithNoText() throws Exception {
        // Create a new document with a single empty page
        final PDFDocument document = createEmptyPdf();
        final File emptyPdf = newOutputFileWithDelete(EMPTY_PDF_FILE_PATH);
        DocumentHelper.saveFullAndClose(document, emptyPdf.getCanonicalPath());

        final File emptyTxt = newOutputFileWithDelete(EMPTY_TEXT_FILE_PATH);

        final URL inputUrl = emptyPdf.toURI().toURL();
        final URL outputUrl = emptyTxt.toURI().toURL();

        final ArrayList<LogRecord> logRecords = new ArrayList<>();
        final Logger logger = Logger.getLogger(TextExtract.class.getName());
        try (LogRecordListCollector collector = new LogRecordListCollector(logger, logRecords)) {
            TextExtract.extractTextReadingOrder(inputUrl, outputUrl);
        }

        // Verify that we got the expected log message
        assertEquals("Must have one log record", 1, logRecords.size());
        final LogRecord logRecord = logRecords.get(0);
        assertEquals(inputUrl.toURI().getPath() + " did not have any text to extract.", logRecord.getMessage());
        assertEquals(Level.INFO, logRecord.getLevel());

        // Verify that the output file was not created
        final Path path = Paths.get(outputUrl.toURI());
        assertTrue(outputUrl.toURI().getPath() + " should not exist.",
                   Files.notExists(path, LinkOption.NOFOLLOW_LINKS));
    }

    /*
     * Create a PDF with a single blank page, for testing.
     */
    private static PDFDocument createEmptyPdf() throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException {
        final PDFDocument document = PDFDocument.newInstance(PDFOpenOptions.newInstance());
        final PDFPage page = PDFPage.newInstance(document, PDFRectangle.newInstance(document, ASRectangle.US_LETTER));
        PDFPageTree.newInstance(document, page);
        return document;
    }
}

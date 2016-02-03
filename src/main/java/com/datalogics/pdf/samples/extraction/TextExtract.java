/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.extraction;

import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.services.readingorder.ReadingOrderTextExtractor;
import com.adobe.pdfjt.services.textextraction.Word;
import com.adobe.pdfjt.services.textextraction.WordsIterator;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.FontUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

/**
 * This sample demonstrates how to extract text from a document. The text is extracted in reading order and then saved
 * to a text file.
 */
public final class TextExtract {
    private static final String INPUT_PDF_PATH = "/com/datalogics/pdf/samples/pdfjavatoolkit-ds.pdf";
    private static final String OUTPUT_TEXT_PATH = "Text Extract.txt";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private TextExtract() {}

    /**
     * Main method.
     *
     * @param args two command line arguments - input path and output path
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        URL inputUrl = null;
        URL outputUrl = null;

        if (args.length > 1) {
            inputUrl = new URL(args[0]);
            outputUrl = new URL(args[1]);
        } else {
            inputUrl = TextExtract.class.getResource(INPUT_PDF_PATH);
            outputUrl = new File(OUTPUT_TEXT_PATH).toURI().toURL();
        }

        extractTextReadingOrder(inputUrl, outputUrl);
    }

    /**
     * Extracts the text from a PDF file in reading order.
     *
     * @param inputUrl An URL for the input document, to extract text from
     * @param outputUrl An URL for the file stream where the extracted text will be written
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws UnsupportedEncodingException the character encoding is not supported
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     */
    public static void extractTextReadingOrder(final URL inputUrl, final URL outputUrl)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException,
                    UnsupportedEncodingException, IOException, PDFUnableToCompleteOperationException {
        File outputFile = null;
        try {
            outputFile = new File(outputUrl.toURI());
        } catch (final URISyntaxException e) {
            throw new PDFIOException(e);
        }

        if (outputFile.exists()) {
            Files.delete(outputFile.toPath());
        }

        PDFDocument document = null;
        try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            document = DocumentUtils.openPdfDocument(inputUrl);

            final PDFFontSet docFontSet = FontUtils.getDocFontSet(document);
            final ReadingOrderTextExtractor extractor = ReadingOrderTextExtractor.newInstance(document, docFontSet);
            final WordsIterator wordsIter = extractor.getWordsIterator();

            while (wordsIter.hasNext()) {
                final Word word = wordsIter.next();
                outputStream.write(word.toString().getBytes("UTF-8"));
            }

        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}

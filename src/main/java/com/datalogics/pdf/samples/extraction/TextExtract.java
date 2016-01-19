/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.extraction;

import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
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
import java.nio.file.Files;

/**
 * This sample demonstrates how to extract text from a document. The text is extracted in reading order and then saved
 * to a text file.
 */
public final class TextExtract {
    private static final String INPUT_PDF_PATH = "/com/datalogics/pdf/samples/pdfjavatoolkit-ds.pdf";
    private static final String OUTPUT_TEXT_PATH = "TextExtract.txt";

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
        String inputPath = null;
        String outputPath = null;
        PDFDocument document = null;
        FileOutputStream outputStream = null;

        if (args.length > 1) {
            inputPath = args[0];
            outputPath = args[1];
        } else {
            inputPath = INPUT_PDF_PATH;
            outputPath = OUTPUT_TEXT_PATH;
        }

        try {
            final File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                Files.delete(outputFile.toPath());
            }
            outputStream = new FileOutputStream(outputFile);
            document = DocumentUtils.openPdfDocument(inputPath);
            extractTextReadingOrder(document, outputStream);
        } finally {
            if (document != null) {
                document.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Extracts the text from a PDF file in reading order.
     *
     * @param document the input document, to extract text from
     * @param outputStream the file stream containing the extracted text
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws UnsupportedEncodingException the character encoding is not supported
     * @throws IOException an I/O operation failed or was interrupted
     */
    private static void extractTextReadingOrder(final PDFDocument document, final FileOutputStream outputStream)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException,
                    UnsupportedEncodingException, IOException {
        final PDFFontSet docFontSet = FontUtils.getDocFontSet(document);
        final ReadingOrderTextExtractor extractor = ReadingOrderTextExtractor
                                                                             .newInstance(document, docFontSet);
        // final LayoutModeTextExtractor extractor = LayoutModeTextExtractor.newInstance(document, docFontSet);
        final WordsIterator wordsIter = extractor.getWordsIterator();

        while (wordsIter.hasNext()) {
            final Word word = wordsIter.next();
            outputStream.write(word.toString().getBytes("UTF-8"));
        }
    }
}

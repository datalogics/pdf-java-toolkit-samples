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
import com.adobe.pdfjt.core.types.ASQuad;
import com.adobe.pdfjt.pdf.content.processor.PDFCharacter;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFColorSpaceDeviceRGB;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.readingorder.ReadingOrderTextExtractor;
import com.adobe.pdfjt.services.textextraction.Word;
import com.adobe.pdfjt.services.textextraction.WordsIterator;

import com.datalogics.pdf.content.ContentAppender;
import com.datalogics.pdf.content.PageContentAppender;
import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.FontUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * This sample draws a character box around every character in the input file, creating a new output file.
 */
public final class TextCharacterBoxes {
    public static final String INPUT_PDF_PATH = "/com/datalogics/pdf/samples/pdfjavatoolkit-ds.pdf";
    public static final String OUTPUT_PDF_PATH = "TextCharacterBoxes.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private TextCharacterBoxes() {}

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
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            outputUrl = IoUtils.createUrlFromPath(args[1]);
        } else {
            inputUrl = TextCharacterBoxes.class.getResource(INPUT_PDF_PATH);
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PDF_PATH);
        }

        drawCharacterBoxes(inputUrl, outputUrl);
    }

    /**
     * Draw boxes around every character in a PDF document.
     *
     * <p>
     * The boxes are stroked with a 0.125 pt red color
     *
     * @param inputUrl An URL for the input document
     * @param outputUrl An URL for the output PDF document with the characters boxed
     * @throws Exception a general exception was thrown
     */
    public static void drawCharacterBoxes(final URL inputUrl, final URL outputUrl)
                    throws Exception {
        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(inputUrl);

            final PDFFontSet docFontSet = FontUtils.getDocFontSet(document);
            final ReadingOrderTextExtractor extractor = ReadingOrderTextExtractor.newInstance(document, docFontSet);
            final Iterator<PDFPage> pagesIterator = document.requirePages().iterator();
            while (pagesIterator.hasNext()) {
                final PDFPage page = pagesIterator.next();
                drawCharacterQuadsOnPage(extractor, page);
            }
        } finally {
            if (document != null) {
                DocumentHelper.saveFullAndClose(document, outputUrl.getPath());
            }
        }
    }

    /**
     * Draw the quads around every character on a page.
     *
     * @param extractor the text extractor for the page
     * @param page the page to draw the character quads on
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws Exception a general exception was thrown
     */
    private static void drawCharacterQuadsOnPage(final ReadingOrderTextExtractor extractor,
                                         final PDFPage page)
                    throws Exception {
        final WordsIterator wordsIter = extractor.getWordsIterator(page, page.getIndex());

        if (wordsIter.hasNext()) {
            final PDFDocument document = page.getPDFDocument();
            try (PageContentAppender contentAppender = new PageContentAppender(document, page)) {
                contentAppender.setLineWidth(0.125);
                contentAppender.strokeColor(PDFColorSpaceDeviceRGB.newInstance(document),
                                            (float) 1.0, (float) 0.0, (float) 0.0);
                do {
                    final Word word = wordsIter.next();
                    final List<PDFCharacter> characters = word.getCharacters();
                    if (characters != null) {
                        for (final PDFCharacter character : characters) {
                            final ASQuad quad = character.getBoundingQuad();
                            strokeQuad(contentAppender, quad);
                        }
                    }
                } while (wordsIter.hasNext());
            }
        }
    }

    /**
     * Stroke a single quad.
     *
     * @param contentAppender where to place the generated content
     * @param quad the quad to stroke
     */
    private static void strokeQuad(final ContentAppender contentAppender, final ASQuad quad) {
        contentAppender.moveTo(quad.p1().x(), quad.p1().y());
        contentAppender.lineTo(quad.p2().x(), quad.p2().y());
        contentAppender.lineTo(quad.p3().x(), quad.p3().y());
        contentAppender.lineTo(quad.p4().x(), quad.p4().y());
        contentAppender.closePath();
        contentAppender.strokePath();
    }
}

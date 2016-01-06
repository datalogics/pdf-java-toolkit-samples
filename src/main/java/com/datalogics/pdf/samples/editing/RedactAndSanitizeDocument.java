/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.editing;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.internal.io.RandomAccessFileByteWriter;
import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASDate;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFVersion;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotation;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationEnum;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationRedaction;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;
import com.adobe.pdfjt.services.ap.AppearanceService;
import com.adobe.pdfjt.services.ap.spi.APContext;
import com.adobe.pdfjt.services.ap.spi.APResources;
import com.adobe.pdfjt.services.fontresources.PDFFontSetUtil;
import com.adobe.pdfjt.services.redaction.RedactionOptions;
import com.adobe.pdfjt.services.redaction.RedactionService;
import com.adobe.pdfjt.services.textextraction.TextExtractor;
import com.adobe.pdfjt.services.textextraction.Word;
import com.adobe.pdfjt.services.textextraction.WordsIterator;

import com.datalogics.pdf.document.FontSetLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.Locale;

/**
 *
 */
public class RedactAndSanitizeDocument {
    private static final String searchString = "Reader";
    private static final String inputPDFPath = "pdfjavatoolkit-ds.pdf";
    private static final String outputPDFPath = "pdfjavatoolkit-ds-redacted.pdf";

    private static final double[] color = { 1.0, 0, 0 }; // RGB Red
    private static final double[] incolor = { 0, 0, 0 }; // RGB Black

    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            path = inputPDFPath;
        }
        run(path);
    }


    static void run(final String inputPath) throws Exception {
        PDFDocument document = null;

        try {
            document = openPdfDocument(inputPath);
            markTextForRedaction(document, searchString);
            applyRedaction(document);
        } finally {
            if (document != null) {
                document.close();
            }
        }

    }

    private static void markTextForRedaction(final PDFDocument document, final String searchTerm)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException,
                    PDFConfigurationException, PDFInvalidParameterException {
        final String searchTermLowerCase = searchTerm.toLowerCase(Locale.ENGLISH);
        final PDFFontSet docFontSet = setupDocFontSet(document);

        final TextExtractor extractor = TextExtractor.newInstance(document,
                                                                  docFontSet);
        final WordsIterator wordsIter = extractor.getWordsIterator();

        final PDFPageTree pgTree = document.requirePages();
        PDFPage currentPage = pgTree.getPage(0);
        while (wordsIter.hasNext()) {

            final Word word = wordsIter.next();

            final String currentWord = word.toString().toLowerCase(Locale.ENGLISH);

            if (currentWord.matches(searchTermLowerCase)) {

                // This code compares a zero-based page index with a
                // one-based page index. If the next word is not on
                // the same page as the previous word, update the
                // current page, and the location of the page's right edge.
                if (1 + currentPage.getPageNumber() != word.getPageNumber()) {
                    currentPage = pgTree.getPage(word.getPageNumber() - 1);
                }
                addRedactionAnnotationToWord(document, word, currentPage);
            }
        }
        generateAnnotationAppearances(document, docFontSet);
        // Redaction Annotations are a PDF v1.7 feature.
        if (document.getOriginalVersion().lessThan(PDFVersion.v1_7)) {
            document.setToSaveVersion(PDFVersion.v1_7);
        }
    }

    private static PDFFontSet setupDocFontSet(final PDFDocument document)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException {
        PDFFontSet sysFontSet = null;
        final FontSetLoader fontSetLoader = FontSetLoader.newInstance();

        sysFontSet = fontSetLoader.getFontSet();
        return PDFFontSetUtil.buildWorkingFontSet(document,
                                                  sysFontSet, document.getDocumentLocale(), null);

    }

    private static void addRedactionAnnotationToWord(final PDFDocument document, final Word word,
                                                     final PDFPage currentPage) throws PDFInvalidDocumentException,
                                                                     PDFIOException, PDFSecurityException {
        final PDFAnnotationRedaction annot = PDFAnnotationRedaction
                                                                   .newInstance(document);

        // Set a few annotation properties that will be used to
        // generate its Appearance
        annot.setQuads(word.getBoundingQuads());
        annot.setRect(annot.getRedactionAreaBBox());
        annot.setColor(color);
        annot.setInteriorColor(incolor[0], incolor[1], incolor[2]);

        // Set the Annotation's creation and modification date.
        final ASDate now = new ASDate();
        annot.setCreationDate(now.toString());
        annot.setModificationDate(now.toString());

        // Set more Annotation properties.
        annot.setSubject("Redact");
        annot.setTitle("PDFJT");
        annot.setFlags(PDFAnnotation.kPrint);

        currentPage.addAnnotation(annot);
    }

    private static void generateAnnotationAppearances(final PDFDocument document, final PDFFontSet docFontSet)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, PDFFontException,
                    PDFConfigurationException, PDFInvalidParameterException {
        // Generate appearance streams for only the redaction annotations of the
        // document, using font and locale resources.
        final APResources apResources = new APResources(docFontSet,
                                                        PDFDocument.ROOT_LOCALE, null);
        final APContext apContext = new APContext(apResources, true, null);
        apContext.setAnnotationsToBeProcessed(EnumSet
                                                     .of(PDFAnnotationEnum.Redact));

        AppearanceService.generateAppearances(document, apContext, null);
    }

    private static void applyRedaction(final PDFDocument document)
                    throws PDFInvalidParameterException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFUnableToCompleteOperationException, PDFFontException, IOException {
        ByteWriter writer = null;
        RedactionOptions redactionOptions = null;

        redactionOptions = new RedactionOptions(null);

        // Applying redaction
        writer = getByteWriterFromFile(outputPDFPath);
        RedactionService.applyRedaction(document, redactionOptions, writer);
    }

    private static ByteWriter getByteWriterFromFile(final String outputPath) throws IOException {
        final File file = new File(outputPath);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        final RandomAccessFile outputPdfFile = new RandomAccessFile(file, "rw");
        return new RandomAccessFileByteWriter(outputPdfFile);
    }

    private static PDFDocument openPdfDocument(final String inputPath)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {
        ByteReader reader = null;
        PDFDocument document = null;

        final InputStream inputStream = RedactAndSanitizeDocument.class.getResourceAsStream(inputPath);
        reader = new InputStreamByteReader(inputStream);
        document = PDFDocument.newInstance(reader, PDFOpenOptions.newInstance());

        return document;
    }
}

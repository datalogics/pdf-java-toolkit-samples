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
import com.adobe.pdfjt.core.fontset.PDFFontSetManager;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASDate;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveLinearOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveOptions;
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
import com.adobe.pdfjt.services.sanitization.SanitizationOptions;
import com.adobe.pdfjt.services.sanitization.SanitizationService;
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
 * This sample demonstrates how to open a document, add redaction annotations to it, and then apply the redaction. The
 * document is then sanitized.
 */
public final class RedactAndSanitizeDocument {
    private static final String searchString = "Reader";
    private static final String inputPDFPath = "pdfjavatoolkit-ds.pdf";
    private static final String outputPDFPath = "pdfjavatoolkit-ds-out.pdf";

    private static final double[] color = { 1.0, 0, 0 }; // RGB Red
    private static final double[] incolor = { 0, 0, 0 }; // RGB Black

    /**
     * This is a utility class, and won't be instantiated.
     */

    private RedactAndSanitizeDocument() {}

    /**
     * Main program.
     *
     * @param args Two command line arguments - input path and output path
     * @throws Exception A general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String inputPath;
        String outputPath;
        if (args.length > 1) {
            inputPath = args[0];
            outputPath = args[1];
        } else {
            inputPath = inputPDFPath;
            outputPath = outputPDFPath;
        }
        run(inputPath, outputPath);
    }

    /**
     * Mark text in the input document for redaction, and apply the redaction. Then sanitize the document.
     *
     * @param inputPath The PDF document to be redacted and sanitized
     * @param outputPath The redacted and sanitized output document
     * @throws Exception A general exception was thrown
     */
    static void run(final String inputPath, final String outputPath) throws Exception {
        PDFDocument document = null;

        try {
            document = openPdfDocument(inputPath);

            markTextForRedaction(document, searchString);

            applyRedaction(document, outputPath);

            sanitizeDocument(document, outputPath);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Use a search term to find all occurrences of it in a document. Then add redaction annotations to them.
     *
     * @param document The document to receive PDF annotations
     * @param searchTerm The term to be redacted
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws PDFConfigurationException There was a system problem configuring PDF support
     * @throws PDFInvalidParameterException One or more of the parameters passed to a method is invalid
     */
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

    /**
     * Add a redaction annotation to a particular word.
     *
     * @param document The document to receive a redaction annotation
     * @param word The word to be redacted
     * @param currentPage The current page that contains the word to be redacted
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     */
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

    /**
     * Apply preexisting redaction annotations to a document and save it to a file.
     *
     * @param document The document to apply the redaction to
     * @param outputPath The redacted output document
     * @throws PDFInvalidParameterException One or more of the parameters passed to a method is invalid
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws PDFUnableToCompleteOperationException A general issue occurred during the processing of the request
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws IOException An I/O operation failed or was interrupted
     */
    private static void applyRedaction(final PDFDocument document, final String outputPath)
                    throws PDFInvalidParameterException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFUnableToCompleteOperationException, PDFFontException, IOException {
        final ByteWriter writer = getByteWriterFromFile(outputPath);
        RedactionOptions redactionOptions = null;

        redactionOptions = new RedactionOptions(null);

        // Applying redaction
        RedactionService.applyRedaction(document, redactionOptions, writer);
    }

    /**
     * Apply sanitization to a document.
     *
     * @param document The document to be sanitized
     * @param sanitizedPath The sanitized output document
     * @throws IOException An I/O operation failed or was interrupted
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException One or more of the parameters passed to a method is invalid
     * @throws PDFConfigurationException There was a system problem configuring PDF support
     * @throws PDFUnableToCompleteOperationException A general issue occurred during the processing of the request
     */
    private static void sanitizeDocument(final PDFDocument document, final String sanitizedPath)
                    throws IOException, PDFFontException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException, PDFConfigurationException,
                    PDFUnableToCompleteOperationException {
        final ByteWriter writer = getByteWriterFromFile(sanitizedPath);
        final PDFSaveOptions saveOptions = PDFSaveLinearOptions.newInstance();
        // Optimize the document for fast web viewing. This is a part of sanitization.
        saveOptions.setForceCompress(true);// All the streams should be encoded with flate filter.
        final SanitizationOptions options = new SanitizationOptions();
        options.setPDFFontSet(PDFFontSetManager.getPDFFontSetInstance());
        options.setSaveOptions(saveOptions);
        SanitizationService.sanitizeDocument(document, options, writer);// API to start the sanitization.
    }

    /**
     * Generate appearance streams for redactin annotations.
     *
     * @param document The document that needs appearances generated for it's redaction annotations
     * @param docFontSet A fontset with the appropriate fonts added from the PDFDocument
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws PDFConfigurationException There was a system problem configuring PDF support
     * @throws PDFInvalidParameterException One or more of the parameters passed to a method is invalid
     */
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

    /**
     * Create a PDFFontSet that contains fonts used in the original document.
     *
     * @param document The document whose fonts need to be loaded
     * @return A fontset with the appropriate fonts added from the PDFDocument
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     */
    private static PDFFontSet setupDocFontSet(final PDFDocument document)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException {
        PDFFontSet sysFontSet = null;
        final FontSetLoader fontSetLoader = FontSetLoader.newInstance();

        sysFontSet = fontSetLoader.getFontSet();
        return PDFFontSetUtil.buildWorkingFontSet(document,
                                                  sysFontSet, document.getDocumentLocale(), null);
    }

    /**
     * Create a ByteWriter from a path to an output file.
     *
     * @param outputPath The path ByteWriter should write to
     * @return A ByteWrite for the otputPath
     * @throws IOException An I/O operation failed or was interrupted
     */
    private static ByteWriter getByteWriterFromFile(final String outputPath) throws IOException {
        final File file = new File(outputPath);
        if (file.exists()) {
            Files.delete(file.toPath());
        }

        final RandomAccessFile outputPdfFile = new RandomAccessFile(file, "rw");
        return new RandomAccessFileByteWriter(outputPdfFile);
    }

    /**
     * Open a PDF file using an input path.
     *
     * @param inputPath The PDF file to open
     * @return A new PDFDocument instance of the input document
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws IOException An I/O operation failed or was interrupted
     */
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
/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

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
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;
import com.adobe.pdfjt.services.ap.AppearanceService;
import com.adobe.pdfjt.services.ap.spi.APContext;
import com.adobe.pdfjt.services.ap.spi.APResources;
import com.adobe.pdfjt.services.digsig.SignatureManager;
import com.adobe.pdfjt.services.fontresources.PDFFontSetUtil;
import com.adobe.pdfjt.services.redaction.RedactionHandler;
import com.adobe.pdfjt.services.redaction.RedactionOptions;
import com.adobe.pdfjt.services.redaction.RedactionService;
import com.adobe.pdfjt.services.redaction.handler.PDFColorSpaceContainer;
import com.adobe.pdfjt.services.redaction.handler.RedactedObjectInfo;
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
import java.util.logging.Logger;

/**
 * This sample demonstrates how to open a document, add redaction annotations to it, and then apply the redaction. The
 * document is then sanitized.
 *
 * <p>
 * Redaction and sanitization is done in three steps. Redaction annotations are first added to the document. Those
 * annocations mark where the redaction will take place. Note that this step does not apply the actual redaction. Once
 * the annotations are in place, they are applied to the document. This step removes the content below the annotations,
 * and replaces it with black boxes.
 *
 * <p>
 * The third step of the process is sanitization. It removes the following items from the document:
 * <ul>
 * <li>Metadata
 * <li>Embedded content and attached files
 * <li>Scripts
 * <li>Hidden layers
 * <li>Embedded search indexes
 * <li>Stored form data
 * <li>Review and comment data
 * <li>Hidden data from previous document saves
 * <li>Obscured text and images
 * <li>Comments hidden within the body of the PDF file
 * <li>Unreferenced data
 * <li>Links, actions and JavaScript
 * <li>Overlapping objects
 * </ul>
 */
public final class RedactAndSanitizeDocument {
    private static final Logger logger = Logger.getLogger(RedactAndSanitizeDocument.class.getName());

    private static final String searchPDFString = "Reader";
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
     * @param args Two command line arguments - output path and search string
     * @throws Exception A general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String outputPath = null;
        String searchString = null;
        PDFDocument document = null;

        if (args.length > 1) {
            outputPath = args[0];
            searchString = args[1];
        } else {

            outputPath = outputPDFPath;
            searchString = searchPDFString;
        }

        try {
            document = openPdfDocument(inputPDFPath);

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
     * @throws PDFInvalidDocumentException A ge neral problem with the PDF document, which may now be in an invalid
     *         state
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
        // final LocalRedactionHandler redactionHandler = new LocalRedactionHandler();
        redactionOptions = new RedactionOptions(new LocalRedactionHandler());

        // Applying redaction
        RedactionService.applyRedaction(document, redactionOptions, writer);
    }

    /**
     * Apply sanitization to a document.
     *
     * @param document The document to be sanitized
     * @param sanitizedPath The sanitized output document
     * @throws IOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFUnableToCompleteOperationException A general issue occurred during the processing of the request
     * @throws PDFInvalidParameterException One or more of the parameters passed to a method is invalid
     * @throws PDFConfigurationException There was a system problem configuring PDF support
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     */
    private static void sanitizeDocument(final PDFDocument document, final String sanitizedPath)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, PDFFontException,
                    PDFConfigurationException, PDFInvalidParameterException, PDFUnableToCompleteOperationException,
                    IOException {

        if (canSanitizeDocument(document)) {
            final ByteWriter writer = getByteWriterFromFile(sanitizedPath);
            final PDFSaveOptions saveOptions = PDFSaveLinearOptions.newInstance();
        // Optimize the document for fast web viewing. This is a part of sanitization.
            saveOptions.setForceCompress(true);// All the streams should be encoded with flate filter.
            final SanitizationOptions options = new SanitizationOptions();
            options.setPDFFontSet(PDFFontSetManager.getPDFFontSetInstance());
            options.setSaveOptions(saveOptions);
            SanitizationService.sanitizeDocument(document, options, writer);// API to start the sanitization.
        }
    }

    /**
     * Checks if a PDF document can/should be sanitized. Throws an error if it can't/should be sanitized.
     *
     * @param document A PDF document, to check if it can be sanitized
     * @return Returns true of the document can/should be sanitized
     * @throws PDFInvalidDocumentException A general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException There was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException Some general security issue occurred during the processing of the request
     * @throws PDFFontException There was an error in the font set or an individual font
     * @throws PDFConfigurationException There was a system problem configuring PDF support
     * @throws PDFInvalidParameterException One or more of the parameters passed to a method is invalid
     */
    private static boolean canSanitizeDocument(final PDFDocument document)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, PDFFontException,
                    PDFConfigurationException, PDFInvalidParameterException {
        if (document.requireCatalog().getCollection() != null) {
            throw new PDFInvalidDocumentException("This document has collections."
                                                  + " Shouldn't call sanitization on this.");
        }

        final SignatureManager sigMgr = SignatureManager.newInstance(document);

        if (sigMgr.hasSignedSignatureFields() || sigMgr.hasUsageRights()) {
            throw new PDFInvalidDocumentException("This document is digitally signed."
                                                  + " Shouldn't call sanitization on this.");
        }

        final PDFInteractiveForm acroform = document.requireCatalog().getInteractiveForm();
        if (acroform != null) {
            // Acrobat reports an error while performing sanitization in XFA documents.
            if (acroform.hasXFA()) {
                throw new PDFInvalidDocumentException("This document has XFA content."
                                                          + " Shouldn't call sanitization on this.");
            }

            if (acroform.getNeedAppearances()) {
                AppearanceService.generateAppearances(document, null, null);
            }
        }

        return true;
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

    /**
     * An implementation of the RedactionHandler class. Most commonly used to receive callbacks when an object is
     * redacted
     */
    private static class LocalRedactionHandler implements RedactionHandler {

        /**
         * Returns true, if client wants the JPXDecoded image to be redacted. otherwise return false
         */
        @Override
        public boolean getColorSpaceBasedOnColorComponents(final int arg0, final PDFColorSpaceContainer arg1) {
            return false;
        }

        /**
         * A callback used when an object is redacted.
         */
        @Override
        public void objectRedacted(final RedactedObjectInfo redactedObject) {
            logger.info(redactedObject.toString());
        }

    }
}

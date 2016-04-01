/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.internal.io.ByteWriter;
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
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.IOException;
import java.net.URL;
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
 * <li>Review and comment data, including:
 * <ul>
 * <li>FreeText
 * <li>Highlights
 * <li>Text corrections
 * <li>Lines, polygons, and other shapes
 * <li>Ink
 * <li>Stamps
 * <li>Sticky notes
 * <li>Popups
 * <li>Comment history list
 * </ul>
 * <li>Hidden data from previous document saves
 * <li>Obscured text and images, including content that is partially obscured
 * <li>Comments hidden within the body of the PDF file
 * <li>Unreferenced data
 * <li>Links, actions and JavaScript
 * <li>Overlapping objects
 * </ul>
 */
public final class RedactAndSanitizeDocument {
    private static final Logger LOGGER = Logger.getLogger(RedactAndSanitizeDocument.class.getName());

    public static final String SEARCH_PDF_STRING = "Reader";
    public static final String INPUT_PDF_PATH = "pdfjavatoolkit-ds.pdf";
    public static final String OUTPUT_PDF_PATH = "RedactAndSanitize.pdf";

    private static final double[] COLOR = { 1.0, 0, 0 }; // RGB Red
    private static final double[] INTERIOR_COLOR = { 0, 0, 0 }; // RGB Black

    /**
     * This is a utility class, and won't be instantiated.
     */

    private RedactAndSanitizeDocument() {}

    /**
     * Main program.
     *
     * @param args Three command line arguments - input URL, output URL and a search string.
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String searchString = null;
        URL inputUrl = null;
        URL outputUrl = null;


        if (args.length > 2) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            outputUrl = IoUtils.createUrlFromPath(args[1]);
            searchString = args[2];
        } else {
            inputUrl = RedactAndSanitizeDocument.class.getResource(INPUT_PDF_PATH);
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PDF_PATH);
            searchString = SEARCH_PDF_STRING;
        }

        redactAndSanitize(inputUrl, outputUrl, searchString);
    }

    /**
     * Redacts and sanitizes a document that's passed in.
     *
     * @param inputUrl input URL of the PDF document
     * @param outputUrl output URL of the PDF document
     * @param redactionString The text to be redacted
     * @throws Exception a general exception was thrown
     */
    public static void redactAndSanitize(final URL inputUrl, final URL outputUrl, final String redactionString)
                    throws Exception {
        PDFDocument document = null;
        try {
            document = DocumentUtils.openPdfDocument(inputUrl);

            markTextForRedaction(document, redactionString);

            applyRedaction(document, outputUrl);

        } finally {
            if (document != null) {
                document.close();
            }
        }

        try {
            document = DocumentUtils.openPdfDocument(outputUrl);

            sanitizeDocument(document, outputUrl);
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
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
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
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
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
        annot.setColor(COLOR);
        annot.setInteriorColor(INTERIOR_COLOR);

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
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     *         state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws IOException an I/O operation failed or was interrupted
     */
    private static void applyRedaction(final PDFDocument document, final URL outputUrl)
                    throws PDFInvalidParameterException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFUnableToCompleteOperationException, PDFFontException, IOException {
        ByteWriter writer = null;
        try {
            writer = IoUtils.newByteWriter(outputUrl);
            RedactionOptions redactionOptions = null;
            redactionOptions = new RedactionOptions(new LocalRedactionHandler());

            // Applying redaction
            RedactionService.applyRedaction(document, redactionOptions, writer);
            writer.close();
        } catch (final IOException e) {
            throw new PDFIOException(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Apply sanitization to a document.
     *
     * @param document The document to be sanitized
     * @param sanitizedUrl The sanitized output document
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    public static void sanitizeDocument(final PDFDocument document, final URL sanitizedUrl)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, PDFFontException,
                    PDFConfigurationException, PDFInvalidParameterException, PDFUnableToCompleteOperationException {

        if (!canSanitizeDocument(document)) {
            LOGGER.warning("The document was not sanitized");
            return;
        }
        ByteWriter writer = null;
        try {
            writer = IoUtils.newByteWriter(sanitizedUrl);
        } catch (final IOException e) {
            throw new PDFIOException(e);
        }
        final PDFSaveOptions saveOptions = PDFSaveLinearOptions.newInstance();
        // Optimize the document for fast web viewing. This is a part of sanitization.
        saveOptions.setForceCompress(true);// All the streams should be encoded with flate filter.
        final SanitizationOptions options = new SanitizationOptions();
        options.setPDFFontSet(FontSetLoader.newInstance().getFontSet());
        options.setSaveOptions(saveOptions);
        SanitizationService.sanitizeDocument(document, options, writer);// API to start the sanitization.
    }

    /**
     * Checks if a PDF document can/should be sanitized. Throws an error if it can't/shouldn't be sanitized.
     *
     * @param document A PDF document, to check if it can be sanitized
     * @return Returns true of the document can/should be sanitized
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     */
    private static boolean canSanitizeDocument(final PDFDocument document)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, PDFFontException,
                    PDFConfigurationException, PDFInvalidParameterException {
        if (document.requireCatalog().getCollection() != null) {
            return false;
        }

        final SignatureManager sigMgr = SignatureManager.newInstance(document);

        if (sigMgr.hasSignedSignatureFields() || sigMgr.hasUsageRights()) {
            return false;
        }

        final PDFInteractiveForm acroform = document.requireCatalog().getInteractiveForm();
        if (acroform != null) {
            // Acrobat reports an error while performing sanitization in XFA documents.
            if (acroform.hasXFA()) {
                return false;
            }

            if (acroform.getNeedAppearances()) {
                AppearanceService.generateAppearances(document, null, null);
            }
        }

        return true;
    }

    /**
     * Generate appearance streams for redaction annotations.
     *
     * @param document The document that needs appearances generated for it's redaction annotations
     * @param docFontSet A fontset with the appropriate fonts added from the PDFDocument
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
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
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
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
            LOGGER.info(redactedObject.toString());
        }

    }
}

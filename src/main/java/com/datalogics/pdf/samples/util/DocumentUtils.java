/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectForm;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.fontresources.PDFFontSetUtil;
import com.adobe.pdfjt.services.pdfParser.ContentStreamParser;
import com.adobe.pdfjt.services.rasterizer.RasterizationOptions;
import com.adobe.pdfjt.services.rasterizer.impl.DefaultCallBackHandler;
import com.adobe.pdfjt.services.rasterizer.impl.RasterContentItem;
import com.adobe.pdfjt.services.rasterizer.impl.RasterDisplayArea;
import com.adobe.pdfjt.services.rasterizer.impl.RasterDocument;
import com.adobe.pdfjt.services.rasterizer.impl.RasterGraphicsState;
import com.adobe.pdfjt.services.rasterizer.impl.RasterTextState;

import org.apache.commons.collections4.IteratorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * A utility class that contains some commonly used document methods.
 */
public final class DocumentUtils {

    /**
     * This is a utility class, and won't be instantiated.
     */
    private DocumentUtils() {}

    /**
     * Open a PDF file using an input path.
     *
     * @param inputPath The PDF resource to open
     * @return A new {@link PDFDocument} instance of the input document
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static PDFDocument openPdfDocument(final String inputPath)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {
        ByteReader reader = null;
        PDFDocument document = null;

        final InputStream inputStream = DocumentUtils.class.getResourceAsStream(inputPath);
        reader = new InputStreamByteReader(inputStream);
        document = PDFDocument.newInstance(reader, PDFOpenOptions.newInstance());

        return document;
    }

    /**
     * Open a PDF file using an input path.
     *
     * @param inputUrl The URL to a PDF file to open
     * @return A new PDFDocument instance of the input document
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static PDFDocument openPdfDocument(final URL inputUrl)
                    throws IOException, PDFInvalidDocumentException, PDFIOException, PDFSecurityException {
        final PDFOpenOptions pdfOpenOptions = PDFOpenOptions.newInstance();

        return openPdfDocumentWithOptions(inputUrl, pdfOpenOptions);
    }

    /**
     * Open a PDF file using an input path.
     *
     * @param inputUrl The URL to a PDF file to open
     * @param pdfOpenOptions Options used to open a PDF document
     * @return A new PDFDocument instance of the input document
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static PDFDocument openPdfDocumentWithOptions(final URL inputUrl, final PDFOpenOptions pdfOpenOptions)
                    throws IOException, PDFInvalidDocumentException, PDFIOException, PDFSecurityException {
        ByteReader reader = null;
        PDFDocument document = null;

        final InputStream inputStream = inputUrl.openStream();
        reader = new InputStreamByteReader(inputStream);
        document = PDFDocument.newInstance(reader, pdfOpenOptions);

        return document;
    }

    /**
     * Get a working {@link PDFFontSet} for the document.
     *
     * @param document the document to get the font set for
     * @return the font set
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    public static PDFFontSet getDocumentFontSet(final PDFDocument document)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException {
        return PDFFontSetUtil.buildWorkingFontSet(document, null, document.getDocumentLocale(), null);
    }

    /**
     * Parse the given form and return a list of content items.
     *
     * <p>
     * This list of content items can be used to analyze the graphical content of the page, without having to work
     * directly with the content stream.
     *
     * @param page a page to use for parsing context (for optional content, etc.)
     * @param form the form to obtain the contents of
     * @param pdfFontSet the font set to use; if this is null, then the font set will be obtained with
     *        {@link DocumentUtils#getDocumentFontSet(PDFDocument)}
     * @return a list of the content items
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     */
    public static List<RasterContentItem> getFormContentItems(final PDFPage page, final PDFXObjectForm form,
                                                              final PDFFontSet pdfFontSet)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException,
                    PDFInvalidParameterException, PDFFontException, PDFConfigurationException {
        if (page == null) {
            throw new PDFInvalidParameterException("PDF page can not be null.");
        }
        if (form == null) {
            throw new PDFInvalidParameterException("form can not be null.");
        }
        final RasterizationOptions rasterOptions = new RasterizationOptions();
        final RasterDocument rasterDocument = new RasterDocument();
        rasterOptions.registerCallBackManager(new DefaultCallBackHandler());

        final PDFFontSet fontSet = pdfFontSet == null ? getDocumentFontSet(form.getPDFDocument()) : pdfFontSet;

        //@formatter:off
        final ContentStreamParser<RasterGraphicsState, RasterContentItem, RasterTextState, RasterDisplayArea> csParser =
            new ContentStreamParser<>(
                rasterDocument,
                fontSet,
                null,
                rasterOptions.processAnnotations(),
                null,
                false);
        //@formatter:on

        final RasterDisplayArea displayArea = csParser.processObjectsInXObjectForm(page, form);

        // Display area's content items don't have a way to get a size, or access to the internal list, so
        // use the iterator to make a manageable list.
        return IteratorUtils.toList(displayArea.getContentItems().iterator());
    }
}

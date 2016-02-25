/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.exceptions.PDFUnsupportedFeatureException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationEnum;
import com.adobe.pdfjt.services.ap.spi.APContext;
import com.adobe.pdfjt.services.ap.spi.APResources;
import com.adobe.pdfjt.services.digsig.SignatureManager;
import com.adobe.pdfjt.services.formflattener.FormFlattener;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;

/**
 * This sample demonstrates how to use the flattening functions to flatten the interactive elements of a PDF. The
 * flattening functionality currently handles forms and annotations. All flattening is done through the FormFlattener's
 * flattenDocument() method, and specific element types can be flattened or ignored by specifying them in the
 * setAnnotationsToBeProcessed() method of the APContext passed in. Note that PDFJT does not currently flatten
 * transparency.
 *
 * <p>
 * Below is a list of annotation types, and whether they get flattened or not.
 * <ul>
 * <li>Text - flattened</li>
 * <li>Link - flattened</li>
 * <li>Line - flattened</li>
 * <li>Square - flattened</li>
 * <li>Circle - flattened</li>
 * <li>Polygon - flattened</li>
 * <li>PolyLine - flattened</li>
 * <li>Highlight - flattened</li>
 * <li>Underline - flattened</li>
 * <li>Squiggly - flattened</li>
 * <li>StrikeOut - flattened</li>
 * <li>Stamp - flattened</li>
 * <li>Popup - not flattened</li>
 * <li>FileAttachment - not flattened</li>
 * <li>Sound - flattened</li>
 * <li>Movie - flattened</li>
 * <li>3D - flattened</li>
 * </ul>
 * </p>
 */
public final class FlattenPdf {

    public static final String OUTPUT_FLATTENED_PDF_PATH = "Flattened.pdf";
    public static final String INPUT_PDF_PATH = "FormDocument.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private FlattenPdf() {}

    /**
     * Main program.
     *
     * @param args command line arguments
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
        if (args.length > 0) {
            inputUrl = new File(args[0]).toURI().toURL();
            outputUrl = new File(args[1]).toURI().toURL();
        } else {
            inputUrl = ConvertPdfDocument.class.getResource(INPUT_PDF_PATH);
            outputUrl = new File(OUTPUT_FLATTENED_PDF_PATH).toURI().toURL();
        }
        flattenPdf(inputUrl, outputUrl);
    }

    /**
     * Flatten a PDF.
     *
     * @param inputUrl the path to read the input PDF from
     * @param outputUrl the path to output the pdf
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     * @throws PDFUnsupportedFeatureException the requested feature is not currently supported
     * @throws PDFFontException there was an error in the font set or an individual font
     */
    public static void flattenPdf(final URL inputUrl, final URL outputUrl)
                    throws IOException, PDFIOException, PDFSecurityException, PDFInvalidDocumentException,
                    PDFFontException, PDFUnsupportedFeatureException, PDFConfigurationException,
                    PDFInvalidParameterException, PDFUnableToCompleteOperationException {

        final APResources apResources = new APResources(null, null, null);
        final APContext apContext = new APContext(apResources, true, null);

        // This sample flattens all elements in the given PDF. However, specific elements can be included or excluded
        // by setting the enum of the desired element rather than PDFAnnotationEnum.class.
        apContext.setAnnotationsToBeProcessed(EnumSet.allOf(PDFAnnotationEnum.class));

        PDFDocument pdfDoc = null;

        try {
            pdfDoc = DocumentUtils.openPdfDocument(inputUrl);

            // Flatten the signature fields so they can no longer be changed. If you do not want the signature fields
            // to be flattened the the next two lines should be omitted.
            final SignatureManager sigMgr = SignatureManager.newInstance(pdfDoc);
            sigMgr.flattenAllSignatureFields();

            // Flatten all the pages of the given input PDF Document, with the
            // default text formatter.
            FormFlattener.flattenDocument(apContext, pdfDoc, null);

            // Save the flattened file to an output PDF file
            DocumentHelper.saveFullAndClose(pdfDoc, outputUrl.toURI().getPath());
        } catch (final URISyntaxException e) {
            throw new PDFIOException(e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }

}

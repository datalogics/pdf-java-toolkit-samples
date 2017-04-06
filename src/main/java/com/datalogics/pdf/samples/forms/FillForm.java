/*
 * Copyright 2016 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFDocument.PDFDocumentType;
import com.adobe.pdfjt.services.xfa.XFAService;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.net.URL;
import java.util.Locale;

/**
 * This sample will demonstrate how to fill different types of PDF forms. For Acroforms, FDF and XFDF form data formats
 * are accepted. For XFA Forms, only XML form data files are accepted.
 */
public final class FillForm {
    // These sample files demonstrate filling an Acroform with FDF form data.
    public static final String ACROFORM_FDF_INPUT = "acroform_fdf.pdf";
    public static final String ACROFORM_FDF_DATA = "acroform_fdf.fdf";
    public static final String ACROFORM_FDF_OUTPUT = "acroform_fdf_output.pdf";

    // These sample files demonstrate filling an Acroform with XFDF form data. The output will contain barcodes.
    public static final String ACROFORM_XFDF_INPUT = "acroform_xfdf.pdf";
    public static final String ACROFORM_XFDF_DATA = "acroform_xfdf.xfdf";
    public static final String ACROFORM_XFDF_OUTPUT = "acroform_xfdf_output.pdf";

    // These sample files demonstrate filling an XFA form with XML form data.
    public static final String XFA_PDF_INPUT = "xfa_form.pdf";
    public static final String XFA_XML_DATA = "xfa_data.xml";
    public static final String XFA_OUTPUT = "xfa_output.pdf";

    // Some XML constants
    public static final String XFA_DATA_NS_URI = "http://www.xfa.org/schema/xfa-data/1.0/";
    public static final String XFA_DATA_ROOT_NODE = "xfa:datasets";
    public static final String XFA_DATA_CHILD_NODE = "xfa:data";

    // Accepted formats
    public static final String XML_FORMAT = "XML";
    public static final String FDF_FORMAT = "FDF";
    public static final String XFDF_FORMAT = "XFDF";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private FillForm() {}

    /**
     * Main program for filling PDF forms.
     *
     * @param args The input PDF, the input form, and the output PDF
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        // If we've been given enough arguments, get the input PDF, the input form data file, and the name of the output
        // file. Try to parse the form data file type.
        if (args.length > 2) {
            final URL inputForm = IoUtils.createUrlFromPath(args[1]);

            final String format = IoUtils.getFileExtensionFromUrl(inputForm);
            if (XML_FORMAT.equalsIgnoreCase(format)
                || FDF_FORMAT.equalsIgnoreCase(format)
                || XFDF_FORMAT.equalsIgnoreCase(format)) {
                fillPdfForm(IoUtils.createUrlFromPath(args[0]), inputForm, format.toUpperCase(Locale.US),
                            IoUtils.createUrlFromPath(args[2]));
            } else {
                throw new IllegalArgumentException("Form data format of " + format
                                                   + " is not supported. Supported types: XML, FDF, and XFDF.");
            }
        } else {
            final Class<FillForm> classReference = FillForm.class;
            fillPdfForm(classReference.getResource(ACROFORM_FDF_INPUT), classReference.getResource(ACROFORM_FDF_DATA),
                        FDF_FORMAT, IoUtils.createUrlFromPath(ACROFORM_FDF_OUTPUT));
            fillPdfForm(classReference.getResource(ACROFORM_XFDF_INPUT),
                        classReference.getResource(ACROFORM_XFDF_DATA), XFDF_FORMAT,
                        IoUtils.createUrlFromPath(ACROFORM_XFDF_OUTPUT));
            fillPdfForm(classReference.getResource(XFA_PDF_INPUT), classReference.getResource(XFA_XML_DATA),
                        XML_FORMAT, IoUtils.createUrlFromPath(XFA_OUTPUT));
        }
    }

    /**
     * Fill a PDF form with the provided data.
     *
     * @param inputFormUrl The form to be filled
     * @param inputDataUrl The data with which to fill the form
     * @param formType The type of form passed in
     * @param outputUrl The file to which the filled form will be saved
     * @throws Exception a general exception was thrown
     */
    public static void fillPdfForm(final URL inputFormUrl, final URL inputDataUrl, final String formType,
                                   final URL outputUrl)
                    throws Exception {
        final PDFDocument pdfDocument = DocumentUtils.openPdfDocument(inputFormUrl);

        // There are two types of forms that we can fill, so find out which kind we have here.
        final PDFDocumentType documentType = XFAService.getDocumentType(pdfDocument);

        if (documentType == PDFDocumentType.Acroform) {
            // If this is an Acroform, make sure the form data is either FDF for XFDF.
            if (FDF_FORMAT.equalsIgnoreCase(formType)) {
                fillAcroformFdf(pdfDocument, inputDataUrl, outputUrl);
            } else if (XFDF_FORMAT.equalsIgnoreCase(formType)) {
                fillAcroformXfdf(pdfDocument, inputDataUrl, outputUrl);
            } else {
                throw new IllegalArgumentException("Invalid formData type for Acroform document. "
                                                   + "FDF and XFDF supported.");
            }
        } else if (documentType.isXFA()) {
            // If the document has an XFA form, make sure that we were passed an XML data file.
            // Note that PDF Java Toolkit doesn't support generating appearances or running calculations on XFA forms
            // (though field formatting is supported), so be sure to use Acrobat or another full-featured PDF viewer
            // to verify the output. A viewer like OSX's Preview won't cut it.
            if (XML_FORMAT.equalsIgnoreCase(formType)) {
                fillXfa(pdfDocument, inputDataUrl, outputUrl);
            } else {
                throw new IllegalArgumentException("Invalid formData type for XFA document. XML supported.");
            }
        } else {
            // If the form document is not XFA or Acroform, it's either not a form or it's something we don't support.
            throw new IllegalArgumentException("PDF File does not contain an AcroForm or an XFA form.");
        }
    }

    /**
     * Fill an Acroform with FDF form data.
     *
     * @param pdfDocument The form to be filled
     * @param inputDataUrl The data with which to fill the form
     * @param outputUrl The file to which the filled form will be saved
     * @throws Exception a general exception was thrown
     */
    public static void fillAcroformFdf(final PDFDocument pdfDocument, final URL inputDataUrl, final URL outputUrl)
                    throws Exception {
        FormImporter.fillAcroformFdf(pdfDocument, inputDataUrl, outputUrl);
    }

    /**
     * Fill an Acroform with XFDF form data.
     *
     * @param pdfDocument The form to be filled
     * @param inputDataUrl The data with which to fill the form
     * @param outputUrl The file to which the filled form will be saved
     * @throws Exception a general exception was thrown
     */
    public static void fillAcroformXfdf(final PDFDocument pdfDocument, final URL inputDataUrl, final URL outputUrl)
                    throws Exception {
        FormImporter.fillAcroformXfdf(pdfDocument, inputDataUrl, outputUrl);
    }

    /**
     * Fill an XFA form with XML form data. Will not generate appearances or run calculations on the form.
     *
     * @param pdfDocument The form to be filled
     * @param inputDataUrl The data with which to fill the form
     * @param outputUrl The file to which the filled form will be saved
     * @throws Exception a general exception was thrown
     */
    public static void fillXfa(final PDFDocument pdfDocument, final URL inputDataUrl, final URL outputUrl)
                    throws Exception {
        FormImporter.fillXfa(pdfDocument, inputDataUrl, outputUrl);
    }

}

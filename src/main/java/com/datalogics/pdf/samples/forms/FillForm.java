/*
 * Copyright 2016 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import static com.datalogics.pdf.samples.forms.FormImporter.FormType.FDF;
import static com.datalogics.pdf.samples.forms.FormImporter.FormType.UNKNOWN;
import static com.datalogics.pdf.samples.forms.FormImporter.FormType.XFDF;
import static com.datalogics.pdf.samples.forms.FormImporter.FormType.XML;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;

import com.datalogics.pdf.samples.util.IoUtils;

import java.net.URL;

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

            final FormImporter.FormType formType = FormImporter.FormType.valueOf(inputForm);

            final String format = IoUtils.getFileExtensionFromUrl(inputForm);
            if (formType == UNKNOWN) {
                throw new IllegalArgumentException("Form data format of " + format
                                                   + " is not supported. Supported types: XML, FDF, and XFDF.");
            } else {
                fillPdfForm(IoUtils.createUrlFromPath(args[0]), inputForm, formType,
                            IoUtils.createUrlFromPath(args[2]));
            }
        } else {
            final Class<FillForm> classReference = FillForm.class;
            fillPdfForm(classReference.getResource(ACROFORM_FDF_INPUT), classReference.getResource(ACROFORM_FDF_DATA),
                        FDF, IoUtils.createUrlFromPath(ACROFORM_FDF_OUTPUT));
            fillPdfForm(classReference.getResource(ACROFORM_XFDF_INPUT),
                        classReference.getResource(ACROFORM_XFDF_DATA), XFDF,
                        IoUtils.createUrlFromPath(ACROFORM_XFDF_OUTPUT));
            fillPdfForm(classReference.getResource(XFA_PDF_INPUT), classReference.getResource(XFA_XML_DATA), XML,
                        IoUtils.createUrlFromPath(XFA_OUTPUT));
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
    public static void fillPdfForm(final URL inputFormUrl, final URL inputDataUrl,
                                   final FormImporter.FormType formType, final URL outputUrl)
                    throws Exception {
        FormImporter.fillPdfForm(inputFormUrl, inputDataUrl, formType, outputUrl);
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

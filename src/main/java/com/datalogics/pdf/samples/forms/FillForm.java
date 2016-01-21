/*
 * Copyright 2016 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFDocument.PDFDocumentType;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.services.ap.AppearanceService;
import com.adobe.pdfjt.services.fdf.FDFDocument;
import com.adobe.pdfjt.services.fdf.FDFService;
import com.adobe.pdfjt.services.forms.FormFieldService;
import com.adobe.pdfjt.services.xfa.XFAService;
import com.adobe.pdfjt.services.xfa.XFAService.XFAElement;
import com.adobe.pdfjt.services.xfdf.XFDFService;

import com.datalogics.pdf.document.DocumentHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
     * @throws Exception Throws a general exception
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
            final String inputForm = args[1];
            final String[] split = inputForm.split("\\.");
            final String format = split[split.length - 1];
            if (XML_FORMAT.equalsIgnoreCase(format)
                || FDF_FORMAT.equalsIgnoreCase(format)
                || XFDF_FORMAT.equalsIgnoreCase(format)) {
                fillPdfForm(args[0], inputForm, format.toUpperCase(Locale.US), args[2]);
            } else {
                throw new IllegalArgumentException("Form data format of " + format
                                                   + " is not supported. Supported types: XML, FDF, and XFDF.");
            }
        } else {
            fillPdfForm(ACROFORM_FDF_INPUT, ACROFORM_FDF_DATA, FDF_FORMAT, ACROFORM_FDF_OUTPUT);
            fillPdfForm(ACROFORM_XFDF_INPUT, ACROFORM_XFDF_DATA, XFDF_FORMAT, ACROFORM_XFDF_OUTPUT);
            fillPdfForm(XFA_PDF_INPUT, XFA_XML_DATA, XML_FORMAT, XFA_OUTPUT);
        }
    }

    /**
     * Fill a PDF form with the provided data.
     *
     * @param pdf The form to be filled
     * @param form The data with which to fill the form
     * @param formType The type of form passed in
     * @param output The file to which the filled form will be saved
     * @throws Exception Throws a general exception
     */
    public static void fillPdfForm(final String pdf, final String form, final String formType, final String output)
                    throws Exception {
        final PDFDocument pdfDocument = openPdfDocument(pdf);

        // There are two types of forms that we can fill, so find out which kind we have here.
        final PDFDocumentType documentType = XFAService.getDocumentType(pdfDocument);

        if (documentType == PDFDocumentType.Acroform) {
            // If this is an Acroform, make sure the form data is either FDF for XFDF.
            if (FDF_FORMAT.equalsIgnoreCase(formType)) {
                fillAcroformFdf(pdfDocument, form, output);
            } else if (XFDF_FORMAT.equalsIgnoreCase(formType)) {
                fillAcroformXfdf(pdfDocument, form, output);
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
                fillXfa(pdfDocument, form, output);
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
     * @param form The data with which to fill the form
     * @param output The file to which the filled form will be saved
     * @throws Exception Throws a general exception
     */
    public static void fillAcroformFdf(final PDFDocument pdfDocument, final String form, final String output)
                    throws Exception {

        // Open the input form data file.
        InputStream formStream = FillForm.class.getResourceAsStream(form);
        if (formStream == null) {
            formStream = new FileInputStream(form);
        }
        final ByteReader formByteReader = new InputStreamByteReader(formStream);
        final FDFDocument fdfDocument = FDFDocument.newInstance(formByteReader);

        // Use the FDFService to get the form data into the PDF.
        final FDFService fdfService = new FDFService(pdfDocument);
        fdfService.importForm(fdfDocument);

        // Run calculations on the AcroForm...
        FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();
        // Run formatting...
        FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
        // And generate appearances.
        AppearanceService.generateAppearances(pdfDocument, null, null);

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, output);
    }

    /**
     * Fill an Acroform with XFDF form data.
     *
     * @param pdfDocument The form to be filled
     * @param form The data with which to fill the form
     * @param output The file to which the filled form will be saved
     * @throws Exception Throws a general exception
     */
    public static void fillAcroformXfdf(final PDFDocument pdfDocument, final String form, final String output)
                    throws Exception {

        // If this is XFDF form data, fill the form using the XFDFService, which uses a slightly different
        // process than the FDFService. Just get the data file into an InputStream, then import the data into the PDF
        // document.
        InputStream formStream = FillForm.class.getResourceAsStream(form);
        if (formStream == null) {
            formStream = new FileInputStream(form);
        }
        XFDFService.importFormData(pdfDocument, formStream);

        // Run calculations on the AcroForm...
        FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();
        // Run formatting...
        FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
        // And generate appearances.
        AppearanceService.generateAppearances(pdfDocument, null, null);

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, output);
    }

    /**
     * Fill an XFA form with XML form data. Will not generate appearances or run calculations on the form.
     *
     * @param pdfDocument The form to be filled
     * @param form The data with which to fill the form
     * @param output The file to which the filled form will be saved
     * @throws Exception Throws a general exception
     */
    public static void fillXfa(final PDFDocument pdfDocument, final String form, final String output)
                    throws Exception {

        // Start by getting the form data into an InputStream.
        InputStream formStream = FillForm.class.getResourceAsStream(form);
        if (formStream == null) {
            final File formFile = new File(form);
            // For robustness's sake, we'll check that document looks about how we expect it to. If it doesn't, we'll
            // add some extra information to make it more compatible. These two functions just do Java XML stuff and
            // don't require any special knowledge of PDF Java Toolkit.
            if (!hasXfaHeaders(formFile)) {
                addXfaHeaders(formFile);
            }
            formStream = new FileInputStream(formFile);
        }

        // Once we have an XML file with the proper header, use the XFAService to get the data into the PDF.
        XFAService.importElement(pdfDocument, XFAElement.DATASETS, formStream);

        // Just save the file. Generating appearances and running calculations aren't supported for XFA forms, so
        // there's no need to try it.
        DocumentHelper.saveFullAndClose(pdfDocument, output);
    }

    /**
     * Open a PDF file using an input path.
     *
     * @param inputPath The PDF file to open
     * @return A new PDFDocument instance of the input document
     * @throws Exception Throws a general exception
     */
    public static PDFDocument openPdfDocument(final String inputPath) throws Exception {

        ByteReader reader = null;
        PDFDocument document = null;

        InputStream inputStream = FillForm.class.getResourceAsStream(inputPath);
        if (inputStream == null) {
            inputStream = new FileInputStream(new File(inputPath));
        }

        reader = new InputStreamByteReader(inputStream);
        document = PDFDocument.newInstance(reader, PDFOpenOptions.newInstance());

        return document;
    }

    /**
     * Determine if the first element is the XFA-data root element as expected.
     *
     * @param xfaData - a file containing XML form data
     * @return a boolean indicating the presence of XFA header tags.
     * @throws Exception - SAX, IO, ParserConfiguration
     */
    private static boolean hasXfaHeaders(final File xfaData) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document xmlDoc = builder.parse(xfaData);
        final String xmlRootName = xmlDoc.getDocumentElement().getNodeName();

        return xmlRootName.equals(XFA_DATA_ROOT_NODE);
    }

    /**
     * Add XFA headers to an XML data file. This should only be called after determining that the headers aren't there.
     *
     * @param xfaData - XML data that XFA headers will be added to
     * @throws Exception - IO, ParserConfiguration, Transformer, etc.
     */
    private static void addXfaHeaders(final File xfaData) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document oldDoc = builder.parse(xfaData);
        final Node oldRoot = oldDoc.getDocumentElement();
        final Document newDoc = builder.newDocument();
        final Element newRoot = newDoc.createElementNS(XFA_DATA_NS_URI, XFA_DATA_ROOT_NODE);

        newDoc.appendChild(newRoot);
        final Node dataNode = newRoot.appendChild(newDoc.createElement(XFA_DATA_ROOT_NODE));
        dataNode.appendChild(newDoc.importNode(oldRoot, true));

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Result xmlFile = new StreamResult(xfaData);
        final Source newXml = new DOMSource(newDoc);
        transformer.transform(newXml, xmlFile);

    }
}

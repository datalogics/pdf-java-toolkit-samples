/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
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
 * This sample will demonstrate how to fill different types of PDF forms.
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
     * @throws Exception Throws a general exception
     */
    public static void main(final String... args) throws Exception {

        if (args.length > 2) {
            final String inputForm = args[1];
            final String[] split = inputForm.split("\\.");
            final String format = split[split.length - 1];
            if ("XML".equalsIgnoreCase(format)
                || "FDF".equalsIgnoreCase(format)
                || "XFDF".equalsIgnoreCase(format)) {
                fillPdfForm(args[0], inputForm, format.toUpperCase(Locale.US), args[2]);
            } else {
                throw new IllegalArgumentException("Form data format of " + format + " is not supported");
            }
        } else {
            fillPdfForm(ACROFORM_FDF_INPUT, ACROFORM_FDF_DATA, "FDF", ACROFORM_FDF_OUTPUT);
            fillPdfForm(ACROFORM_XFDF_INPUT, ACROFORM_XFDF_DATA, "XFDF", ACROFORM_XFDF_OUTPUT);
            fillPdfForm(XFA_PDF_INPUT, XFA_XML_DATA, "XML", XFA_OUTPUT);
        }
    }

    /**
     * Fill a PDF form with provided form data.
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

            if ("FDF".equalsIgnoreCase(formType)) {
                // If this is an Acroform and the data file is FDF, use the FDFService to fill the form.
                final FDFService fdfService = new FDFService(pdfDocument);
                InputStream formStream = FillForm.class.getResourceAsStream(form);
                if (formStream == null) {
                    formStream = new FileInputStream(form);
                }
                final ByteReader formByteReader = new InputStreamByteReader(formStream);
                final FDFDocument fd = FDFDocument.newInstance(formByteReader);
                fdfService.importForm(fd);
                // Run calculations on the AcroForm.
                FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();
                // Run formatting on the AcroForm.
                FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
                // Generate appearances on the AcroForm.
                AppearanceService.generateAppearances(pdfDocument, null, null);
                // Save the file.
                DocumentHelper.saveFullAndClose(pdfDocument, output);
                System.out.println("Successfully saved a form with FDF data");
            } else if ("XFDF".equalsIgnoreCase(formType)) {
                // If this is XFDF form data, fill the form using the XFDFService, which uses a slightly different
                // process than the FDFService
                InputStream formStream = FillForm.class.getResourceAsStream(form);
                if (formStream == null) {
                    formStream = new FileInputStream(form);
                }
                XFDFService.importFormData(pdfDocument, formStream);
                // Run calculations on the AcroForm.
                FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();
                // Run formatting on the AcroForm.
                FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
                // Generate appearances on the AcroForm.
                AppearanceService.generateAppearances(pdfDocument, null, null);
                // Save the file.
                DocumentHelper.saveFullAndClose(pdfDocument, output);
                System.out.println("Successfully saved a form with XFDF data");
            } else {
                throw new IllegalArgumentException("Invalid formData type for Acroform document.");
            }
        } else if (documentType.isXFA()) {
            // If the document has an XFA form, make sure that we were passed an XML data file.
            if ("XML".equalsIgnoreCase(formType)) {
                InputStream formStream = FillForm.class.getResourceAsStream(form);
                if (formStream == null) {
                    final File formFile = new File(form);
                    if (!hasXfaHeaders(formFile)) {
                        addXfaHeaders(formFile);
                    }
                    formStream = new FileInputStream(formFile);
                }
                XFAService.importElement(pdfDocument, XFAElement.DATASETS, formStream);
                // Run calculations on the AcroForm.
                FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();
                // Run formatting on the AcroForm.
                FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
                // Generate appearances on the AcroForm.
                AppearanceService.generateAppearances(pdfDocument, null, null);
                DocumentHelper.saveFullAndClose(pdfDocument, output);
                System.out.println("Successfully saved a form with XML data");
            } else {
                throw new IllegalArgumentException("Invalid formData type for XFA document.");
            }
        } else {
            // If the form document is not XFA or Acroform, it's either not a form or it's something we don't support.
            throw new IllegalArgumentException("PDF File does not contain an AcroForm or an XFA form.");
        }
    }

    /**
     * Open a PDF file using an input path.
     *
     * @param inputPath The PDF file to open
     * @return A new PDFDocument instance of the input document
     * @throws Exception Throws a general exception
     */
    private static PDFDocument openPdfDocument(final String inputPath) throws Exception {
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
     * This method determines if the first element is the xfa-data root element expected.
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

        return xmlRootName.equals("xfa:datasets");
    }

    /**
     * If the XML data file does not contain xfa headers, this method is called to add them
     *
     * @param xfaData - xml data that xfa headers will be added to
     * @throws Exception - IO, ParserConfiguration, Transformer, etc.
     */
    private static void addXfaHeaders(final File xfaData) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document oldDoc = builder.parse(xfaData);
        final Node oldRoot = oldDoc.getDocumentElement();
        final Document newDoc = builder.newDocument();
        final Element newRoot = newDoc.createElementNS("http://www.xfa.org/schema/xfa-data/1.0/", "xfa:datasets");

        newDoc.appendChild(newRoot);
        final Node dataNode = newRoot.appendChild(newDoc.createElement("xfa:data"));
        dataNode.appendChild(newDoc.importNode(oldRoot, true));

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Result xmlFile = new StreamResult(xfaData);
        final Source newXml = new DOMSource(newDoc);
        transformer.transform(newXml, xmlFile);

    }
}

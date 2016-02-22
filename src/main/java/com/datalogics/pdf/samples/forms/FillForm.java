/*
 * Copyright 2016 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFDocument.PDFDocumentType;
import com.adobe.pdfjt.services.ap.AppearanceService;
import com.adobe.pdfjt.services.fdf.FDFDocument;
import com.adobe.pdfjt.services.fdf.FDFService;
import com.adobe.pdfjt.services.forms.FormFieldService;
import com.adobe.pdfjt.services.xfa.XFAService;
import com.adobe.pdfjt.services.xfa.XFAService.XFAElement;
import com.adobe.pdfjt.services.xfdf.XFDFService;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
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
            final URL inputForm = new File(args[1]).toURI().toURL();

            final String format = IoUtils.getFileExtensionFromUrl(inputForm);
            if (XML_FORMAT.equalsIgnoreCase(format)
                || FDF_FORMAT.equalsIgnoreCase(format)
                || XFDF_FORMAT.equalsIgnoreCase(format)) {
                fillPdfForm(new File(args[0]).toURI().toURL(), inputForm, format.toUpperCase(Locale.US),
                            new File(args[2]).toURI().toURL());
            } else {
                throw new IllegalArgumentException("Form data format of " + format
                                                   + " is not supported. Supported types: XML, FDF, and XFDF.");
            }
        } else {
            final Class<FillForm> classReference = FillForm.class;
            fillPdfForm(classReference.getResource(ACROFORM_FDF_INPUT), classReference.getResource(ACROFORM_FDF_DATA),
                        FDF_FORMAT, new File(ACROFORM_FDF_OUTPUT).toURI().toURL());
            fillPdfForm(classReference.getResource(ACROFORM_XFDF_INPUT),
                        classReference.getResource(ACROFORM_XFDF_DATA), XFDF_FORMAT,
                        new File(ACROFORM_XFDF_OUTPUT).toURI().toURL());
            fillPdfForm(classReference.getResource(XFA_PDF_INPUT), classReference.getResource(XFA_XML_DATA),
                        XML_FORMAT, new File(XFA_OUTPUT).toURI().toURL());
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

        // Open the input form data file.
        final InputStream formStream = inputDataUrl.openStream();
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
        DocumentHelper.saveFullAndClose(pdfDocument, outputUrl.toURI().getPath());
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

        // If this is XFDF form data, fill the form using the XFDFService, which uses a slightly different
        // process than the FDFService. Just get the data file into an InputStream, then import the data into the PDF
        // document.
        final InputStream formStream = inputDataUrl.openStream();
        XFDFService.importFormData(pdfDocument, formStream);

        // Run calculations on the AcroForm...
        FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();
        // Run formatting...
        FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
        // And generate appearances.
        AppearanceService.generateAppearances(pdfDocument, null, null);

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, outputUrl.toURI().getPath());
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

        // Start by getting the form data into an InputStream.
        final InputStream formStream = inputDataUrl.openStream();

        // For robustness's sake, we'll check that document looks about how we expect it to. If it doesn't, we'll
        // add some extra information to make it more compatible. These two functions just do Java XML stuff and
        // don't require any special knowledge of PDF Java Toolkit.
        if (!hasXfaHeaders(inputDataUrl)) {
            addXfaHeaders(inputDataUrl);
        }

        // Once we have an XML file with the proper header, use the XFAService to get the data into the PDF.
        XFAService.importElement(pdfDocument, XFAElement.DATASETS, formStream);

        // Just save the file. Generating appearances and running calculations aren't supported for XFA forms, so
        // there's no need to try it.
        DocumentHelper.saveFullAndClose(pdfDocument, outputUrl.toURI().getPath());
    }

    /**
     * Determine if the first element is the XFA-data root element as expected.
     *
     * @param xfaData - a file containing XML form data
     * @return a boolean indicating the presence of XFA header tags.
     * @throws Exception a general exception was thrown
     */
    private static boolean hasXfaHeaders(final URL inputDataUrl) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document xmlDoc = builder.parse(inputDataUrl.openStream());
        final String xmlRootName = xmlDoc.getDocumentElement().getNodeName();

        return xmlRootName.equals(XFA_DATA_ROOT_NODE);
    }

    /**
     * Add XFA headers to an XML data file. This should only be called after determining that the headers aren't there.
     *
     * @param xfaData - XML data that XFA headers will be added to
     * @throws Exception a general exception was thrown
     */
    private static void addXfaHeaders(final URL inputDataUrl) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document oldDoc = builder.parse(inputDataUrl.openStream());
        final Node oldRoot = oldDoc.getDocumentElement();
        final Document newDoc = builder.newDocument();
        final Element newRoot = newDoc.createElementNS(XFA_DATA_NS_URI, XFA_DATA_ROOT_NODE);

        newDoc.appendChild(newRoot);
        final Node dataNode = newRoot.appendChild(newDoc.createElement(XFA_DATA_ROOT_NODE));
        dataNode.appendChild(newDoc.importNode(oldRoot, true));

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Result xmlFile = new StreamResult(inputDataUrl.openConnection().getOutputStream());
        final Source newXml = new DOMSource(newDoc);
        transformer.transform(newXml, xmlFile);

    }
}

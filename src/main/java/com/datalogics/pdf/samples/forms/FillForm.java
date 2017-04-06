/*
 * Copyright 2016 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.Version;
import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidXMLException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
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

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

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

        // Open the input form data file.
        final InputStream formStream = inputDataUrl.openStream();
        final ByteReader formByteReader = new InputStreamByteReader(formStream);
        final FDFDocument fdfDocument = FDFDocument.newInstance(formByteReader);

        // Use the FDFService to get the form data into the PDF.
        final FDFService fdfService = new FDFService(pdfDocument);
        fdfService.importForm(fdfDocument);

        finishAndSaveForm(pdfDocument, outputUrl);
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

        // Run calculations on the AcroForm...only required before PDFJT 4.5.0
        finishAndSaveForm(pdfDocument, outputUrl);
    }

    /**
     * Run scripts, generate appearances, and save document.
     *
     * @param pdfDocument the document to complete and save
     * @param outputUrl the URL to which to save the file
     *
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     * @throws PDFInvalidXMLException The XML passed to the method either directly or indirectly is invalid
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws URISyntaxException a string could not be parsed as a URI reference
     */
    private static void finishAndSaveForm(final PDFDocument pdfDocument, final URL outputUrl)
                    throws IOException, PDFInvalidDocumentException, PDFSecurityException, PDFIOException,
                    PDFInvalidParameterException, PDFInvalidXMLException, PDFConfigurationException,
                    PDFUnableToCompleteOperationException, PDFFontException, URISyntaxException {
        if (pdfjtIsBeforeVersion4()) {
            // Run calculations scripts on the AcroForm...only required before PDFJT 4.5.0
            FormFieldService.getAcroFormFieldManager(pdfDocument).runCalculateScripts();

            // Run format scripts on the AcroForm...only required before PDFJT 4.7.0
            FormFieldService.getAcroFormFieldManager(pdfDocument).runFormatScripts();
        }

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
        // Check to see if the input data file is properly formatted for the XFAService to use. The two outermost
        // elements should be <xfa:datasets><xfa:data>; if they're not, make it so.
        if (!hasXfaRootHeader(inputDataUrl)) {
            addXfaRootHeader(new File(inputDataUrl.toURI()));
        }
        if (!hasXfaChildHeader(inputDataUrl)) {
            addXfaChildHeader(new File(inputDataUrl.toURI()));
        }

        // Start by getting the form data into an InputStream.
        final InputStream formStream = inputDataUrl.openStream();

        // If we have an XML file with the proper header, use the XFAService to get the data into the PDF.
        XFAService.importElement(pdfDocument, XFAElement.DATASETS, formStream);

        // Just save the file. Generating appearances and running calculations aren't supported for XFA forms, so
        // there's no need to try it.
        DocumentHelper.saveFullAndClose(pdfDocument, outputUrl.toURI().getPath());
    }

    /**
     * Determine if the first element is an xfadataset root element as expected.
     *
     * @param inputDataUrl a file containing XML form data
     * @return a boolean indicating the presence of XFA header tags.
     * @throws Exception a general exception was thrown
     */
    private static boolean hasXfaRootHeader(final URL inputDataUrl) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document xmlDoc = builder.parse(inputDataUrl.openStream());
        final String xmlRootName = xmlDoc.getDocumentElement().getNodeName();

        return xmlRootName.equals(XFA_DATA_ROOT_NODE);
    }

    /**
     * Determine if the first child element is an xfadata element as expected.
     *
     * @param inputDataUrl a file containing XML form data
     * @return a boolean indicating the presence of XFA header tags.
     * @throws Exception a general exception was thrown
     */
    private static boolean hasXfaChildHeader(final URL inputDataUrl) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document xmlDoc = builder.parse(inputDataUrl.openStream());
        final String firstChildName = xmlDoc.getDocumentElement().getChildNodes().item(0).getNodeName();

        return firstChildName.equals(XFA_DATA_CHILD_NODE);
    }

    /**
     * Add a datasets element to the XML.
     *
     * @param xfaData the XML file
     * @throws Exception a general exception was thrown
     */
    private static void addXfaRootHeader(final File xfaData) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document oldDoc = builder.parse(xfaData);
        final Node oldRoot = oldDoc.getDocumentElement();
        final Document newDoc = builder.newDocument();
        final Element newRoot = newDoc.createElementNS(XFA_DATA_NS_URI, XFA_DATA_ROOT_NODE);

        newDoc.appendChild(newRoot);
        final Node dataNode = newRoot;
        dataNode.appendChild(newDoc.importNode(oldRoot, true));

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Result xmlFile = new StreamResult(xfaData);
        final Source newXml = new DOMSource(newDoc);
        transformer.transform(newXml, xmlFile);
    }

    /**
     * Add a data element to the XML.
     *
     * @param xfaData the XML file
     * @throws Exception a general exception was thrown
     */
    private static void addXfaChildHeader(final File xfaData) throws Exception {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document oldDoc = builder.parse(xfaData);
        final Node oldRoot = oldDoc.getElementsByTagName(XFA_DATA_ROOT_NODE).item(0).getFirstChild();
        final Document newDoc = builder.newDocument();
        final Element newRoot = newDoc.createElementNS(XFA_DATA_NS_URI, XFA_DATA_ROOT_NODE);

        newDoc.appendChild(newRoot);
        final Node dataNode = newRoot.appendChild(newDoc.createElement(XFA_DATA_CHILD_NODE));
        dataNode.appendChild(newDoc.importNode(oldRoot, true));

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final Result xmlFile = new StreamResult(xfaData);
        final Source newXml = new DOMSource(newDoc);
        transformer.transform(newXml, xmlFile);
    }

    /**
     * Check to see if PDFJT is before version 4.0.0-SNAPSHOT.
     *
     * <p>
     * This is necessary to accommodate both old and new dependencies on PDFJT. Uses the version.properties resource
     * stored in PDFJT.
     *
     * @return is PDFJT before version 4.0.0-SNAPSHOT
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static boolean pdfjtIsBeforeVersion4() throws IOException {
        try (final InputStream propertiesStream = Version.class.getResourceAsStream("version.properties")) {
            final Properties versionProperties = new Properties();
            versionProperties.load(propertiesStream);
            final String pdfjtVersion = versionProperties.getProperty("Implementation-Version");

            final DefaultArtifactVersion pdfjtArtifactVersion = new DefaultArtifactVersion(pdfjtVersion);
            final DefaultArtifactVersion version400Snapshot = new DefaultArtifactVersion("4.0.0-SNAPSHOT");
            return pdfjtArtifactVersion.compareTo(version400Snapshot) < 0;
        }
    }

}

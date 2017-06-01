/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.forms.PDFField;
import com.adobe.pdfjt.pdf.interactive.forms.PDFFieldType;
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import htmlflow.HtmlView;
import htmlflow.elements.HtmlForm;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This sample demonstrates how to create an HTML form by inspecting an AcroForm in a PDF.
 */
public class ConvertAcroFormToHtml {
    public static final String DEFAULT_INPUT = "acroform_fdf.pdf";
    public static final String HTML_OUTPUT = "acroform.html";

    private static final Logger LOGGER = Logger.getLogger(ConvertAcroFormToHtml.class.getName());

    /**
     * This is a utility class, and won't be instantiated.
     */
    private ConvertAcroFormToHtml() {}

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     * @throws PDFUnableToCompleteOperationException
     * @throws PDFSecurityException
     * @throws PDFIOException
     * @throws PDFInvalidDocumentException
     */
    public static void main(final String[] args) throws URISyntaxException, IOException, PDFInvalidDocumentException,
                    PDFIOException, PDFSecurityException, PDFUnableToCompleteOperationException {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        URL inputUrl = null;
        URL outputUrl = null;
        if (args.length > 0) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            outputUrl = IoUtils.createUrlFromPath(args[1]);
        } else {
            inputUrl = ExportFormDataToCsv.class.getResource(DEFAULT_INPUT);
            outputUrl = IoUtils.createUrlFromPath(HTML_OUTPUT);
        }

        createHtmlForm(inputUrl, outputUrl);
    }

    /**
     * Create an HTML form from the AcroForm found in the PDF specified by inputUrl.
     *
     * @param inputUrl
     * @param outputUrl
     * @throws URISyntaxException
     * @throws IOException
     * @throws PDFSecurityException
     * @throws PDFIOException
     * @throws PDFInvalidDocumentException
     * @throws PDFUnableToCompleteOperationException
     */
    public static void createHtmlForm(final URL inputUrl, final URL outputUrl)
                    throws URISyntaxException, IOException, PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFUnableToCompleteOperationException {
        PDFDocument pdfDocument = null;

        try {
            pdfDocument = DocumentUtils.openPdfDocument(inputUrl);

            final PDFInteractiveForm form = pdfDocument.getInteractiveForm();
            final Iterator<PDFField> fieldIterator = form.iterator();

            final HtmlView<?> taskView = new HtmlView<>();

            // setup the head element of the Html document
            taskView
                    .head()
                    // use the title of the Pdf as the title of the Html document
                    .title(pdfDocument.getDocumentInfo().getTitle());

            // create an Html form
            final HtmlForm<?> htmlForm = taskView.body().form("Form");

            // add each field from the Pdf form to the Html form
            while (fieldIterator.hasNext()) {
                final PDFField field = fieldIterator.next();

                // determine what Html element should be used based on the type of field in the Pdf form
                final PDFFieldType fieldType = field.getFieldType();
                if (fieldType == PDFFieldType.Text) {
                    // output the qualified name of the field in the Pdf as text before the field
                    // in the Html form so the user knows what the field is for
                    htmlForm.text(field.getQualifiedName())
                            // use the qualified name as the name of the field in the Html form as well
                            .inputText(field.getQualifiedName()).br();
                } else {
                    // log a warning if a field was not output because a matching type was not found
                    LOGGER.warning(field.getQualifiedName() + " was not output in the Html form!");
                }
            }

            final File outputFile = new File(outputUrl.toURI());
            if (outputFile.exists()) {
                Files.delete(outputFile.toPath());
            }

            // output the Html form to a file
            try (PrintStream out = new PrintStream(outputFile)) {
                taskView.setPrintStream(out).write();
            }

        } finally {
            if (pdfDocument != null) {
                pdfDocument.close();
                pdfDocument = null;
            }
        }
    }
}

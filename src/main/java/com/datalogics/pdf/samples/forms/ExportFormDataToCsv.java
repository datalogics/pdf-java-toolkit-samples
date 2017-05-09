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
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This sample demonstrates exporting data from PDF form fields with formatting to a CSV file.
 */
public class ExportFormDataToCsv {
    private static final Logger LOGGER = Logger.getLogger(ExportFormDataToCsv.class.getName());

    /**
     * @param args
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws URISyntaxException a string could not be parsed as a URI reference
     */
    public static void main(final String[] args) throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, IOException, PDFUnableToCompleteOperationException, URISyntaxException {
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

            exportFormFields(inputUrl, outputUrl);
        }
    }

    /**
     * Export the form data to a comma separated form (CSV). The first row that is written out in the CSV contains the
     * qualified field names and the second row contains the field values.
     *
     * @param inputUrl
     * @param outputUrl
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws URISyntaxException a string could not be parsed as a URI reference
     */
    public static void exportFormFields(final URL inputUrl, final URL outputUrl) throws PDFInvalidDocumentException,
                    PDFIOException, PDFSecurityException, IOException, PDFUnableToCompleteOperationException,
                    URISyntaxException {
        final PDFDocument pdfDocument = DocumentUtils.openPdfDocument(inputUrl);

        final PDFInteractiveForm form = pdfDocument.getInteractiveForm();

        final File outputFile = new File(outputUrl.toURI());

        if (outputFile.createNewFile()) {
            final PrintWriter writer = new PrintWriter(outputFile);

            exportFieldNames(form, writer);

            exportFieldValues(form, writer);

            writer.flush();
            writer.close();
        }

        pdfDocument.close();
    }

    /**
     * Export the form field names in comma separated form
     *
     * @param writer
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    private static void exportFieldNames(final PDFInteractiveForm form, final PrintWriter writer)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException {
        final Iterator<PDFField> fieldIterator = form.iterator();

        while (fieldIterator.hasNext()) {
            final PDFField field = fieldIterator.next();

            writer.write(field.getQualifiedName());
            writeComma(writer);
        }

        writeNewLine(writer);
    }

    /**
     * Export the values in the form fields, using their formatted values, in comma separated form. If no value is found
     * in the field, a space is written instead
     *
     * @param form
     * @param writer
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    private static void exportFieldValues(final PDFInteractiveForm form, final PrintWriter writer)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException {
        final Iterator<PDFField> fieldIterator = form.iterator();
        while (fieldIterator.hasNext()) {
            final PDFField field = fieldIterator.next();

            final String formattedValue = field.getFormattedValue();
            if (formattedValue != null) {
                writer.write(formattedValue);
            } else {
                writer.write(" ");
                LOGGER.warning(field.getQualifiedName() + " has no value!");
            }

            writeComma(writer);
        }
    }

    /**
     * Write a "," (comma) to the provided PrintWriter
     *
     * @param writer
     */
    private static void writeComma(final PrintWriter writer) {
        writer.write(",");
    }

    /**
     * Write a new line, using the system defined line separator, to the provided PrintWriter
     *
     * @param writer
     */
    private static void writeNewLine(final PrintWriter writer) {
        writer.write(System.getProperty("line.separator"));
    }

}

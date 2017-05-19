/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.forms.PDFField;
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;
import com.adobe.pdfjt.services.xfa.XFAService;
import com.adobe.pdfjt.services.xfa.XFAService.XFAElement;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.VersionUtils;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Test the Fill Form sample.
 */
public class FillFormTest extends SampleTest {

    private static final String MISSING_TAGS_INPUT = "missing_xfa_tags.xml";
    private static final String TEMP_OUTPUT = "temp.xml";
    private static final String ACROFORM_FDF_DATA = "123456 John Doe 101 N. Wacker Dr, Suite 1800 Chicago IL 60606 "
                    + "1-312-853-8200 johnd@datalogics.com 2 20 15.75 55.75 Yes Off Yes Off Yes";

    /**
     * Expected XFDF data before PDFJT 4. Contains nulls for undefined fields.
     */
    private static final String ACROFORM_XFDF_DATA_PDFJT_3 = "Datalogics, Inc. John Doe 101 N. Wacker Dr. Ste 1800 "
                    + "Chicago IL 60606 0.0 company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation"
                    + ".state\tlocation.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tJohn\tDoe\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\tnull\tnull"
                    + "\t0 <?xml version=\"1.0\" encoding=\"UTF-8\"?><xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:"
                    + "space=\"preserve\"><fields><field name=\"company\"><value>Datalogics, Inc.</value></field>"
                    + "<field name=\"name\"><field name=\"first\"><value>John</value></field><field name="
                    + "\"last\"><value>Doe</value></field></field><field name=\"location\"><field name=\"address\">"
                    + "<value>101 N. Wacker Dr. Ste 1800</value></field><field name=\"city\"><value>Chicago</value>"
                    + "</field><field name=\"state\"><value>IL</value></field><field name=\"zip\"><value>60606</value>"
                    + "</field></field><field name=\"calculatedNumber\"><value>0.0</value></field></fields><ids "
                    + "modified=\"759F29AF67E14390951898904CE98DB0\" original=\"04FE695A7CFA30449E7CC4B320AB79D7\"/>"
                    + "</xfdf> company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation.state\t"
                    + "location.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tJohn\tDoe\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\tnull\t"
                    + "null\t0 company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation.state\t"
                    + "location.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tJohn\tDoe\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\tnull\t"
                    + "null\t0";

    /**
     * Expected XFDF data for PDFJT 4 and newer. Contains empty strings for undefined fields.
     */
    private static final String ACROFORM_XFDF_DATA = "Datalogics, Inc. John Doe 101 N. Wacker Dr. Ste 1800 "
                    + "Chicago IL 60606 0.0 company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation"
                    + ".state\tlocation.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tJohn\tDoe\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\t\t"
                    + "\t0 <?xml version=\"1.0\" encoding=\"UTF-8\"?><xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:"
                    + "space=\"preserve\"><fields><field name=\"company\"><value>Datalogics, Inc.</value></field>"
                    + "<field name=\"name\"><field name=\"first\"><value>John</value></field><field name="
                    + "\"last\"><value>Doe</value></field></field><field name=\"location\"><field name=\"address\">"
                    + "<value>101 N. Wacker Dr. Ste 1800</value></field><field name=\"city\"><value>Chicago</value>"
                    + "</field><field name=\"state\"><value>IL</value></field><field name=\"zip\"><value>60606</value>"
                    + "</field></field><field name=\"calculatedNumber\"><value>0.0</value></field></fields><ids "
                    + "modified=\"759F29AF67E14390951898904CE98DB0\" original=\"04FE695A7CFA30449E7CC4B320AB79D7\"/>"
                    + "</xfdf> company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation.state\t"
                    + "location.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tJohn\tDoe\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\t\t"
                    + "\t0 company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation.state\t"
                    + "location.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tJohn\tDoe\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\t\t"
                    + "\t0";

    private static final String XFA_FORM_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xfa:datasets "
                    + "xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\"><xfa:data><form1><Name>John "
                    + "Doe</Name><Title>Software Engineer</Title><Deptartment>Engineering</Deptartment>"
                    + "<Phone>1-312-853-8200</Phone><Date>2016-01-01</Date><DateNeeded>2016-02-01</DateNeeded>"
                    + "<Reason>Travel</Reason><Payee>John Doe</Payee><Amount>1234</Amount><Date/><DateNeeded/>"
                    + "<Reason/><Payee/><Amount/><Date/><DateNeeded/><Reason/><Payee/><Amount/><Date/><DateNeeded/>"
                    + "<Reason/><Payee/><Amount/><Date/><DateNeeded/><Reason/><Payee/><Amount/><Date/><DateNeeded/>"
                    + "<Reason/><Payee/><Amount/><DeliveryInstructions>Direct Deposit</DeliveryInstructions>"
                    + "<Comments/><AmountPaid/><CheckNo/><DateReceived/></form1></xfa:data></xfa:datasets>";


    // Each test will check that an output file has been created, then it will compare the form data in that file
    // to the values that we expect to see.

    @Test
    public void testAcroformFdf() throws Exception {
        final URL inputUrl = FillForm.class.getResource(FillForm.ACROFORM_FDF_INPUT);
        final PDFDocument inputPdfDocument = DocumentUtils.openPdfDocument(inputUrl);

        final URL inputDataUrl = FillForm.class.getResource(FillForm.ACROFORM_FDF_DATA);

        final File outputPdfFile = newOutputFileWithDelete(FillForm.ACROFORM_FDF_OUTPUT);
        FillForm.fillAcroformFdf(inputPdfDocument, inputDataUrl, outputPdfFile.toURI().toURL());
        assertTrue(outputPdfFile.getPath() + " must exist after run", outputPdfFile.exists());

        checkForms(outputPdfFile.toURI().toURL(), ACROFORM_FDF_DATA);
    }

    @Test
    public void testAcroformXfdf() throws Exception {
        final URL inputUrl = FillForm.class.getResource(FillForm.ACROFORM_XFDF_INPUT);
        final PDFDocument inputPdfDocument = DocumentUtils.openPdfDocument(inputUrl);

        final URL inputDataUrl = FillForm.class.getResource(FillForm.ACROFORM_XFDF_DATA);

        final File outputPdfFile = newOutputFileWithDelete(FillForm.ACROFORM_XFDF_OUTPUT);
        FillForm.fillAcroformXfdf(inputPdfDocument, inputDataUrl, outputPdfFile.toURI().toURL());

        assertTrue(outputPdfFile.getPath() + " must exist after run", outputPdfFile.exists());
        checkForms(outputPdfFile.toURI().toURL(),
                   VersionUtils.pdfjtIsBeforeVersion4() ? ACROFORM_XFDF_DATA_PDFJT_3 : ACROFORM_XFDF_DATA);
    }

    @Test
    public void testXfaXml() throws Exception {
        final URL inputUrl = FillForm.class.getResource(FillForm.XFA_PDF_INPUT);
        final PDFDocument inputPdfDocument = DocumentUtils.openPdfDocument(inputUrl);

        final URL inputDataUrl = FillForm.class.getResource(FillForm.XFA_XML_DATA);

        final File outputPdfFile = newOutputFileWithDelete(FillForm.XFA_OUTPUT);
        FillForm.fillXfa(inputPdfDocument, inputDataUrl, outputPdfFile.toURI().toURL());

        assertTrue(outputPdfFile.getPath() + " must exist after run", outputPdfFile.exists());
        checkForms(outputPdfFile.toURI().toURL(), XFA_FORM_DATA);
    }

    @Test
    public void testXfaMissingTagsXml() throws Exception {
        final URL inputUrl = FillForm.class.getResource(FillForm.XFA_PDF_INPUT);
        final PDFDocument inputPdfDocument = DocumentUtils.openPdfDocument(inputUrl);

        final URL inputDataUrl = FillForm.class.getResource(MISSING_TAGS_INPUT);

        final File outputPdfFile = newOutputFileWithDelete(FillForm.XFA_OUTPUT);
        FillForm.fillXfa(inputPdfDocument, inputDataUrl, outputPdfFile.toURI().toURL());

        assertTrue(outputPdfFile.getPath() + " must exist after run", outputPdfFile.exists());
        checkForms(outputPdfFile.toURI().toURL(), XFA_FORM_DATA);
    }

    private void checkForms(final URL outputFileUrl, final String compare) throws Exception {
        // Check the output doc
        final PDFDocument outputDoc = DocumentUtils.openPdfDocument(outputFileUrl);
        final PDFInteractiveForm pdfForm = outputDoc.getInteractiveForm();
        final Iterator<PDFField> fieldIterator = pdfForm.iterator();
        final StringBuilder sb = new StringBuilder();
        while (fieldIterator.hasNext()) {
            final PDFField field = fieldIterator.next();
            if (field.getValueList() != null) {
                // Turn the list of values in each field into a string. Get rid of the brackets that List.toString()
                // puts in there.
                sb.append(field.getValueList().toString().replace("[", "").replace("]", "") + " ");
            }
        }
        // If we didn't get anything out of the form with the above method, this is probably an XFA form. Get the form
        // data into a temporary file and compare that.
        if (sb.toString().equals("")) {
            try (final OutputStream xfaFields = new FileOutputStream(TEMP_OUTPUT)) {
                XFAService.exportElement(outputDoc, XFAElement.DATASETS, xfaFields);
                xfaFields.close();

                final File tempOutFile = new File(TEMP_OUTPUT);
                final byte[] fileBytes = Files.readAllBytes(Paths.get(tempOutFile.getCanonicalPath()));
                sb.append(new String(fileBytes, "UTF-8"));
                Files.delete(tempOutFile.toPath());
            }
        }
        assertEquals("Form data should match expected values", compare, sb.toString().trim());
    }
}

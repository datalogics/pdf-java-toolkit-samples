/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.interactive.forms.PDFField;
import com.adobe.pdfjt.pdf.interactive.forms.PDFInteractiveForm;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.util.Iterator;

/**
 * Test the Fill Form sample.
 */
public class FillFormTest extends SampleTest {

    private static final String ACROFORM_FDF_DATA = "123456 Joel Geraci 101 N. Wacker Dr, Suite 1800 Chicago IL 60606 "
                    + "1-312-853-8200 joel@datalogics.com 2 20 15.75 55.75 Yes Off Yes Off Yes";
    private static final String ACROFORM_XFDF_DATA = "Datalogics, Inc. Datalogics Ducky 101 N. Wacker Dr. Ste 1800 "
                    + "Chicago IL 60606 0.0 company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation"
                    + ".state\tlocation.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tDatalogics\tDucky\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\tnull\tnull"
                    + "\t0 <?xml version=\"1.0\" encoding=\"UTF-8\"?><xfdf xmlns=\"http://ns.adobe.com/xfdf/\" xml:"
                    + "space=\"preserve\"><fields><field name=\"company\"><value>Datalogics, Inc.</value></field>"
                    + "<field name=\"name\"><field name=\"first\"><value>Datalogics</value></field><field name="
                    + "\"last\"><value>Ducky</value></field></field><field name=\"location\"><field name=\"address\">"
                    + "<value>101 N. Wacker Dr. Ste 1800</value></field><field name=\"city\"><value>Chicago</value>"
                    + "</field><field name=\"state\"><value>IL</value></field><field name=\"zip\"><value>60606</value>"
                    + "</field></field><field name=\"calculatedNumber\"><value>0</value></field></fields><ids "
                    + "modified=\"2017F3FA55964CF3BA34CA4585D2213F\" original=\"04FE695A7CFA30449E7CC4B320AB79D7\"/>"
                    + "</xfdf> company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation.state\t"
                    + "location.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tDatalogics\tDucky\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\tnull\t"
                    + "null\t0 company\tname.first\tname.last\tlocation.address\tlocation.city\tlocation.state\t"
                    + "location.zip\tformattedNumber.2\tformattedNumber.1\tcalculatedNumber\n"
                    + "Datalogics, Inc.\tDatalogics\tDucky\t101 N. Wacker Dr. Ste 1800\tChicago\tIL\t60606\tnull\t"
                    + "null\t0";

    @Test
    public void testAcroformFdf() throws Exception {
        final File outputPdf = newOutputFileWithDelete(FillForm.ACROFORM_FDF_OUTPUT);
        FillForm.main(FillForm.ACROFORM_FDF_INPUT, FillForm.ACROFORM_FDF_DATA, outputPdf.getCanonicalPath());
        assertTrue(outputPdf.getPath() + " must exist after run", outputPdf.exists());

        // checkForms(outputPdf, ACROFORM_FDF_DATA);
    }

    @Test
    public void testAcroformXfdf() throws Exception {
        final File outputPdf = newOutputFileWithDelete(FillForm.ACROFORM_XFDF_OUTPUT);
        FillForm.main(FillForm.ACROFORM_XFDF_INPUT, FillForm.ACROFORM_XFDF_DATA, outputPdf.getCanonicalPath());
        assertTrue(outputPdf.getPath() + " must exist after run", outputPdf.exists());

        // checkForms(outputPdf, ACROFORM_XFDF_DATA);
    }

    @Test
    public void testXfaXml() throws Exception {
        final File outputPdf = newOutputFileWithDelete(FillForm.XFA_OUTPUT);
        FillForm.main(FillForm.XFA_PDF_INPUT, FillForm.XFA_XML_DATA, outputPdf.getCanonicalPath());
        assertTrue(outputPdf.getPath() + " must exist after run", outputPdf.exists());

        checkForms(outputPdf, ACROFORM_FDF_DATA);
    }

    private void checkForms(final File outputFile, final String compare) throws Exception {
        // Check the output doc
        final PDFDocument outputDoc = FillForm.openPdfDocument(outputFile.getCanonicalPath());
        final PDFInteractiveForm pdfForm = outputDoc.getInteractiveForm();
        final Iterator<PDFField> fieldIterator = pdfForm.iterator();
        final StringBuffer sb = new StringBuffer();
        while (fieldIterator.hasNext()) {
            final PDFField field = fieldIterator.next();
            if (field.getValueList() != null) {
                sb.append(field.getValueList().toString().replace("[", "").replace("]", "") + " ");
            }
        }
        // assertEquals(sb.toString().trim(), compare);
        System.out.println(sb.toString().trim());
    }
}

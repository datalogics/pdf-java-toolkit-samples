/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.signature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.services.digsig.SignatureFieldInterface;
import com.adobe.pdfjt.services.digsig.SignatureManager;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * Tests the DocumentSigning Sample.
 */
public class DocumentSigningTest extends SampleTest {
    static final String FILE_NAME = "SignedField1.pdf";
    static final String QUALIFIED_SIGNATURE_FIELD_NAME = "Approver";

    @Test
    public void testMain() throws Exception {
        final File file = newOutputFile(FILE_NAME);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        // The complete file name will be set in the DocSigning class.
        final String path = file.getCanonicalPath().replaceAll("1.pdf", "");

        DocumentSigning.main(path);
        // Make sure the Output file exists.
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = openPdfDocument(file.getCanonicalPath());

        try {
            // Make sure that Signature field is signed.
            final SignatureFieldInterface sigField = getSignedSignatureField(doc);
            assertTrue("Signature field must be signed", sigField.isSigned());
            assertTrue("Signature field must be visible", sigField.isVisible());
            assertEquals("Qualified field names must match", QUALIFIED_SIGNATURE_FIELD_NAME,
                         sigField.getQualifiedName());

        } finally {
            doc.close();
        }
    }

    private static SignatureFieldInterface getSignedSignatureField(final PDFDocument doc)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException {
        // Set up a signature service and get the first signature field.
        final SignatureManager sigService = SignatureManager.newInstance(doc);
        if (sigService.hasSignedSignatureFields()) {
            final Iterator<SignatureFieldInterface> iter = sigService.getDocSignatureFieldIterator();
            if (iter.hasNext()) {
                return iter.next();
            }
        }
        return null;
    }
}

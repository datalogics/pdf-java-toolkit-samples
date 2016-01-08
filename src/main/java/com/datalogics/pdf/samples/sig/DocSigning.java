/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.sig;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.internal.io.RandomAccessFileByteWriter;
import com.adobe.pdfjt.core.credentials.CredentialFactory;
import com.adobe.pdfjt.core.credentials.Credentials;
import com.adobe.pdfjt.core.credentials.PrivateKeyHolder;
import com.adobe.pdfjt.core.credentials.PrivateKeyHolderFactory;
import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.services.digsig.SignatureFieldInterface;
import com.adobe.pdfjt.services.digsig.SignatureManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This is a sample that demonstrates how to find a specific signature field in a document so that API users can sign
 * the correct field.
 */
public final class DocSigning {
    private static final Logger LOGGER = Logger.getLogger(DocSigning.class.getName());


    private static final String der_key_path = "pdfjt-key.der";
    private static final String der_cert_path = "pdfjt-cert.der";
    private static final String inputUnsignedPDFPath = "UnSignedDoc.pdf";
    private static final String outSignedPDFPath = "SignedField";

    private static int sigFieldIndex;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private DocSigning() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            path = outSignedPDFPath;
        }
        run(path);
    }

    static void run(final String outputPath) throws Exception {
        // Query and sign all permissible signature fields.
        signExistingSignatureFields(outputPath);
    }

    private static void signExistingSignatureFields(final String outputPath) throws Exception {
        PDFDocument pdfDoc = null;
        ByteReader byteReader = null;
        sigFieldIndex = 1;
        try {
            // Get the PDF file.
            final InputStream inputStream = DocSigning.class.getResourceAsStream(inputUnsignedPDFPath);
            byteReader = new InputStreamByteReader(inputStream);
            pdfDoc = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());

            // Set up a signature service and iterate over all of the
            // signature fields.
            final SignatureManager sigService = SignatureManager.newInstance(pdfDoc);
            if (sigService.hasUnsignedSignatureFields()) {
                final Iterator<SignatureFieldInterface> iter = sigService.getDocSignatureFieldIterator();
                while (iter.hasNext()) {
                    final SignatureFieldInterface sigField = iter.next();
                    signField(sigService, sigField, outputPath);
                }
            }
        } finally {
            try {
                if (pdfDoc != null) {
                    pdfDoc.close();
                }
            } catch (final PDFException e) {
                LOGGER.severe(e.getMessage());
            }
            if (byteReader != null) {
                byteReader.close();
            }
        }
    }

    private static void signField(final SignatureManager sigMgr,
                                     final SignatureFieldInterface sigField, final String outputPath)
                                                     throws Exception {

        final String qualifiedName = "Fully Qualified Name: " + sigField.getQualifiedName();
        LOGGER.info(qualifiedName);

        ByteWriter byteWriter = null;
        try {
            final Credentials credentials = createCredentials();
            // Must be permitted to sign doc and field must be visible.
            if (sigField.isSigningPermitted()) {
                if (sigField.isVisible()) {
                    // Create output file to hold the signed PDF data.
                    final RandomAccessFile outputRaf = new RandomAccessFile(outputPath + sigFieldIndex++ + ".pdf",
                                                                            "rw");
                    byteWriter = new RandomAccessFileByteWriter(outputRaf);
                    // Sign the document.
                    sigMgr.sign(sigField, credentials, byteWriter);
                } else {
                    throw new PDFIOException("Signature field is not visible");
                }
            }
            byteWriter = null;

        } finally {
            if (byteWriter != null) {
                byteWriter.close();
            }
        }
    }

    private static Credentials createCredentials() throws Exception {

        final String sigAlgorithm = "RSA";
        final InputStream certStream = DocSigning.class.getResourceAsStream(der_cert_path);
        final InputStream keyStream = DocSigning.class.getResourceAsStream(der_key_path);

        return createCredentialsFromDerBytes(certStream, keyStream, sigAlgorithm);
    }


    private static Credentials createCredentialsFromDerBytes(final InputStream certStream,
                                                             final InputStream keyStream,
                                                             final String sigAlgorithm)
                                                                             throws Exception {
        final byte[] derEncodedPrivateKey = getDerEncodedData(keyStream);
        final byte[] derEncodedCert = getDerEncodedData(certStream);
        final PrivateKeyHolder privateKeyHolder = PrivateKeyHolderFactory
                                                                         .newInstance()
                                                                         .createPrivateKey(derEncodedPrivateKey,
                                                                                           sigAlgorithm);
        final Credentials credentials = CredentialFactory.newInstance()
                                                         .createCredentials(privateKeyHolder, derEncodedCert,
                                                                            null);
        return credentials;
    }

    private static byte[] getDerEncodedData(final InputStream inputStream) throws IOException {
        final byte[] derData = new byte[inputStream.available()];
        final int totalBytes = inputStream.read(derData, 0, derData.length);
        if (totalBytes == 0) {
            LOGGER.info("getDerEncodedData(): No bytes read from InputStream");
        }
        inputStream.close();

        return derData;
    }
}

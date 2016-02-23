/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.signature;

import com.adobe.internal.io.ByteWriter;
import com.adobe.pdfjt.core.credentials.CredentialFactory;
import com.adobe.pdfjt.core.credentials.Credentials;
import com.adobe.pdfjt.core.credentials.PrivateKeyHolder;
import com.adobe.pdfjt.core.credentials.PrivateKeyHolderFactory;
import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.services.digsig.SignatureFieldInterface;
import com.adobe.pdfjt.services.digsig.SignatureManager;
import com.adobe.pdfjt.services.digsig.SignatureOptions;
import com.adobe.pdfjt.services.digsig.UserInfo;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This is a sample that demonstrates how to find a specific signature field in a document so that API users can sign
 * the correct field. Note that, since we are using a sample certificate that isn't backed up by any certificate
 * authority, Adobe Acrobat (and possibly other applications) will display a warning when the document is opened. This
 * is not due to any error with the document itself and is only to let the user know that the certificate authenticity
 * could not be verified.
 */
public final class SignDocument {
    private static final Logger LOGGER = Logger.getLogger(SignDocument.class.getName());


    private static final String DER_KEY_PATH = "pdfjt-key.der";
    private static final String DER_CERT_PATH = "pdfjt-cert.der";
    public static final String INPUT_UNSIGNED_PDF_PATH = "UnsignedDocument.pdf";
    public static final String OUTPUT_SIGNED_PDF_PATH = "SignedField.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private SignDocument() {}

    /**
     * Main program.
     *
     * @param args command line arguments. Only one is expected in order to specify the output path. If no arguments are
     *        given, the sample will output to the root of the samples directory by default.
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        URL outputUrl = null;
        if (args.length > 0) {
            outputUrl = new File(args[0]).toURI().toURL();
        } else {
            outputUrl = new File(OUTPUT_SIGNED_PDF_PATH).toURI().toURL();
        }

        final URL inputUrl = SignDocument.class.getResource(INPUT_UNSIGNED_PDF_PATH);
        // Query and sign all permissible signature fields.
        signExistingSignatureFields(inputUrl, outputUrl);
    }

    /**
     * Sign existing signature fields found in the example document.
     *
     * @param inputUrl the URL to the input file
     * @param outputUrl the path to the file to contain the signed document
     * @throws Exception a general exception was thrown
     */
    public static void signExistingSignatureFields(final URL inputUrl, final URL outputUrl) throws Exception {
        PDFDocument pdfDoc = null;
        try {
            // Get the PDF file.
            pdfDoc = DocumentUtils.openPdfDocument(inputUrl);

            // Set up a signature service and iterate over all of the
            // signature fields.
            final SignatureManager sigService = SignatureManager.newInstance(pdfDoc);
            if (sigService.hasUnsignedSignatureFields()) {
                final Iterator<SignatureFieldInterface> iter = sigService.getDocSignatureFieldIterator();
                while (iter.hasNext()) {
                    final SignatureFieldInterface sigField = iter.next();
                    signField(sigService, sigField, outputUrl);
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
        }
    }

    private static void signField(final SignatureManager sigMgr,
                                  final SignatureFieldInterface sigField, final URL outputUrl)
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
                    byteWriter = IoUtils.newByteWriter(outputUrl);

                    final SignatureOptions signatureOptions = SignatureOptions.newInstance();
                    final UserInfo userInfo = UserInfo.newInstance();

                    // This name will show up in the signature as "Digitally signed by <name>".
                    // If no name is specified the signature will say it was signed by whatever name is
                    // on the credentials used to sign the document.
                    userInfo.setName("John Doe");
                    signatureOptions.setUserInfo(userInfo);

                    // Sign the document.
                    sigMgr.sign(sigField, signatureOptions, credentials, byteWriter);
                } else {
                    throw new PDFIOException("Signature field is not visible");
                }
            }
        } finally {
            if (byteWriter != null) {
                byteWriter.close();
            }
        }
    }

    private static Credentials createCredentials() throws Exception {

        // These are sample files whose authenticity won't be able to be verified by Acrobat. When opening a document
        // signed with this certificate, Acrobat will display a warning. This does not indicate any error in the
        // document itself aside from the unverifiable signature.
        final String sigAlgorithm = "RSA";
        final InputStream certStream = SignDocument.class.getResourceAsStream(DER_CERT_PATH);
        final InputStream keyStream = SignDocument.class.getResourceAsStream(DER_KEY_PATH);

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

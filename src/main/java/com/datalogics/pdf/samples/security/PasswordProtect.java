/*
 * Copyright 2018 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.security;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;
import com.adobe.pdfjt.services.security.SecurityLockPassword;
import com.adobe.pdfjt.services.security.UnicodePasswordUtil;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.signature.SignDocument;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;

public class PasswordProtect {
    public static final String INPUT_UNPROTECTED_PDF_PATH = "/com/datalogics/pdf/samples/manipulation/pdfjavatoolkit-ds.pdf";
    public static final String OUTPUT_PASSWORD_PROTECTED_PDF_PATH = "PasswordProtected.pdf";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * This is a utility class, and won't be instantiated.
     */
    private PasswordProtect() {}

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
            outputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PASSWORD_PROTECTED_PDF_PATH);
        }

        final URL inputUrl = SignDocument.class.getResource(INPUT_UNPROTECTED_PDF_PATH);

        // Query and sign all permissible signature fields.
        passwordProtectDocument(inputUrl, outputUrl, "ownerPassword", "userPassword");
    }

    public static void passwordProtectDocument(final URL inputUrl, final URL outputUrl, final String ownerPassword,
                                               final String userPassword)
                    throws Exception {
        final PDFDocument pdfDocument = DocumentUtils.openPdfDocument(inputUrl);
        final UnicodePasswordUtil unicodePasswordUtil = new UnicodePasswordUtil();
        final byte[] ownerPW = unicodePasswordUtil.getPasswordFromUnicode(ownerPassword);
        final byte[] userPW = unicodePasswordUtil.getPasswordFromUnicode(userPassword);

        // Metadata should *not* be encrypted
        final boolean encryptedMetadata = false;
        final SecurityLockPassword securityLockPassword = SecurityLockPassword.newAES_128bit(pdfDocument,
                                                                                             ownerPW,
                                                                                             userPW,
                                                                                             encryptedMetadata);

        final PDFSaveFullOptions pdfSaveFullOptions = PDFSaveFullOptions.newInstance(securityLockPassword);
        DocumentHelper.saveAndClose(pdfDocument, outputUrl.toURI().getPath(), pdfSaveFullOptions);
    }


}

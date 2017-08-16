/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.security;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.permissionprovider.PermissionProvider;
import com.adobe.pdfjt.core.securityframework.SecurityLock;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveOptions;
import com.adobe.pdfjt.pdf.document.PDFVersion;
import com.adobe.pdfjt.services.security.SecurityLockPassword;
import com.adobe.pdfjt.services.security.StandardEncryptionPermissions;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * AddPasswordToDocument demonstrates how to add user and owner passwords to a PDF.
 *
 * <p>
 * A PDF document can have two kinds of passwords
 * <ul>
 * <li>a user or document password
 * <li>an owner or permissions password, which has a higher level of authority
 * </ul>
 *
 * <p>
 * The person or application creating a PDF document can assign an owner password to the file. Anyone with access to
 * this owner password can also add a user password to the document, encrypt the document, and assign or restrict
 * permissions for working with the document, such as restricting the ability of other users to send the document to a
 * printer. A person or application opening a secured PDF document with a user password will be limited to the options
 * provided for the document in the security settings. But if the PDF file only has a user password assigned, when it is
 * opened with the user password the person opening the file can change the security settings. A person or application
 * with access to an owner password can turn on or off permissions for working with that PDF document, and thus create a
 * set of rules for the document that all other users or applications must follow.
 *
 * <p>
 * The permissions include the right to:
 * <ul>
 * <li>Print a PDF document
 * <li>Add any changes to the document
 * <li>Use the document assembly feature, which allows a user to compile a collection of PDF documents into a single
 * file
 * <li>Copy content from a PDF to another document
 * <li>Copy content for accessibility, such as for the vision impaired
 * <li>Extract pages
 * <li>Add comments
 * <li>Complete form fields
 * <li>Add a digital signature
 * <li>Create template pages
 * </ul>
 *
 * <p>
 * Whenever a password is assigned to a PDF document, that document will also be encrypted to secure it for
 * transmission. Documents created using Adobe Acrobat can be assigned one of three different encryption algorithms. The
 * type of encryption method supported by Adobe Acrobat varies depending on the version of the product:
 * <ul>
 * <li>128 bit RC4 (Acrobat 6.0 and later)
 * <li>128 bit AES (Acrobat 7.0 and later)
 * <li>256 bit AES (Acrobat 9.0 and later)
 * </ul>
 *
 * <p>
 * Note: You cannot use the same password in a PDF document for both the owner password and the user password.
 */
public final class AddPasswordToDocument {
    private static final Logger LOGGER = Logger.getLogger(AddPasswordToDocument.class.getName());

    public static final String INPUT_PDF_PATH = "pdfjavatoolkit-ds.pdf";
    public static final String DEFAULT_OWNER_PASSWORD = "ownerpassword";
    public static final String DEFAULT_USER_PASSWORD = "userpassword";
    public static final String OUTPUT_PASSWORD_PROTECTED_PDF_PATH = "PasswordProtected.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private AddPasswordToDocument() {}

    /**
     * Main program.
     *
     * @param args command line arguments. Four arguments are expected, path to input PDF that should have passwords
     *        added to it, the owner password that should be applied, the user password that should be applied, and the
     *        path to write the password protected PDF to. (If the owner or user password are not required to be added
     *        to the PDF, supply an empty string for either of those arguments.)
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws URISyntaxException a string could not be parsed as a URI reference
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    public static void main(final String[] args) throws PDFSecurityException,
                    PDFInvalidParameterException, PDFInvalidDocumentException, PDFIOException,
                    PDFUnableToCompleteOperationException, IOException, URISyntaxException {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        URL inputUrl = null;
        String ownerPassword = null;
        String userPassword = null;
        URL outputUrl = null;
        if (args.length > 0) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            ownerPassword = args[1];
            userPassword = args[2];
            outputUrl = IoUtils.createUrlFromPath(args[3]);
        } else {
            inputUrl = AddPasswordToDocument.class.getResource(INPUT_PDF_PATH);
            ownerPassword = DEFAULT_OWNER_PASSWORD;
            userPassword = DEFAULT_USER_PASSWORD;
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PASSWORD_PROTECTED_PDF_PATH);
        }

        addPassword(inputUrl, ownerPassword, userPassword, outputUrl);
    }

    /**
     * Add the specified owner and user passwords to the PDF specified by the inputUrl and write the resulting PDF to
     * the outputUrl.
     *
     * @param inputUrl the URL of the PDF to have a password added to it.
     * @param ownerPassword the password to set as the owner password on the PDF.
     * @param userPassword the password to set as the user password on the PDF.
     * @param outputUrl the URL that indicates where the input PDF should be written once passwords have been added to
     *        it.
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws IOException an I/O operation failed or was interrupted
     * @throws URISyntaxException a string could not be parsed as a URI reference
     */
    public static void addPassword(final URL inputUrl, final String ownerPassword, final String userPassword,
                                    final URL outputUrl)
                    throws PDFSecurityException, PDFInvalidParameterException, PDFInvalidDocumentException,
                    PDFIOException, PDFUnableToCompleteOperationException, IOException, URISyntaxException {
        PDFDocument document = null;
        try {
            // Attach font set to PDF
            final PDFFontSet fontSet = FontSetLoader.newInstance().getFontSet();
            final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
            openOptions.setFontSet(fontSet);

            // Get the PDF file.
            document = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);

            final StandardEncryptionPermissions encryptPerms = StandardEncryptionPermissions.newInstanceAll();

            // Update the encryption permissions by turning off the "Add" permission.
            encryptPerms.allowAdd(false);

            // And turning off "Extract" permission.
            encryptPerms.allowExtract(false);

            // And turning on the "Fill" permission.
            encryptPerms.allowFill(true);

            // And turning on the "Modify" permission.
            encryptPerms.allowModify(true);

            final PermissionProvider permsProvider = encryptPerms.getPermissionProvider();

            // Get the bytes that represent the Owner and User passwords if they were supplied
            byte[] ownerPasswordBytes = null;
            if (ownerPassword != null && !ownerPassword.isEmpty()) {
                ownerPasswordBytes = ownerPassword.getBytes("UTF-8");
                LOGGER.warning("Owner password being applied to document");
            }

            byte[] userPasswordBytes = null;
            if (userPassword != null && !userPassword.isEmpty()) {
                userPasswordBytes = userPassword.getBytes("UTF-8");
                LOGGER.warning("User password being applied to document");
            }

            // Create the security lock using the Owner and User passwords provided
            // It is okay for the ownerPasswordBytes and userPasswordBytes to be null. In a case where either are null,
            // the PDF will not have that particular password added.
            final SecurityLock securityLock = SecurityLockPassword.newAES_128bit(ownerPasswordBytes, userPasswordBytes,
                                                                                 permsProvider,
                                                              false);

            // Create PDFSaveFullOptions based on the created security lock.
            // The client can also create PDFSaveFullOptions based on other
            // features and set security lock in the PDFSaveFullOptions object
            // by calling saveFullOptions.setSecurityLock(securityLock);
            final PDFSaveFullOptions saveFullOptions = PDFSaveFullOptions.newInstance(securityLock);

            saveFullOptions.setVersion(PDFVersion.v1_7);

            // To increase security save all COS object in COS streams since
            // all streams will get encrypted (Acrobat 9 compatible).
            saveFullOptions.setObjectCompressionMode(PDFSaveOptions.OBJECT_COMPRESSION_ALL);

            DocumentHelper.saveAndClose(document, outputUrl.toURI().getPath(), saveFullOptions);
        } finally {
            if (document != null) {
                document.close();
                document = null;
            }
        }
    }
}

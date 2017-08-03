/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.exceptions.PDFSecurityAuthorizationException;
import com.adobe.pdfjt.core.permissionprovider.PermissionProvider;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFEncryptionType;
import com.adobe.pdfjt.services.permissions.PermissionsManager;
import com.adobe.pdfjt.services.security.SecurityKeyPassword;
import com.adobe.pdfjt.services.security.StandardEncryptionPermissions;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.net.URL;

/**
 * Tests the AddPasswordToDocument Sample.
 */
public class AddPasswordToDocumentTest extends SampleTest {
    @Rule
    public final ExpectedException expected = ExpectedException.none();
    private static final String FILE_NAME = "PasswordProtected.pdf";

    @Test
    public void testOwnerPassword() throws Exception {
        File file = SampleTest.newOutputFileWithDelete(FILE_NAME);
        PDFDocument pdfDocument = null;

        final URL inputUrl = AddPasswordToDocument.class.getResource(AddPasswordToDocument.INPUT_PDF_PATH);

        final URL outputUrl = file.toURI().toURL();

        AddPasswordToDocument.addPassword(inputUrl, AddPasswordToDocument.DEFAULT_OWNER_PASSWORD,
                                          AddPasswordToDocument.DEFAULT_USER_PASSWORD, outputUrl);

        pdfDocument = DocumentUtils.openPdfDocument(file.toURI().toURL());

        // Test that the PDF is not unlocked
        assertFalse(pdfDocument.isUnlocked());

        // Test that the owner password specified is added to the PDF and unlocks the PDF
        if (pdfDocument.getEncryptionType().equals(PDFEncryptionType.Password)) {
            final SecurityKeyPassword ownerKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_OWNER_PASSWORD.getBytes("UTF-8"));
            assertTrue(pdfDocument.unlock(ownerKey));
        }

        if (pdfDocument != null) {
            pdfDocument.close();
            pdfDocument = null;
        }

        file = null;
    }

    @Test
    public void testUserPassword() throws Exception {
        File file = SampleTest.newOutputFileWithDelete(FILE_NAME);
        PDFDocument pdfDocument = null;

        final URL inputUrl = AddPasswordToDocument.class.getResource(AddPasswordToDocument.INPUT_PDF_PATH);

        final URL outputUrl = file.toURI().toURL();

        AddPasswordToDocument.addPassword(inputUrl, AddPasswordToDocument.DEFAULT_OWNER_PASSWORD,
                                          AddPasswordToDocument.DEFAULT_USER_PASSWORD, outputUrl);

        pdfDocument = DocumentUtils.openPdfDocument(file.toURI().toURL());

        // Test that the PDF is not unlocked
        assertFalse(pdfDocument.isUnlocked());

        // Test that the user password specified is added to the PDF and unlocks the PDF
        if (pdfDocument.getEncryptionType().equals(PDFEncryptionType.Password)) {
            final SecurityKeyPassword userKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_USER_PASSWORD.getBytes("UTF-8"));
            assertTrue(pdfDocument.unlock(userKey));
        }

        if (pdfDocument != null) {
            pdfDocument.close();
            pdfDocument = null;
        }

        file = null;
    }

    @Test
    public void testPermissions() throws Exception {
        final File file = SampleTest.newOutputFileWithDelete(FILE_NAME);
        PDFDocument pdfDocument = null;

        final URL inputUrl = AddPasswordToDocument.class.getResource(AddPasswordToDocument.INPUT_PDF_PATH);

        final URL outputUrl = file.toURI().toURL();

        AddPasswordToDocument.addPassword(inputUrl, AddPasswordToDocument.DEFAULT_OWNER_PASSWORD,
                                          AddPasswordToDocument.DEFAULT_USER_PASSWORD, outputUrl);

        pdfDocument = DocumentUtils.openPdfDocument(file.toURI().toURL());

        assertFalse(pdfDocument.isUnlocked());

        if (pdfDocument.getEncryptionType().equals(PDFEncryptionType.Password)) {
            final SecurityKeyPassword userKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_USER_PASSWORD.getBytes("UTF-8"));
            assertTrue(pdfDocument.unlock(userKey));
        }

        // Test that the correct permissions are set on the PDF
        final PermissionsManager permissionsManager = PermissionsManager.newInstance(pdfDocument);
        final PermissionProvider permissionProvider =
            permissionsManager.getPermissionProvider(PermissionProvider.SECURITY);
        final StandardEncryptionPermissions encryptionPermissions =
            StandardEncryptionPermissions.newInstance(permissionProvider);

        assertFalse(encryptionPermissions.mayAdd());
        assertFalse(encryptionPermissions.mayExtract());
        assertFalse(encryptionPermissions.maySecure());

        assertTrue(encryptionPermissions.mayAssemble());
        assertTrue(encryptionPermissions.mayCopy());
        assertTrue(encryptionPermissions.mayFill());
        assertTrue(encryptionPermissions.mayModify());
        assertTrue(encryptionPermissions.mayPrintHigh());
        assertTrue(encryptionPermissions.mayPrintLow());
    }

    @Test
    public void testEmptyOwnerPassword() throws Exception {
        // Creates a PDF that has a user password and no owner password
        File file = SampleTest.newOutputFileWithDelete(FILE_NAME);
        PDFDocument pdfDocument = null;

        final URL inputUrl = AddPasswordToDocument.class.getResource(AddPasswordToDocument.INPUT_PDF_PATH);

        final URL outputUrl = file.toURI().toURL();

        AddPasswordToDocument.addPassword(inputUrl, null,
                                          AddPasswordToDocument.DEFAULT_USER_PASSWORD, outputUrl);

        pdfDocument = DocumentUtils.openPdfDocument(file.toURI().toURL());

        assertFalse(pdfDocument.isUnlocked());

        // Test that the owner password does not unlock the document and that the user password does
        if (pdfDocument.getEncryptionType().equals(PDFEncryptionType.Password)) {
            final SecurityKeyPassword ownerKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_OWNER_PASSWORD.getBytes("UTF-8"));

            // No owner password on the PDF so an exception is expected
            expected.expect(PDFSecurityAuthorizationException.class);
            expected.expectMessage("Wrong password");
            pdfDocument.unlock(ownerKey);

            // The user password should still be on the PDF and unlock the PDF
            final SecurityKeyPassword userKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_USER_PASSWORD.getBytes("UTF-8"));
            assertTrue(pdfDocument.unlock(userKey));
        }

        if (pdfDocument != null) {
            pdfDocument.close();
            pdfDocument = null;
        }

        file = null;
    }

    @Test
    public void testEmptyUserPassword() throws Exception {
        // Creates a PDF that has an owner password and no user password
        File file = SampleTest.newOutputFileWithDelete(FILE_NAME);
        PDFDocument pdfDocument = null;

        final URL inputUrl = AddPasswordToDocument.class.getResource(AddPasswordToDocument.INPUT_PDF_PATH);

        final URL outputUrl = file.toURI().toURL();

        AddPasswordToDocument.addPassword(inputUrl, AddPasswordToDocument.DEFAULT_OWNER_PASSWORD,
                                          null, outputUrl);

        pdfDocument = DocumentUtils.openPdfDocument(file.toURI().toURL());

        // Test that the owner password specified is added to the PDF and unlocks the PDF
        assertFalse(pdfDocument.isUnlocked());

        // Test that the user password does not unlock the document and that the owner password does
        if (pdfDocument.getEncryptionType().equals(PDFEncryptionType.Password)) {
            final SecurityKeyPassword userKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_USER_PASSWORD.getBytes("UTF-8"));

            // No user password on the PDF so an exception is expected
            expected.expect(PDFSecurityAuthorizationException.class);
            expected.expectMessage("Wrong password");
            pdfDocument.unlock(userKey);

            // The owner password should still be on the PDF and unlock the PDF
            final SecurityKeyPassword ownerKey =
                            SecurityKeyPassword.newInstance(pdfDocument,
                                                AddPasswordToDocument.DEFAULT_OWNER_PASSWORD.getBytes("UTF-8"));
            assertTrue(pdfDocument.unlock(ownerKey));
        }

        if (pdfDocument != null) {
            pdfDocument.close();
            pdfDocument = null;
        }

        file = null;
    }
}

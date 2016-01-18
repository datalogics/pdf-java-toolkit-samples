/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.services.fontresources.PDFFontSetUtil;

import com.datalogics.pdf.document.FontSetLoader;

/**
 * A utility class that contains some commonly used font methods.
 */
public final class FontUtils {

    /**
     * This is a utility class, and won't be instantiated.
     */
    private FontUtils() {}

    /**
     * Create a PDFFontSet that contains fonts used in the original document.
     *
     * @param document The document whose fonts need to be loaded
     * @return A fontset with the appropriate fonts added from the PDFDocument
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    public static PDFFontSet setupDocFontSet(final PDFDocument document)
                    throws PDFInvalidDocumentException, PDFIOException, PDFFontException, PDFSecurityException {
        PDFFontSet sysFontSet = null;
        final FontSetLoader fontSetLoader = FontSetLoader.newInstance();

        sysFontSet = fontSetLoader.getFontSet();
        return PDFFontSetUtil.buildWorkingFontSet(document,
                                                  sysFontSet, document.getDocumentLocale(), null);
    }
}

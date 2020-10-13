/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.RandomAccessFileByteReader;
import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.filters.PDFFilter;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFColorSpace;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFColorSpaceICCBased;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFICCProfile;
import com.adobe.pdfjt.pdf.graphics.font.PDFCIDFont;
import com.adobe.pdfjt.pdf.graphics.font.PDFCIDFontWidths;
import com.adobe.pdfjt.pdf.graphics.font.PDFCIDSystemInfo;
import com.adobe.pdfjt.pdf.graphics.font.PDFFont;
import com.adobe.pdfjt.pdf.graphics.font.PDFFontDescriptor;
import com.adobe.pdfjt.pdf.graphics.font.PDFFontFile;
import com.adobe.pdfjt.pdf.graphics.font.PDFFontSimple;
import com.adobe.pdfjt.pdf.graphics.font.PDFFontType0;
import com.adobe.pdfjt.pdf.graphics.font.impl.PDFFontUtils;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;

import com.datalogics.pdf.dumper.ContentDumper;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * Base class for tests for the samples.
 *
 * <p>
 * This base class provides a font cache, and some utility functions to ease the writing of tests.
 */
public class SampleTestBase {
    private TestFontCacheManager fontCacheManager = null;

    @Before
    public void setUp() throws IOException {
        fontCacheManager = new TestFontCacheManager();
    }

    @After
    public void tearDown() throws Exception {
        fontCacheManager.close();
    }

    /**
     * Get the page contents as a string.
     *
     * @param path the path of the PDF file to open
     * @param pageIndex the index of the page to retrieve the contents from
     * @return the contents of the page as a string
     * @throws FileNotFoundException an attempt to open a file from a pathname has failed
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     * @throws UnsupportedEncodingException the character encoding is not supported
     */
    protected String pageContentsAsString(final String path, final int pageIndex)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException,
                    PDFFontException, PDFInvalidParameterException, PDFConfigurationException {
        final PDFDocument doc = openPdfDocument(path);
        return pageContentsAsString(doc, pageIndex);
    }

    /**
     * Get the page contents as a string.
     *
     * @param doc an open PDF document
     * @param pageIndex the index of the page to retrieve the contents from
     * @return the contents of the page as a string
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     * @throws UnsupportedEncodingException the character encoding is not supported
     */
    protected String pageContentsAsString(final PDFDocument doc, final int pageIndex)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException,
                    PDFFontException, PDFInvalidParameterException,
                    PDFConfigurationException {
        final PDFPage page = pageFromDocument(doc, pageIndex);
        return ContentDumper.getPageContextDump(page, null);
    }

    /**
     * Open a PDF document given a path.
     *
     * @param path the path of the PDF file to open
     * @return the open PDF document
     * @throws FileNotFoundException an attempt to open a file from a pathname has failed
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    protected PDFDocument openPdfDocument(final String path) throws FileNotFoundException,
                    PDFInvalidDocumentException,
                    PDFIOException, PDFSecurityException {
        final RandomAccessFile raf = new RandomAccessFile(path, "r");
        final ByteReader byteReader = new RandomAccessFileByteReader(raf);
        final PDFDocument doc = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
        return doc;
    }

    /**
     * Get the {@link PDFResources} from a page in the document.
     *
     * @param doc the PDFDocument to get the page from
     * @param pageIndex The index into the page tree
     * @return the resources for the page
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    protected PDFResources pageResources(final PDFDocument doc, final int pageIndex)
                    throws PDFInvalidDocumentException,
                    PDFIOException, PDFSecurityException {
        return pageFromDocument(doc, pageIndex).getResources();
    }

    /**
     * Get a page from a {@link PDFDocument}.
     *
     * @param doc the PDFDocument to get the page from
     * @param pageIndex The index into the page tree
     * @return the given page
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    protected PDFPage pageFromDocument(final PDFDocument doc, final int pageIndex) throws PDFInvalidDocumentException,
                    PDFIOException, PDFSecurityException {
        final PDFPageTree pageTree = doc.requirePages();
        final PDFPage page = pageTree.getPage(pageIndex);
        return page;
    }

    /**
     * Return contents of a resource file as a string.
     *
     * <p>
     * The resource is found via the class, and copied to a string. LF characters are replaced with CR LF to match PDF
     * content streams
     *
     * @param resourceName name of the resource to copy to a string
     * @return string containing the contents of the file, with LF translated to CR LF
     * @throws IOException an I/O operation failed or was interrupted
     */
    protected String contentsOfResource(final String resourceName) throws IOException {
        // Scanner trick: http://stackoverflow.com/a/5445161
        try (InputStream is = this.getClass().getResourceAsStream(resourceName);
             Scanner scanner = new Scanner(is, "UTF-8")) {
            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                String returnVal = scanner.next();
                returnVal = returnVal.replaceAll("\\r?\\n", System.lineSeparator());
                return returnVal;
            } else {
                return "";
            }
        }
    }

    /**
     * Return contents of a file as a string.
     *
     * <p>
     * A text file is passed in, and copied to a string. LF characters are replaced with CR LF to match PDF content
     * streams
     *
     * @param file a text file
     * @return string containing the contents of the file, with LF translated to CR LF
     * @throws IOException an I/O operation failed or was interrupted
     */
    protected String contentsOfTextFile(final File file) throws IOException {
        Scanner scanner = null;
        try {
            final InputStream is = new FileInputStream(file);
            scanner = new Scanner(is, "UTF-8");

            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                String returnVal = scanner.next();
                returnVal = returnVal.replaceAll("\\r?\\n", System.lineSeparator());
                return returnVal;
            } else {
                return "";
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Checks validity of a simple font.
     *
     * <p>
     * Checks values in the font, that there are widths, and that there is a font descriptor with a name.
     *
     * @param font the {@link PDFFont} to test
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    protected void assertNiceSimpleFont(final PDFFont font) throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException {
        final PDFFontSimple simpleFont = (PDFFontSimple) font;
        if (!(font.getBaseFont().equals(ASName.k_ZapfDingbats) || font.getBaseFont().equals(ASName.k_Symbol))) {
            assertEquals(ASName.k_WinAnsiEncoding, simpleFont.getEncoding().getBaseEncoding().getName());
        }
        assertEquals(32, simpleFont.getFirstChar());
        assertEquals(255, simpleFont.getLastChar());

        final int[] widths = simpleFont.getWidths();
        assertEquals("Actual size of font widths array must match calculated width",
                     simpleFont.getLastChar() - simpleFont.getFirstChar() + 1, widths.length);

        final PDFFontDescriptor descriptor = simpleFont.getFontDescriptor();
        assertEquals("Descriptor font name must match base font name", descriptor.getFontName(),
                     simpleFont.getBaseFont());
    }

    /**
     * Checks validity of a Type 0 font.
     *
     * <p>
     * Checks values in the font, that there is a descendant font, and that there is a font descriptor with a name.
     *
     * <p>
     * Also checks that the font has a valid name if subsetted.
     *
     * @param font the {@link PDFFont} to test
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    protected void assertNiceType0Font(final PDFFont font)
                    throws PDFSecurityException, PDFIOException, PDFInvalidDocumentException {
        assertThat("it is a type 0 font", font, instanceOf(PDFFontType0.class));
        final PDFFontType0 fontType0 = (PDFFontType0) font;

        final String baseFontName = fontType0.getBaseFont().asString();
        if (baseFontName.length() > 7 && baseFontName.charAt(6) == '+') {
            assertSubsetted(font);
        }

        assertThat("there is a ToUnicode cmap", fontType0.getToUnicodeCMap(), not(nullValue()));
        assertThat("it has Identity-H encoding", fontType0.getEncoding().getCMapName(), equalTo(ASName.k_Identity_H));

        final PDFCIDFont descendantFont = fontType0.getDescendantFont();
        assertThat("there is a descendant font", descendantFont, not(nullValue()));

        final PDFCIDFontWidths widths = descendantFont.getW();
        assertThat("there are widths", widths, not(nullValue()));

        assertThat("there is a DW value", descendantFont.getDW(), not(nullValue()));

        final PDFCIDSystemInfo cidSystemInfo = descendantFont.getCIDSystemInfo();
        assertThat("there is CIDSystemInfo", cidSystemInfo, not(nullValue()));

        assertThat("descendant font name must match main font name", descendantFont.getBaseFont(),
                   equalTo(fontType0.getBaseFont()));

        final PDFFontDescriptor descriptor = descendantFont.getFontDescriptor();
        assertEquals("Descriptor font name must match base font name", descriptor.getFontName(),
                     fontType0.getBaseFont());

        if (PDFFontUtils.isSubsetFont(font)) {
            final PDFFontFile fontFile3 = descriptor.getFontFile3();
            final PDFFontFile fontFile2 = descriptor.getFontFile2();

            assertTrue("there is a FontFile2 or a FontFile3", fontFile2 != null || fontFile3 != null);

            if (fontFile3 != null) {
                final ASName subtype = fontFile3.getSubtype();
                assertThat("the subtype is Type1C or CIDFontType0C",
                           subtype.asString(),
                           anyOf(equalTo("Type1C"), equalTo("CIDFontType0C")));
            }
        }
    }

    /**
     * Checks validity of a font.
     *
     * <p>
     * Checks the font as appropriate using {@link #assertNiceSimpleFont(PDFFont)} or
     * {@link #assertNiceType0Font(PDFFont)} as appropriate.
     *
     * <p>
     * Also checks that the font has a valid name if subsetted.
     *
     * @param font the {@link PDFFont} to test
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    protected void assertNiceFont(final PDFFont font)
                    throws PDFSecurityException, PDFIOException, PDFInvalidDocumentException {
        assertThat(font, anyOf(instanceOf(PDFFontSimple.class), instanceOf(PDFFontType0.class)));

        if (font instanceof PDFFontSimple) {
            assertNiceSimpleFont(font);
        } else {
            assertNiceType0Font(font);
        }
    }

    /**
     * Assert a font is subsetted.
     *
     * @param font the font
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    protected void assertSubsetted(final PDFFont font) throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException {
        // Test this validity first, because it gives better diagnostics than isFontEmbedded()
        assertValidSubsetName(font.getBaseFont().asString());
        assertTrue(PDFFontUtils.isSubsetFont(font));
        assertTrue(PDFFontUtils.isFontEmbedded(font));
    }

    /**
     * Assert that a font has a valid subset name.
     *
     * <p>
     * The initial '/' may or may not be present.
     *
     * @param string the string
     */
    protected void assertValidSubsetName(String string) {
        if (string.charAt(0) == '/') {
            string = string.substring(1);
        }
        assertTrue("Subset name must have more than 7 characters", string.length() > 7);
        assertEquals("Subset name must have '+' in 7th character", "+", string.substring(6, 7));
        for (int i = 0; i < 6; i++) {
            final char subsetNameChar = string.charAt(i);
            assertTrue("Subset prefix must be made of six alphabetic, Latin letters", subsetNameChar >= 'A'
                                                                                      && subsetNameChar <= 'Z');
        }
    }

    /**
     * Assert that the color space is a nice color space.
     *
     * <p>
     * Basically must be ICC-based with three components, and verify that the ICC profile is flate compressed.
     *
     * @param cs the {@link PDFColorSpace} to test
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    protected void assertNiceColorSpace(final PDFColorSpace cs) throws PDFIOException,
                    PDFSecurityException, PDFInvalidDocumentException {
        assertTrue(cs instanceof PDFColorSpaceICCBased);
        assertEquals(3, cs.getNumberOfComponents());
        assertEquals(ASName.k_ICCBased, cs.getName());

        // Verify that the ColorSpace is compressed
        if (cs instanceof PDFColorSpaceICCBased) {
            final PDFICCProfile iccProfile = ((PDFColorSpaceICCBased) cs).getPDFICCProfile();
            assertTrue(iccProfile.hasInputFilters());

            final PDFDocument doc = iccProfile.getPDFDocument();
            final PDFFilter flateFilter = PDFFilter.newInstance(doc, ASName.k_FlateDecode, null);
            assertTrue(iccProfile.getInputFilters().filterAlreadyInList(flateFilter));
        }
    }

    /**
     * Create a {@link File} with the specified name in <tt>target/test-output</tt>.
     *
     * @param filename the name of the file to create
     * @return a {@link File} object containing the file location.
     */
    protected static File newOutputFile(final String filename) {
        final File parent = new File(new File("target"), "test-output");
        try {
            Files.createDirectories(parent.toPath());
        } catch (final IOException e) {
            throw new RuntimeException("can't create directory " + parent, e);
        }
        return new File(parent, filename);
    }

    /**
     * Create a {@link File} with the specified name in <tt>target/test-output</tt>. If the file already exists, delete
     * it.
     *
     * @param filename the name of the file to create
     * @return a {@link File} object containing the file location
     * @throws IOException an I/O operation failed or was interrupted
     */
    protected static File newOutputFileWithDelete(final String filename) throws IOException {
        final File file = newOutputFile(filename);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        return file;
    }
}

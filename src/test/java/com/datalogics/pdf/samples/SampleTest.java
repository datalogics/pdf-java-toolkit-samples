/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.internal.io.RandomAccessFileByteReader;
import com.adobe.internal.io.stream.InputByteStream;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.pdf.document.PDFContents;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.filters.PDFFilter;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFColorSpace;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFColorSpaceICCBased;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFICCProfile;
import com.adobe.pdfjt.pdf.graphics.font.PDFFont;
import com.adobe.pdfjt.pdf.graphics.font.PDFFontDescriptor;
import com.adobe.pdfjt.pdf.graphics.font.PDFFontSimple;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;

import org.junit.After;
import org.junit.Before;

import java.io.File;
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
public class SampleTest {
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
    protected String pageContentsAsString(final String path, final int pageIndex) throws FileNotFoundException,
                    PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException,
                    UnsupportedEncodingException {
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
                    throws PDFInvalidDocumentException,
                    PDFIOException, PDFSecurityException, IOException, UnsupportedEncodingException {
        final PDFPage page = pageFromDocument(doc, pageIndex);
        final PDFContents contents = page.getContents();
        final InputByteStream stream = contents.getContents();
        final byte[] data = new byte[(int) stream.bytesAvailable()];
        stream.read(data);
        final String contentsAsString = new String(data, "cp1252");
        return contentsAsString;
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
     * Open a PDF document from the resources folder given a path.
     *
     * @param path the path inside of the resources folder to the PDF file to open
     * @return the open PDF document
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws IOException an I/O operation failed or was interrupted
     */
    protected PDFDocument openPdfDocumentFromResource(final String path)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {
        final InputStream inputStream = this.getClass().getResourceAsStream(path);
        final ByteReader byteReader = new InputStreamByteReader(inputStream);
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
     * The resource is found via the class, and copied to a string. LF characters are replaced with CR to match PDF
     * content streams
     *
     * @param resourceName name of the resource to copy to a string
     * @return string containing the contents of the file, with CR translated to LF
     * @throws IOException an I/O operation failed or was interrupted
     */
    protected String contentsOfResource(final String resourceName) throws IOException {
        // Scanner trick: http://stackoverflow.com/a/5445161
        try (InputStream is = this.getClass().getResourceAsStream(resourceName);
             Scanner s = new Scanner(is, "UTF-8")) {
            s.useDelimiter("\\A");
            if (s.hasNext()) {
                String returnVal = s.next();
                returnVal = returnVal.replace("\r\n", "\r");
                returnVal = returnVal.replace("\n\r", "\r");
                returnVal = returnVal.replace("\n", "\r");
                return returnVal;
            } else {
                return "";
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
        return new File(new File(new File("target"), "test-output"), filename);
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

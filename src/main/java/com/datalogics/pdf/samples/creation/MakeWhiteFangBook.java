/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import com.adobe.pdfjt.core.exceptions.PDFConfigurationException;
import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASMatrix;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.graphics.PDFExtGState;
import com.adobe.pdfjt.pdf.graphics.PDFRectangle;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;
import com.adobe.pdfjt.services.imageconversion.ImageManager;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.layout.LayoutEngine;
import com.datalogics.pdf.text.Dimension;
import com.datalogics.pdf.text.Heading;
import com.datalogics.pdf.text.Length;
import com.datalogics.pdf.text.Paragraph;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * This sample creates a short version of the book White Fang. It uses PDFJT to insert a picture on the first page, then
 * Talkeetna to flow the text of chapter one.
 *
 */
public final class MakeWhiteFangBook {
    private static final String INPUT_IMAGE_PATH = "WhiteFangCover.jpg";
    public static final String OUTPUT_PDF_PATH = "WhiteFang.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private MakeWhiteFangBook() {}

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
        URL outputUrl;
        if (args.length > 0) {
            outputUrl = new File(args[0]).toURI().toURL();
        } else {
            outputUrl = new File(OUTPUT_PDF_PATH).toURI().toURL();
        }
        makeWhiteFangBook(outputUrl);
    }


    /**
     * Create a short version of the book White Fang. It uses PDFJT to insert a picture on the first page, then
     * Talkeetna to flow the text of chapter one.
     *
     * @param outputUrl the path upon which to write the output
     * @throws Exception a general exception was thrown
     */
    public static void makeWhiteFangBook(final URL outputUrl) throws Exception {
        PDFDocument document = null;

        try {
            document = PDFDocument.newInstance(PDFOpenOptions.newInstance());

            addCoverPage(document);

            addBookText(document);

            DocumentHelper.saveFullAndClose(document, outputUrl.toURI().getPath());
        } finally {
            if (document != null) {
                document.close();
            }
        }

    }

    /**
     * Add the cover page to the document.
     *
     * @param document The document to receive a cover page
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    private static void addCoverPage(final PDFDocument document)
                    throws IOException, PDFIOException, PDFSecurityException, PDFInvalidDocumentException {
        // Create an image decoder and decode the image file
        final InputStream imageStream = MakeWhiteFangBook.class.getResourceAsStream(INPUT_IMAGE_PATH);
        final BufferedImage bim = ImageIO.read(imageStream);
        // Create PDFXObjectImage from bufferedImage
        final PDFXObjectImage image = ImageManager.getPDFImage(bim, document);

        // Create test page object
        final ASRectangle pageSize = ASRectangle.US_LETTER;
        final PDFPage newPage = PDFPage.newInstance(document, PDFRectangle.newInstance(document, pageSize));
        PDFPageTree.newInstance(document, newPage);

        // Images by default occupy a 1 x 1 space at the origin. This transform scales the image to its size in pixels,
        // and then centers the image onto the page by translating it by half the difference between the image size and
        // page size, in both dimensions.
        final ASMatrix imageTransform = ASMatrix.createIdentityMatrix()
                                                .scale(bim.getWidth(), bim.getHeight())
                                                .translate((pageSize.width() - bim.getWidth()) / 2.0,
                                                           (pageSize.height() - bim.getHeight()) / 2.0);
        ImageManager.insertImageInPDF(image, newPage, PDFExtGState.newInstance(document), imageTransform);
    }

    /**
     * Add the text of the book to the PDF document.
     *
     * <p>
     * The input file is read, and each line is considered to be a paragraph. Paragraphs are added to a
     * {@link LayoutEngine}, where they are automatically flowed and create new pages at the end of the PDF document.
     *
     * @param pdfDoc The document to add the text to
     * @throws Exception a general exception was thrown
     */
    private static void addBookText(final PDFDocument pdfDoc) throws Exception {
        // Read in a text and add each line as separate paragraph
        try (LayoutEngine layoutEngine = new LayoutEngine(pdfDoc);
             InputStream is = MakeWhiteFangBook.class.getResourceAsStream("WhiteFang.chapter1.txt");
             InputStreamReader isr = new InputStreamReader(is, "UTF-8");
             BufferedReader br = new BufferedReader(isr)) {
            for (String line; (line = br.readLine()) != null;) {
                // The input file has blank lines between paragraphs, so we'll filter them out
                if (!line.matches("\\s*")) {
                    if (line.startsWith("# ")) {
                        addHeading1(layoutEngine, line.substring(2));
                    } else if (line.startsWith("## ")) {
                        addHeading2(layoutEngine, line.substring(3));
                    } else {
                        addOneParagraph(layoutEngine, line);
                    }
                }
            }
        }
    }

    /**
     * Add a level-1 heading to the layout.
     *
     * @param layoutEngine the layout engine
     * @param text the text to lay out
     */
    private static void addHeading1(final LayoutEngine layoutEngine, final String text) {
        final Heading h = new Heading(text);
        h.getStyle().setFontSize(new Length(36, Dimension.PT));
        h.getStyle().setMarginBottom(new Length(0, Dimension.PT));
        layoutEngine.add(h);
    }

    /**
     * Add a level-2 heading to the layout.
     *
     * @param layoutEngine the layout engine
     * @param text the text to lay out
     */
    private static void addHeading2(final LayoutEngine layoutEngine, final String text) {
        final Heading h = new Heading(text);
        layoutEngine.add(h);
    }

    /**
     * Add one paragraph to the document.
     *
     * @param layoutEngine the layout engine to add to
     * @param line the line from the original file to add
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFFontException there was an error in the font set or an individual font
     * @throws PDFConfigurationException there was a system problem configuring PDF support
     */
    private static void addOneParagraph(final LayoutEngine layoutEngine, final String line)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException,
                    PDFInvalidParameterException, PDFFontException, PDFConfigurationException {
        layoutEngine.add(new Paragraph(line));
    }
}

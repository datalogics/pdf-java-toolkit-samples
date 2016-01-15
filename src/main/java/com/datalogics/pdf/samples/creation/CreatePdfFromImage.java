/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.types.ASMatrix;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.graphics.PDFExtGState;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.services.imageconversion.ImageManager;

import com.datalogics.pdf.document.DocumentHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * This sample shows how to create a PDF document from an image file. The file formats demonstrated here include PNG,
 * JPG/JPEG, GIF, and BMP.
 */
public final class CreatePdfFromImage {
    private static final Logger LOGGER = Logger.getLogger(CreatePdfFromImage.class.getName());

    // Image from pixabay.com, public domain images
    public static final String INPUT_PNG = "PDF-Java-Toolkit-Icon.png";
    public static final String INPUT_JPG = "PDF-Java-Toolkit-Icon.jpg";
    public static final String INPUT_GIF = "PDF-Java-Toolkit-Icon.gif";
    public static final String INPUT_BMP = "PDF-Java-Toolkit-Icon.bmp";
    public static final String[] INPUT_ARRAY = { INPUT_PNG, INPUT_JPG, INPUT_GIF, INPUT_BMP };
    public static final String OUTPUT_PNG = "PDF_from_PNG.pdf";
    public static final String OUTPUT_JPG = "PDF_from_JPG.pdf";
    public static final String OUTPUT_GIF = "PDF_from_GIF.pdf";
    public static final String OUTPUT_BMP = "PDF_from_BMP.pdf";
    private static final Double PTS_PER_IN = 72.0; // points per inch

    /**
     * This is a utility class, and won't be instantiated.
     */
    private CreatePdfFromImage() {}

    /**
     * Main program for creating PDFs from images.
     *
     * @param args The name of the output file and the image file to be used
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If we have more than one argument, get the output destination, get the image name, and parse the format.
        // If we don't, just use the defaults included in the sample.
        if (args.length > 1) {
            final String outputFile = args[0];
            final String inputImage = args[1];
            final String[] split = inputImage.split("\\.");
            final String format = split[split.length - 1];
            final String[] supportedFormats = ImageIO.getReaderFileSuffixes();
            boolean supported = false;
            for (int i = 0; i < supportedFormats.length; i++) {
                if (supportedFormats[i].equalsIgnoreCase(format)) {
                    supported = true;
                    break;
                }
            }
            if (supported) {
                try {
                    createPdfFromImage(format.toUpperCase(Locale.ENGLISH), inputImage, outputFile);
                } catch (final Exception e) {
                    LOGGER.warning("There was a problem processing the image: " + e.getMessage());
                }
            } else {
                throw new Exception("Image format of " + format + "not supported");
            }
        } else {
            createPdfFromImage("BMP", INPUT_BMP, OUTPUT_BMP);
            createPdfFromImage("PNG", INPUT_PNG, OUTPUT_PNG);
            createPdfFromImage("JPG", INPUT_JPG, OUTPUT_JPG);
            createPdfFromImage("GIF", INPUT_GIF, OUTPUT_GIF);
        }
    }


    /**
     * Create a PDF document from a given image file.
     *
     * @param imageFormat The format the image is in - PNG, JPG, GIF, BMP are supported
     * @param inputImage The name of the image resource to use
     * @param outputPdf The name of the output file to be created
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws Exception a general exception was thrown
     */
    public static void createPdfFromImage(final String imageFormat, final String inputImage, final String outputPdf)
                    throws Exception {
        // Get the image for the reader to use. We'll try to use the sample's resource (default behavior) or, failing
        // that, we'll treat the input as a file to be opened. Set the BufferedImage to be used here as well.
        final BufferedImage bufferedImage;

        try (final InputStream resourceStream = CreatePdfFromImage.class.getResourceAsStream(inputImage)) {
            if (resourceStream == null) {
                bufferedImage = ImageIO.read(new File(inputImage));
            } else {
                bufferedImage = ImageIO.read(ImageIO.createImageInputStream(resourceStream));
                resourceStream.close();
            }
        }

        if (bufferedImage == null) {
            throw new IOException("Unable to read " + imageFormat + "image: " + inputImage);
        }

        // Fit the image to a 792pt by 612pt page, maintaining at least a 1/2 inch (72 pt) margin.
        Double newImageWidth;
        Double newImageHeight;
        final Double pageWidth;
        final Double pageHeight;
        final int imageWidth = bufferedImage.getWidth();
        final int imageHeight = bufferedImage.getHeight();
        final PDFDocument pdfDocument;
        if ((imageWidth / imageHeight) > 1) {
            // The image's width is greater than its height. Fit the height to fill an 11 x 8.5 page, then scale the
            // width. If the width goes off the page, fit the width to fill the page and then scale the height.
            newImageHeight = ASRectangle.US_LETTER.width() - PTS_PER_IN;
            newImageWidth = (newImageHeight / imageHeight) * imageWidth;
            if (newImageWidth > ASRectangle.US_LETTER.height() - PTS_PER_IN) {
                newImageWidth = ASRectangle.US_LETTER.height() - PTS_PER_IN;
                newImageHeight = (newImageWidth / imageWidth) * imageHeight;
            }
            // Create a letter sized PDF document in landscape mode.
            pdfDocument = PDFDocument.newInstance(new ASRectangle(0, 0, ASRectangle.US_LETTER.height(),
                                                  ASRectangle.US_LETTER.width()), PDFOpenOptions.newInstance());
            pageWidth = ASRectangle.US_LETTER.height();
            pageHeight = ASRectangle.US_LETTER.width();
        } else {
            // The image is square or its height is greater than its width. Fit the width to fill an 8.5 x 11 page,
            // then scale the height. If the height goes off the page, fit the height to fill the page and then scale
            // the width.
            newImageWidth = ASRectangle.US_LETTER.width() - PTS_PER_IN;
            newImageHeight = (newImageWidth / imageWidth) * imageHeight;
            if (newImageHeight > ASRectangle.US_LETTER.height() - PTS_PER_IN) {
                newImageHeight = ASRectangle.US_LETTER.height() - PTS_PER_IN;
                newImageWidth = (newImageHeight / imageHeight) * imageWidth;
            }
            // Create a letter sized PDF document in portrait mode.
            pdfDocument = PDFDocument.newInstance(ASRectangle.US_LETTER, PDFOpenOptions.newInstance());
            pageWidth = ASRectangle.US_LETTER.width();
            pageHeight = ASRectangle.US_LETTER.height();
        }

        // Convert the BufferedImage to a PDFXObjectImage
        final PDFXObjectImage image = ImageManager.getPDFImage(bufferedImage, pdfDocument);

        // Create a default external graphics state which describes how graphics are to be rendered on a device.
        final PDFExtGState pdfExtGState = PDFExtGState.newInstance(pdfDocument);

        // Create a transformation matrix which maps positions from user coordinates to device coordinates. Translate
        // so that the image is centered with at least a 1/2" margin.
        final double translateX = (pageWidth - newImageWidth) / 2.0;
        final double translateY = (pageHeight - newImageHeight) / 2.0;
        final ASMatrix asMatrix = ASMatrix.createIdentityMatrix().scale(newImageWidth, newImageHeight)
                                                                     .translate(translateX, translateY);

        // Now add the image to the first PDF page using the graphics state and the transformation matrix.
        ImageManager.insertImageInPDF(image, pdfDocument.requirePages().getPage(0), pdfExtGState, asMatrix);

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, outputPdf);
    }
}

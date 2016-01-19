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
import com.adobe.pdfjt.services.manipulations.PMMOptions;
import com.adobe.pdfjt.services.manipulations.PMMService;

import com.datalogics.pdf.document.DocumentHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageIO;

/**
 * This sample shows how to create a PDF document from an image file. The file formats demonstrated here include PNG,
 * JPG/JPEG, GIF, and BMP.
 */
public final class CreatePdfFromImage {

    public static final String INPUT_PNG = "PDF-Java-Toolkit-Icon.png";
    public static final String INPUT_JPG = "PDF-Java-Toolkit-Icon.jpg";
    public static final String INPUT_GIF = "PDF-Java-Toolkit-Icon.gif";
    public static final String INPUT_BMP = "PDF-Java-Toolkit-Icon.bmp";
    public static final String OUTPUT_PDF = "PDF_from_Images.pdf";
    private static final Double PTS_PER_IN = 72.0; // points per inch

    /**
     * This is a utility class, and won't be instantiated.
     */
    private CreatePdfFromImage() {}

    /**
     * Main program for creating a PDF from images. If passed more than one image, each image will make become new page
     * in the document.
     *
     * @param args The name of the output file and a number of images (1 or more) to be used to create a PDF
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        PDFDocument outputDocument = null;
        final String outputFile;
        // If we have more than one argument, get the output destination, get the image names, and parse the formats.
        // If we don't, just use the defaults included in the sample.
        if (args.length > 1) {
            outputFile = args[0];
            for (int i = 1; i < args.length; i++) {
                final String inputImage = args[i];
                final String[] split = inputImage.split("\\.");
                final String format = split[split.length - 1];
                final String[] supportedFormats = ImageIO.getReaderFileSuffixes();
                boolean supported = false;
                for (int j = 0; j < supportedFormats.length; j++) {
                    if (supportedFormats[j].equalsIgnoreCase(format)) {
                        supported = true;
                        break;
                    }
                }
                if (supported) {
                    outputDocument = createPdfFromImage(format.toUpperCase(Locale.ENGLISH), inputImage,
                                                            outputDocument);
                } else {
                    throw new PDFInvalidParameterException("Image format of " + format
                                                           + " not supported. Valid image formats are JPG/JPEG"
                                                           + ", PNG, BMP, and GIF.");
                }
            }
        } else {
            outputDocument = createPdfFromImage("BMP", INPUT_BMP, null);
            outputDocument = createPdfFromImage("PNG", INPUT_PNG, outputDocument);
            outputDocument = createPdfFromImage("JPG", INPUT_JPG, outputDocument);
            outputDocument = createPdfFromImage("GIF", INPUT_GIF, outputDocument);
            outputFile = OUTPUT_PDF;
        }
        DocumentHelper.saveFullAndClose(outputDocument, outputFile);
    }


    /**
     * Create a PDF document from a given image file.
     *
     * @param imageFormat The format the image is in - PNG, JPG, GIF, BMP are supported
     * @param inputImage The name of the image resource to use
     * @param inputPdf If the output of this call should be appended to an existing PDF, pass it here, otherwise use
     *        null
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws Exception a general exception was thrown
     */
    public static PDFDocument createPdfFromImage(final String imageFormat, final String inputImage,
                                                 final PDFDocument inputPdf) throws Exception {
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
        PDFDocument pdfDocument;
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
            if (inputPdf != null) {
                final PMMService pmmService = new PMMService(inputPdf);
                pmmService.appendPages(pdfDocument, "", PMMOptions.newInstanceAll());
                pdfDocument = inputPdf;
            }

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

            if (inputPdf != null) {
                final PMMService pmmService = new PMMService(inputPdf);
                pmmService.appendPages(pdfDocument, "", PMMOptions.newInstanceAll());
                pdfDocument = inputPdf;
            }

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
        final int numPages = pdfDocument.requirePages().getCount();
        ImageManager.insertImageInPDF(image, pdfDocument.requirePages().getPage(numPages - 1), pdfExtGState, asMatrix);

        return PDFDocument.newInstance(pdfDocument.finish());
    }
}

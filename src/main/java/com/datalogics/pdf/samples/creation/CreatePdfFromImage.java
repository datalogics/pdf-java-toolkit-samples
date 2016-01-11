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
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

/**
 * This sample shows how to create a PDF document from an image file. The file formats demonstrated here include PNG,
 * JPG/JPEG, GIF, and BMP.
 */
public final class CreatePdfFromImage {

    // Image from pixabay.com, public domain images
    public static final String inputPng = "ducky.png";
    public static final String inputJpg = "ducky.jpg";
    public static final String inputGif = "ducky.gif";
    public static final String inputBmp = "ducky.bmp";
    public static final String outputPng = "PDF_from_PNG.pdf";
    public static final String outputJpg = "PDF_from_JPG.pdf";
    public static final String outputGif = "PDF_from_GIF.pdf";
    public static final String outputBmp = "PDF_from_BMP.pdf";
    private static final Double ptsPerIn = 72.0; // points per inch

    /**
     * This is a utility class, and won't be instantiated.
     */
    private CreatePdfFromImage() {}

    /**
     * Main program for creating PDFs from images.
     *
     * @param args The name of the output file and the image file to be used
     * @throws Exception Throws a general exception
     */
    public static void main(final String... args) throws Exception {
        // If we have more than one argument, get the output destination, get the image name, and parse the format.
        // If we don't, just use the defaults included in the sample.
        if (args.length > 1) {
            final String outputFile = args[0];
            final String inputImage = args[1];
            final String[] split = inputImage.split("\\.");
            final String format = split[split.length - 1];
            if ("JPG".equalsIgnoreCase(format)
                || "JPEG".equalsIgnoreCase(format)
                || "GIF".equalsIgnoreCase(format)
                || "PNG".equalsIgnoreCase(format)
                || "BMP".equalsIgnoreCase(format)) {
                createPdfFromImage(format.toUpperCase(Locale.ENGLISH), inputImage, outputFile);
            } else {
                throw new Exception("Image format of " + format + "not supported");
            }
        } else {
            createPdfFromImage("JPG", inputJpg, outputJpg);
            createPdfFromImage("PNG", inputPng, outputPng);
            createPdfFromImage("GIF", inputGif, outputGif);
            createPdfFromImage("BMP", inputBmp, outputBmp);
        }
    }


    /**
     * Create a PDF document from a given image file.
     *
     * @param imageFormat The format the image is in - PNG, JPG, GIF, BMP are supported
     * @param inputImage The name of the image resource to use
     * @param outputPdf The name of the output file to be created
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFInvalidParameterException one or more parameters passed were invalid
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws Exception A general exception was thrown
     */
    public static void createPdfFromImage(final String imageFormat, final String inputImage, final String outputPdf)
                    throws Exception {
        // Get an image reader for the given format. We'll use this to look at image metadata.
        ImageReader reader = null;
        final Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(imageFormat);
        if (imageReaders.hasNext()) {
            reader = imageReaders.next();
        }

        // If imageReaders is empty, or if we somehow got a null value out of it, stop here.
        if (reader == null) {
            throw new IOException("Unable to get a " + imageFormat + " image reader");
        }

        // Get the image for the reader to use. We'll try to use the sample's resource (default behavior) or, failing
        // that, we'll treat the input as a file to be opened. Set the BufferedImage to be used here as well.
        final BufferedImage bufferedImage;
        final InputStream resourceStream = CreatePdfFromImage.class.getResourceAsStream(inputImage);
        try {
            if (resourceStream == null) {
                reader.setInput(ImageIO.createImageInputStream(new File(inputImage)));
                bufferedImage = ImageIO.read(new File(inputImage));
            } else {
                reader.setInput(ImageIO
                                   .createImageInputStream(CreatePdfFromImage.class.getResourceAsStream(inputImage)));
                bufferedImage = ImageIO.read(ImageIO
                                   .createImageInputStream(CreatePdfFromImage.class.getResourceAsStream(inputImage)));
                resourceStream.close();
            }
        } finally {
            if (resourceStream != null) {
                resourceStream.close();
            }
        }

        if (resourceStream != null) {
            resourceStream.close();
        }

        // Fit the image to a 792pt by 612pt page, maintaining at least a 1/2 inch (72 pt) margin.
        final Double pageWidth;
        final Double pageHeight;
        final int w = bufferedImage.getWidth();
        final int h = bufferedImage.getHeight();
        if ((w / h) >= 1) {
            pageWidth = ASRectangle.US_LETTER.height() - ptsPerIn;
            pageHeight = (h * (ASRectangle.US_LETTER.height() - ptsPerIn)) / w;
        } else {
            pageHeight = ASRectangle.US_LETTER.height() - ptsPerIn;
            pageWidth = (w * (ASRectangle.US_LETTER.height() - ptsPerIn)) / h;
        }

        // Create a PDF document with the first page being the proper size to contain our image.
        final PDFDocument pdfDocument = PDFDocument.newInstance(ASRectangle.US_LETTER, PDFOpenOptions.newInstance());

        // Convert the BufferedImage to a PDFXObjectImage
        final PDFXObjectImage image = ImageManager.getPDFImage(bufferedImage, pdfDocument);

        // Create a default external graphics state which describes how graphics are to be rendered on a device.
        final PDFExtGState pdfExtGState = PDFExtGState.newInstance(pdfDocument);

        // Create a transformation matrix which maps positions from user coordinates to device coordinates. We're just
        // translating the image 1/2 inch from the origin to get a margin.
        final ASMatrix asMatrix = new ASMatrix(pageWidth, 0, 0, pageHeight, ptsPerIn / 2, ptsPerIn / 2);

        // Now add the image to the first PDF page using the graphics state and the transformation matrix.
        ImageManager.insertImageInPDF(image, pdfDocument.requirePages().getPage(0), pdfExtGState, asMatrix);

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, outputPdf);
    }
}
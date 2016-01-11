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

import org.w3c.dom.NodeList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

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
    private static final Double mmPerIn = 25.4; // millimeters per inch
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
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException,
                    PDFInvalidParameterException, IOException {
        // Get an image reader for the given format. We'll use this to look at image metadata.
        ImageReader reader = null;
        final Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(imageFormat);
        if (imageReaders.hasNext()) {
            reader = imageReaders.next();
        }

        // If imageReaders is empty, or if we somehow got a null value out of it, stop here.
        if (reader == null) {
            throw new PDFIOException("Unable to get a " + imageFormat + " reader");
        }

        // Get the image for the reader to use. We'll try to use the sample's resource (default behavior) or, failing
        // that, we'll treat the input as a file to be opened. Set the BufferedImage to be used here as well.
        final BufferedImage bufferedImage;
        final InputStream resourceStream;

        resourceStream = CreatePdfFromImage.class.getResourceAsStream(inputImage);
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


        // Try to find the pixel density from the metadata. If it's missing, we'll just have to make do without. We'll
        // maintain the aspect ratio of the image while fitting it within a basic 612px x 792px page.
        final Double pixelsPerMm = getPixelsPerMm(reader.getImageMetadata(0));
        final Double pageWidth;
        final Double pageHeight;
        if (pixelsPerMm == -1) {
            final int w = bufferedImage.getWidth();
            final int h = bufferedImage.getHeight();
            if ((w / h) >= 1) {
                pageWidth = 792.0;
                pageHeight = (h * 792.0) / w;
            } else {
                pageHeight = 792.0;
                pageWidth = (w * 792.0) / h;
            }
        } else {
            // Convert pixels per millimeter to points
            pageWidth = Math.floor((ptsPerIn * bufferedImage.getWidth()) / (pixelsPerMm * mmPerIn));
            pageHeight = Math.floor((ptsPerIn * bufferedImage.getHeight()) / (pixelsPerMm * mmPerIn));
        }

        // Create a PDF document with the first page being the proper size to contain our image.
        final PDFDocument pdfDocument = PDFDocument.newInstance(new ASRectangle(new double[] { 0, 0, pageWidth,
            pageHeight }), PDFOpenOptions.newInstance());

        // Convert the BufferedImage to a PDFXObjectImage
        final PDFXObjectImage image = ImageManager.getPDFImage(bufferedImage, pdfDocument);

        // Create a default external graphics state which describes how graphics are to be rendered on a device.
        final PDFExtGState pdfExtGState = PDFExtGState.newInstance(pdfDocument);

        // Create a transformation matrix which maps positions from user coordinates to device coordinates. There is no
        // transform taking place here though but it is a required parameter.
        final ASMatrix asMatrix = new ASMatrix(pageWidth, 0, 0, pageHeight, 0, 0);

        /*
         * Now add the image to the first PDF page using the graphics state and the transformation matrix.
         */
        ImageManager.insertImageInPDF(image, pdfDocument.requirePages().getPage(0), pdfExtGState, asMatrix);

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, outputPdf);
    }

    private static Double getPixelsPerMm(final IIOMetadata metadata) {
        // This code assumes square pixels so it only gets the horizontal measurement
        final IIOMetadataNode standardTree = (IIOMetadataNode) metadata
                        .getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        final NodeList pixelSizes = standardTree.getElementsByTagName("HorizontalPixelSize");
        // If there is no "HorizontalPixelSize" element, return now. We'll need to handle this later.
        if (pixelSizes.getLength() == 0) {
            return -1.0;
        }
        final IIOMetadataNode pixelSize = (IIOMetadataNode) pixelSizes.item(0);
        return Double.parseDouble(pixelSize.getAttribute("value"));
    }
}
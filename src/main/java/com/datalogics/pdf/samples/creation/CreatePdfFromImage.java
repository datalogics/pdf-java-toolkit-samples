/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

/**
 *
 */
public class CreatePdfFromImage {

    // Image from pixabay.com, public domain images
    private static final String inputPng = "ducky.png";
    private static final String inputJpg = "ducky.jpg";
    private static final String inputGif = "ducky.gif";
    private static final String inputBmp = "ducky.bmp";
    private static final String outputPng = "PDF_from_PNG.pdf";
    private static final String outputJpg = "PDF_from_JPG.pdf";
    private static final String outputGif = "PDF_from_GIF.pdf";
    private static final String outputBmp = "PDF_from_BMP.pdf";
    private static final Double mmPerIn = 25.4; // millimeters per inch
    private static final Double ptsPerIn = 72.0; // points per inch

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        createPdfFromImage("JPG", inputJpg, outputJpg);
        createPdfFromImage("PNG", inputPng, outputPng);
        createPdfFromImage("GIF", inputGif, outputGif);
        createPdfFromImage("BMP", inputBmp, outputBmp);
    }


    /**
     * Create a PDF document from a given image file.
     *
     * @param imageFormat The format the image is in - PNG, JPG, GIF, BMP are supported
     * @param inputImage The name of the image resource to use
     * @param outputPdf The name of the output file to be created
     * @throws Exception A general exception was thrown
     */
    public static void createPdfFromImage(final String imageFormat, final String inputImage, final String outputPdf)
                    throws Exception {
        ImageReader reader = null;
        final Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(imageFormat);
        if (imageReaders.hasNext()) {
            reader = imageReaders.next();
        } else {
            throw new Exception("A " + imageFormat + " reader isn't available");

        }
        reader.setInput(ImageIO.createImageInputStream(CreatePdfFromImage.class.getResourceAsStream(inputImage)));
        final BufferedImage bufferedImage = ImageIO
                        .read(ImageIO.createImageInputStream(CreatePdfFromImage.class.getResourceAsStream(inputImage)));
        // Try to find the pixel density from the metadata. If it's missing, we'll just have to make do without. We'll
        // maintain the aspect ratio of the image while fitting it within the typical definition of an 8.5" x 11" page.
        final Double pixelsPerMm = getPixelsPerMm(reader.getImageMetadata(0));
        final Double pageWidth;
        final Double pageHeight;
        if (pixelsPerMm == -1) {
            final int w = bufferedImage.getWidth();
            final int h = bufferedImage.getHeight();
            if ((w / h) > 1) {
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

        System.out.println("Width: " + pageWidth + " Height: " + pageHeight);

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

        System.out.println("Page 1 image added.");

        // Save the file.
        DocumentHelper.saveFullAndClose(pdfDocument, outputPdf);
    }

    private static Double getPixelsPerMm(final IIOMetadata metadata) {
        // This code assumes square pixels so it only gets the horizontal measurement
        final IIOMetadataNode standardTree = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        final NodeList pixelSizes = standardTree.getElementsByTagName("HorizontalPixelSize");
        if (pixelSizes.getLength() == 0) {
            return -1.0;
        }
        final IIOMetadataNode pixelSize = (IIOMetadataNode) pixelSizes.item(0);
        return Double.parseDouble(pixelSize.getAttribute("value"));
    }

    private static Double getJpegPixelDensity() {

        return 0.0;
    }

}
/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        final URL outputFileUrl;
        // If we have more than one argument, get the output destination, get the image names, and parse the formats.
        // If we don't, just use the defaults included in the sample.
        if (args.length > 1) {
            outputFileUrl = new URL(args[0]);
            final List<URL> inputImages = new ArrayList<URL>();
            for (int i = 1; i < args.length; i++) {
                inputImages.add(createUrlFromString(args[i]));
            }
            createPdfFromImages(inputImages, outputFileUrl);
        } else {
            final URL bmpImageUrl = CreatePdfFromImage.class.getResource(INPUT_BMP);
            final URL pngImageUrl = CreatePdfFromImage.class.getResource(INPUT_PNG);
            final URL jpgImageUrl = CreatePdfFromImage.class.getResource(INPUT_JPG);
            final URL gifImageUrl = CreatePdfFromImage.class.getResource(INPUT_GIF);

            final List<URL> inputImages = Arrays.asList(bmpImageUrl, pngImageUrl, jpgImageUrl, gifImageUrl);

            outputFileUrl = new File(OUTPUT_PDF).toURI().toURL();

            createPdfFromImages(inputImages, outputFileUrl);
        }
    }

    /**
     * Create a PDF document from a given list of image files.
     *
     * @param inputImages The list of inputImage URLs to use
     * @param outputPdfUrl The output PDF URL
     * @throws Exception a general exception was thrown
     */
    public static void createPdfFromImages(final List<URL> inputImages, final URL outputPdfUrl) throws Exception {
        PDFDocument outputDocument = null;

        for (final URL inputImage : inputImages) {
            final String format = getImageFormatFromUrl(inputImage);
            if (isImageFormatSupported(format)) {
                outputDocument = createPdfFromImage(inputImage,
                                                    outputDocument);
            } else {
                throw new PDFInvalidParameterException("Image format of " + format
                                                       + " not supported. Valid image formats are JPG/JPEG"
                                                       + ", PNG, BMP, and GIF.");
            }
        }

        DocumentHelper.saveFullAndClose(outputDocument, outputPdfUrl.toURI().getPath());
    }

    /**
     * Create a PDF document from a given image file. PNG, JPG, GIF, BMP are supported
     *
     * @param inputImageUrl The image URL to use
     * @param outputPdfUrl The output URL to use
     * @throws Exception a general exception was thrown
     */
    public static void createPdfFromImageAndSave(final URL inputImageUrl,
                                                 final URL outputPdfUrl) throws Exception {
        final PDFDocument outputDocument = createPdfFromImage(inputImageUrl, null);

        DocumentHelper.saveFullAndClose(outputDocument, outputPdfUrl.toURI().getPath());
    }

    /**
     * Create a PDF document from a given image file. PNG, JPG, GIF, BMP are supported
     *
     * @param inputImageUrl The image URL to use
     * @param inputPdf If the output of this call should be appended to an existing PDF, pass it here, otherwise use
     *        null
     * @throws Exception a general exception was thrown
     */
    public static PDFDocument createPdfFromImage(final URL inputImageUrl,
                                                 final PDFDocument inputPdf) throws Exception {
        // Get the image for the reader to use. We'll try to use the sample's resource (default behavior) or, failing
        // that, we'll treat the input as a file to be opened. Set the BufferedImage to be used here as well.
        final BufferedImage bufferedImage;

        try (final InputStream resourceStream = inputImageUrl.openStream()) {
            if (resourceStream == null) {
                bufferedImage = ImageIO.read(new File(inputImageUrl.toURI().getPath()));
            } else {
                bufferedImage = ImageIO.read(ImageIO.createImageInputStream(resourceStream));
                resourceStream.close();
            }
        }

        if (bufferedImage == null) {
            throw new IOException("Unable to read " + getImageFormatFromUrl(inputImageUrl) + "image: "
                                  + inputImageUrl.toString());
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

    private static String getImageFormatFromUrl(final URL imagePath) throws URISyntaxException {
        final String imageStringPath = imagePath.toString();

        final String[] split = imageStringPath.split("\\.");

        return split[split.length - 1];
    }

    private static boolean isImageFormatSupported(final String imageFormat) throws URISyntaxException {
        final String[] supportedFormats = ImageIO.getReaderFileSuffixes();

        final Set<String> supportedFormatsSet = new HashSet<String>(Arrays.asList(supportedFormats));

        return supportedFormatsSet.contains(imageFormat);
    }

    private static URL createUrlFromString(final String inputString) throws MalformedURLException {
        return new URL(inputString);
    }
}

/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.images;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.image.Resampler;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObject;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectMap;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.imageconversion.ImageManager;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;

import java.net.URI;
import java.util.Iterator;

/**
 * This sample demonstrates how to downsample images that are in a PDF and replace the originals with the downsampled
 * versions.
 * <p>
 * The input PDFXObjectImage is down-sampled using the Nearest Neighbor downsampling method by default.
 * </p>
 * Supported downsampling methods are:
 * <ul>
 * <li>Resampler.kResampleNearestNeighbor: Nearest Neighbor downsampling.</li>
 * <li>Resampler.kResampleBicubic: Bi-cubic downsampling.</li>
 * <li>Resampler.kResampleLinear: Linear downsampling.</li>
 * </ul>
 */
public final class ImageDownsampling {

    private static final String INPUT_IMAGE_PATH = "ducky.pdf";
    private static final String OUTPUT_IMAGE_PATH = "downsampled_ducky_";

    private static final int DEFAULT_SAMPLING_METHOD = Resampler.kResampleNearestNeighbor;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private ImageDownsampling() {}

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

        String path;
        int method;
        if (args.length > 1) {
            path = args[0];
            try {
                method = Integer.parseInt(args[1]);
            } catch (final NumberFormatException e) {
                method = DEFAULT_SAMPLING_METHOD;
            }
        } else {
            path = OUTPUT_IMAGE_PATH;
            method = DEFAULT_SAMPLING_METHOD;
        }

        final String inputPath = new URI(ImageDownsampling.class.getResource(INPUT_IMAGE_PATH).toString()).getPath();
        final PDFDocument pdfDoc = DocumentUtils.openPdfDocument(inputPath);
        downsampleImage(pdfDoc, method);
        DocumentHelper.saveFullAndClose(pdfDoc, path + getResampleMethodString(method) + ".pdf");
    }


    /**
     * This method is used to downsample an image using a valid resampler method.
     *
     * @param pdfDoc PDFDocument
     * @param method Valid resampler method
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     */
    public static void downsampleImage(final PDFDocument pdfDoc, final int method)
                    throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException {
        final double scaleFactor = 0.5;
        /*
         * Downsample all images in the doc and replace the original images with the resampled images.
         */
        final Iterator<PDFPage> pagesIter = pdfDoc.requirePages().iterator();
        while (pagesIter.hasNext()) {
            final PDFPage pdfPage = pagesIter.next();
            final PDFResources pdfResources = pdfPage.getResources();
            if (pdfResources != null) {
                final PDFXObjectMap xobjMap = pdfResources.getXObjectMap();
                if (xobjMap != null) {
                    final Iterator<ASName> xobjIter = xobjMap.keySet().iterator();
                    while (xobjIter.hasNext()) {
                        final ASName key = xobjIter.next();
                        final PDFXObject xobj = xobjMap.get(key);
                        if (xobj instanceof PDFXObjectImage) {
                            final PDFXObjectImage originalImage = (PDFXObjectImage) xobj;
                            final PDFXObjectImage resampledImage = ImageManager.resampleXObjImage(pdfDoc,
                                                                                                  originalImage,
                                                                                                  scaleFactor,
                                                                                                  scaleFactor,
                                                                                                  method);
                            xobjMap.set(key, resampledImage);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a valid string resampler method representation.
     *
     * @param method Numeric resampler method
     * @return String
     */
    public static String getResampleMethodString(final int method) {
        final String[] methodStrings = new String[] { "**invalid**", "NearestNeighbor", "Bicubic", "Linear" };
        return methodStrings[method];
    }
}

/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.internal.io.ByteWriter;
import com.adobe.internal.io.RandomAccessFileByteWriter;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveOptions;
import com.adobe.pdfjt.pdf.filters.PDFFilterFlate;
import com.adobe.pdfjt.pdf.filters.PDFFilterList;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFColorSpaceICCBased;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFICCProfile;
import com.adobe.pdfjt.pdf.graphics.colorspaces.PDFRenderingIntent;
import com.adobe.pdfjt.pdf.interchange.prepress.PDFOutputIntent;
import com.adobe.pdfjt.services.pdfa.PDFAConformanceLevel;
import com.adobe.pdfjt.services.pdfa.PDFAConversionOptions;
import com.adobe.pdfjt.services.pdfa.PDFADefaultConversionHandler;
import com.adobe.pdfjt.services.pdfa.PDFAErrorSetFileStructure;
import com.adobe.pdfjt.services.pdfa.PDFAOCConversionMode;
import com.adobe.pdfjt.services.pdfa.PDFAService;
import com.adobe.pdfjt.services.pdfa.error.PDFAFileStructureErrorCode;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.EnumSet;
import java.util.logging.Logger;

/**
 * This utility class opens a document and uses the PDFAService to convert it to a PDF/A-1b document.
 * PDFAConversionHandler derives from a default implementation (i.e., PDFADefaultConversionHandler). The non-public
 * class MyPDFAConversionHandler, only overrides those methods that it needs.
 *
 */
public final class ConvertPdfA1Util {
    private static final Logger LOGGER = Logger.getLogger(ConvertPdfA1Util.class.getName());

    // constants for setting color space profiles
    private static final String RGB_PROFILE = "sRGB Color Space Profile.icm";
    private static final String CMYK_PROFILE = "USWebCoatedSWOP.icc";
    private static final String GRAY_PROFILE = "AdobeGray20.icm";

    private static ICC_Profile iccRGBProfile;
    private static ICC_Profile iccCMYKProfile;
    private static ICC_Profile iccGrayProfile;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private ConvertPdfA1Util() {}

    /**
     * In this method we take an input PDF Document and attempt to convert it into PDF/A-1b format using PDFAService.
     *
     * @param pdfDoc - PDF Document to be converted
     * @param outputFilePath - name and path to the output file
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     */
    public static void convertPdfA1(final PDFDocument pdfDoc, final String outputFilePath)
                    throws IOException, PDFInvalidDocumentException, PDFIOException, PDFSecurityException,
                    PDFInvalidParameterException, PDFUnableToCompleteOperationException {

        ByteWriter writer = null;

        try {
            // Load up icc profiles for the default colorspaces.
            iccRGBProfile = ICC_Profile.getInstance(ConvertPdfA1Util.class.getResourceAsStream(RGB_PROFILE));
            iccCMYKProfile = ICC_Profile.getInstance(ConvertPdfA1Util.class.getResourceAsStream(CMYK_PROFILE));
            iccGrayProfile = ICC_Profile.getInstance(ConvertPdfA1Util.class.getResourceAsStream(GRAY_PROFILE));

            // Setup the conversion options and handler.
            final PDFAConversionOptions options = createConversionOptions(pdfDoc);
            final MyPdfAConversionHandler handler = new MyPdfAConversionHandler();

            handler.setDefaultColorSpaceProfiles(iccRGBProfile, iccCMYKProfile, iccGrayProfile);

            // attempt to convert the PDF to PDF/A-1b
            if (PDFAService.convert(pdfDoc, PDFAConformanceLevel.Level_1b, options, handler)) {
                final PDFSaveOptions saveOpt = PDFSaveFullOptions.newInstance();

                // if the pdf contains compressed object streams, we should
                // decompress these so that the pdf can be converted to PDF/A-1b
                if (handler.requiresObjectDecompression()) {
                    saveOpt.setObjectCompressionMode(PDFSaveOptions.OBJECT_COMPRESSION_NONE);
                }

                final RandomAccessFile outputPdf = new RandomAccessFile(outputFilePath, "rw");
                writer = new RandomAccessFileByteWriter(outputPdf);

                pdfDoc.save(writer, saveOpt);

                final String successMsg = "\nConverted output written to: " + outputFilePath;
                LOGGER.info(successMsg);
            } else {
                LOGGER.info("Errors encountered when converting document.");
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }
    }


    /**
     * This method sets the options for the pdf document that is to be converted. Such options include removing certain
     * annotations and javascript from the pdf.
     *
     * @param doc - PDF document to be converted
     * @return PDFAConversionOptions
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    private static PDFAConversionOptions createConversionOptions(final PDFDocument doc)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {

        final PDFAConversionOptions options = new PDFAConversionOptions();

        setPdfAOutputIntent(doc, options);
        setDefaultColorSpaces(doc, options);
        options.setUpdatePDFAMetadataOnPartialConversion(false);
        options.setOCConversionMode(PDFAOCConversionMode.ConvertOCUsingDefaultConfigIncludingUsageApp, ASName.k_View);
        options.setRemoveIllegalAnnotations(true);
        options.setRemoveInvisibleNonStandardAnnots(true);
        options.setRemoveHiddenAnnots(true);
        options.setRemoveNoViewAnnots(true);
        options.setOverrideAnnotationFlags(true);
        options.setRemoveNonNormalAnnotAppearances(true);
        options.setRemoveIllegalActions(true);
        options.setRemoveIllegalAdditionalActions(true);
        options.setRemoveJavaScriptNameTree(true);
        options.setOverrideRenderingIntent(PDFRenderingIntent.RELATIVE_COLORIMETRIC);
        options.setRemoveFormXObjectPSData(true);
        options.setRemoveIllegalInterpolation(true);
        options.setRemoveImageAlternates(true);
        options.setRemoveTransferFunctions(true);
        options.setRemoveXFA(true);
        options.setRemoveXObjectOPI(true);
        options.setRemoveEmbeddedFilesNameTree(true);
        options.setShouldEmbedFonts(true);
        options.setRemoveInvalidXMPProperties(true);

        final PDFFilterList list = PDFFilterList.newInstance(doc);
        list.add(PDFFilterFlate.newInstance(doc, null));
        options.setLZWReplacementFilterList(list);

        return options;
    }

    /**
     * Set the output intent for the PDF/A document. Output intent indicates the color characteristics of output devices
     * where the PDF document might be rendered. See “Output Intents” in the ISO 32000:2008 PDF Reference, page 633.
     *
     * @param doc - PDF document to be converted into PDF/A-1b format
     * @param options - PDFA conversion options
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws IOException an I/O operation failed or was interrupted
     * @throws Exception a general exception was thrown
     */
    private static void setPdfAOutputIntent(final PDFDocument doc, final PDFAConversionOptions options)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException, IOException {

        final PDFOutputIntent outputIntent = PDFOutputIntent.newInstance(doc, "GTS_PDFA1", "CGATS TR 001");
        outputIntent.setOutputCondition("U.S. Web Coated(SWOP)v2");
        outputIntent.setRegistryName("http://www.color.org");

        final ICC_Profile profile = ICC_Profile.getInstance(ConvertPdfA1Util.class.getResourceAsStream(RGB_PROFILE));
        final PDFICCProfile destProfile = PDFICCProfile.newInstance(doc, profile);
        outputIntent.setDestOutputProfile(destProfile);
        outputIntent.setOutputConditionIdentifier("CGATS TR 001");
        options.setPDFAOutputIntent(outputIntent, false);
    }

    /**
     * Sets the default color spaces for graphics objects that relate to RGB, CMYK, or Grayscale color models.
     *
     * @param doc - PDF document to be converted into PDF/A-1b format
     * @param options - PDFA conversion options
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     */
    private static void setDefaultColorSpaces(final PDFDocument doc, final PDFAConversionOptions options)
                    throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException {

        final PDFColorSpaceICCBased defaultRgbProfile = PDFColorSpaceICCBased.newInstance(doc, iccRGBProfile);
        final PDFColorSpaceICCBased defaultCmykProfile = PDFColorSpaceICCBased.newInstance(doc, iccCMYKProfile);
        final PDFColorSpaceICCBased defaultGrayProfile = PDFColorSpaceICCBased.newInstance(doc, iccGrayProfile);
        options.setDefaultColorSpaces(defaultRgbProfile, defaultCmykProfile, defaultGrayProfile);
    }

    /**
     * Static inner class MyPDFAConversionHandler overrides the methods from PDFAConversionHandler that it needs.
     *
     */
    private static class MyPdfAConversionHandler extends PDFADefaultConversionHandler {
        private static final Logger LOGGER = Logger.getLogger(MyPdfAConversionHandler.class.getName());

        private boolean decompressObjectStreams;

        private ICC_Profile iccRgbProfile;
        private ICC_Profile iccCmykProfile;
        private ICC_Profile iccGrayProfile;

        /**
         * Sets ICC Color Space Profiles.
         *
         * @param rgbProfile - ICC_Profile
         * @param cmykProfile - ICC_Profile
         * @param grayProfile - ICC_Profile
         */
        public void setDefaultColorSpaceProfiles(final ICC_Profile rgbProfile,
                                                 final ICC_Profile cmykProfile, final ICC_Profile grayProfile) {
            iccRgbProfile = rgbProfile;
            iccCmykProfile = cmykProfile;
            iccGrayProfile = grayProfile;
        }

        /**
         * If a PDF contains compressed object streams, they need to be decompressed, as PDF/A-1 doesn't convert PDFs
         * with these compressed streams. This method returns true if compressed object streams exist in the PDF.
         *
         * @return true (need to decompress) or false
         */
        public boolean requiresObjectDecompression() {
            return this.decompressObjectStreams;
        }

        /**
         * After running the file structure validation and conversion this is the mechanism where we report the errors
         * we found and fixed.
         *
         * @param errorsFound - errors found during file structure validation
         * @param errorsFixed - errors that will be fixed with a save or were fixed during conversion.
         * @return true
         */
        @Override
        public boolean fileStructureErrorsFixed(final PDFAErrorSetFileStructure errorsFound,
                                                final PDFAErrorSetFileStructure errorsFixed) {

            @SuppressWarnings("unchecked")
            final EnumSet<PDFAFileStructureErrorCode> errorCodes = errorsFound.getErrorCodes();
            if (errorCodes.contains(PDFAFileStructureErrorCode.nonCompressedXRefNotPresent)) {
                // this problem is resolved in a post-processing step.
                this.decompressObjectStreams = true;
                errorsFound.unSetErrorFlag(PDFAFileStructureErrorCode.nonCompressedXRefNotPresent);
            }
            return true;
        }


        /**
         * Method to get a new valid PDFICCProfile from a given PDFICCProfile based on the number of components.
         *
         * @param pdficcProfile - the PDFICCProfile that will be replicated
         * @return PDFICCProfile
         */
        @Override
        public PDFICCProfile getValidICCProfile(final PDFICCProfile pdficcProfile) {
            PDFICCProfile fixedProfile = null;

            try {
                if (pdficcProfile.getNumberOfComponents() == 1) {
                    fixedProfile = PDFICCProfile.newInstance(pdficcProfile.getPDFDocument(), iccGrayProfile);
                }

                if (pdficcProfile.getNumberOfComponents() == 2) {
                    fixedProfile = PDFICCProfile.newInstance(pdficcProfile.getPDFDocument(), iccRgbProfile);
                }

                if (pdficcProfile.getNumberOfComponents() == 4) {
                    fixedProfile = PDFICCProfile.newInstance(pdficcProfile.getPDFDocument(), iccCmykProfile);
                }

            } catch (PDFIOException | PDFSecurityException | PDFInvalidDocumentException e) {
                LOGGER.severe(e.getMessage());
            }

            if (fixedProfile == null) {
                return null;
            } else {
                return fixedProfile;
            }

        }
    }
}



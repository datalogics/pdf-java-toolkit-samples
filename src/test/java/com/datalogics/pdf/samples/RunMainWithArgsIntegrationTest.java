/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples;

import static org.reflections.ReflectionUtils.getMethods;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withName;
import static org.reflections.ReflectionUtils.withParameters;

import com.datalogics.pdf.samples.creation.HelloWorld;
import com.datalogics.pdf.samples.creation.MakePdfFromImage;
import com.datalogics.pdf.samples.creation.MakeWhiteFangBook;
import com.datalogics.pdf.samples.extraction.TextExtract;
import com.datalogics.pdf.samples.forms.FillForm;
import com.datalogics.pdf.samples.images.ImageDownsampling;
import com.datalogics.pdf.samples.manipulation.ConvertPdfDocument;
import com.datalogics.pdf.samples.manipulation.FlattenPdf;
import com.datalogics.pdf.samples.manipulation.MergeDocuments;
import com.datalogics.pdf.samples.manipulation.RedactAndSanitizeDocument;
import com.datalogics.pdf.samples.printing.FakePrintService;
import com.datalogics.pdf.samples.printing.FakePrinterJob;
import com.datalogics.pdf.samples.printing.PrintPdf;
import com.datalogics.pdf.samples.signature.SignDocument;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mockit.Mock;
import mockit.MockUp;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

/**
 * As an integration test, run all the main methods in the {@code com.datalogics.pdf.samples} package with an empty
 * argument list.
 *
 * <p>
 * This test is intended to be run as an integration test in Maven. The POM will run the integration tests against the
 * jar, and not the classes directory. Therefore, this test will ensure that all the samples will run correctly when
 * called from the jar with no arguments.
 *
 */
@SuppressFBWarnings(value = { "SIC_INNER_SHOULD_BE_STATIC_ANON", "UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS" },
                    justification = "JMockit coding pattern depends on anonymous classes "
                                    + "and methods with no discernable call site")
@RunWith(Parameterized.class)
public class RunMainWithArgsIntegrationTest {
    Method mainMethod;
    String className;
    String[] argList;
    static final String REQUIRED_DIR = "integration-test-outputs";

    /**
     * Make sure we clear the output directory of previous output files before testing.
     *
     * @throws IOException A file operation failed
     */
    @Before
    public void cleanUp() throws Exception {
        final String workingDir = System.getProperty("user.dir");
        if ((new File(workingDir)).getName().equals(REQUIRED_DIR)) {
            final File[] fileList = new File(workingDir).listFiles();
            if (fileList == null) {
                // No files in directory
                return;
            }
            File file = null;
            for (int i = 0; i < fileList.length; i++) {
                file = fileList[i];
                if (FilenameUtils.getExtension(file.getPath()).equalsIgnoreCase("PDF")
                    || FilenameUtils.getExtension(file.getPath()).equalsIgnoreCase("TXT")) {
                    if (!file.delete()) {
                        throw new IOException("Couldn't delete file " + file.getName());
                    }
                }
            }
        }
    }

    /**
     * Create a test for every class in com.datalogics.pdf.samples that has a <code>main</code> function.
     *
     * @return list of argument lists to construct {@link RunMainWithArgsIntegrationTest} test cases.
     * @throws Exception a general exception was thrown
     */
    @Parameters(name = "mainClass={1}")
    public static Iterable<Object[]> parameters() throws Exception {
        final Set<String> classes = getAllClassNamesInPackage();

        // Create the parameters for every main function in the candidate class
        final ArrayList<Object[]> parameters = new ArrayList<Object[]>();
        for (final String className : classes) {
            final Class<?> klass = Class.forName(className);

            // Get all the public, static methods in the class that are named "main" and take a String array
            @SuppressWarnings("unchecked")
            final Set<Method> mains = getMethods(klass, withModifier(Modifier.PUBLIC), withModifier(Modifier.STATIC),
                                                 withName("main"), withParameters(String[].class));

            // Make a parameter list for them, and add to the tests
            for (final Method main : mains) {
                // Get the resource directory for this class
                String resourceDir = klass.getName();
                resourceDir = resourceDir.substring(0, resourceDir.lastIndexOf('.')).replace('.', File.separatorChar);
                final String workingDir = System.getProperty("user.dir");
                if ((new File(workingDir)).getName().equals(REQUIRED_DIR)) {
                    resourceDir = workingDir + File.separator + "inputs" + File.separator + resourceDir
                                  + File.separator;
                } else {
                    Assert.fail("Testing is being run from the wrong directory.");
                }
                String[] argList;
                if (klass.getSimpleName().equals("HelloWorld")) {
                    argList = new String[] { HelloWorld.OUTPUT_PDF_PATH };
                } else if (klass.getSimpleName().equals("MakePdfFromImage")) {
                    argList = new String[] { MakePdfFromImage.OUTPUT_PDF, resourceDir + MakePdfFromImage.INPUT_BMP,
                        resourceDir + MakePdfFromImage.INPUT_GIF };
                } else if (klass.getSimpleName().equals("MakeWhiteFangBook")) {
                    argList = new String[] { MakeWhiteFangBook.OUTPUT_PDF_PATH };
                } else if (klass.getSimpleName().equals("TextExtract")) {
                    // This sample uses an input file that's in a non-standard location.
                    resourceDir = resourceDir.substring(0, resourceDir.lastIndexOf("com"));
                    argList = new String[] { resourceDir + TextExtract.INPUT_PDF_PATH, TextExtract.OUTPUT_TEXT_PATH };
                } else if (klass.getSimpleName().equals("FillForm")) {
                    argList = new String[] { resourceDir + FillForm.ACROFORM_FDF_INPUT,
                        resourceDir + FillForm.ACROFORM_FDF_DATA,
                        FillForm.ACROFORM_FDF_OUTPUT };
                } else if (klass.getSimpleName().equals("ImageDownsampling")) {
                    argList = new String[] { resourceDir + ImageDownsampling.INPUT_IMAGE_PATH,
                        ImageDownsampling.OUTPUT_IMAGE_PATH };
                } else if (klass.getSimpleName().equals("ConvertPdfDocument")) {
                    argList = new String[] { ConvertPdfDocument.OUTPUT_CONVERTED_PDF_PATH };
                } else if (klass.getSimpleName().equals("FlattenPdf")) {
                    argList = new String[] { resourceDir + FlattenPdf.INPUT_PDF_PATH,
                        FlattenPdf.OUTPUT_FLATTENED_PDF_PATH };
                } else if (klass.getSimpleName().equals("MergeDocuments")) {
                    argList = new String[] { MergeDocuments.OUTPUT_PDF_PATH };
                } else if (klass.getSimpleName().equals("RedactAndSanitizeDocument")) {
                    argList = new String[] { resourceDir + RedactAndSanitizeDocument.INPUT_PDF_PATH,
                        RedactAndSanitizeDocument.OUTPUT_PDF_PATH, RedactAndSanitizeDocument.SEARCH_PDF_STRING };
                } else if (klass.getSimpleName().equals("PrintPdf")) {
                    argList = new String[] { resourceDir + PrintPdf.DEFAULT_INPUT };
                } else if (klass.getSimpleName().equals("SignDocument")) {
                    argList = new String[] { SignDocument.OUTPUT_SIGNED_PDF_PATH };
                } else {
                    // This method isn't setup to be tested yet.
                    continue;
                }
                parameters.add(new Object[] { main, klass.getSimpleName(), argList });
            }
        }

        return parameters;
    }

    /**
     * Get the names of all classes in the package this class is in.
     *
     * @return a set of strings of the class names
     */
    private static Set<String> getAllClassNamesInPackage() {
        final String packageName = RunMainWithArgsIntegrationTest.class.getPackage().getName();
        final Collection<URL> urls = ClasspathHelper.forPackage(packageName);
        final SubTypesScanner scanners = new SubTypesScanner(false);
        final Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(urls)
                                                                                  .setScanners(scanners));
        return reflections.getAllTypes();
    }

    /**
     * Construct a test case for running a main program.
     *
     * @param mainMethod the main method
     * @param className the name of the class, for documentary purposes
     */
    public RunMainWithArgsIntegrationTest(final Method mainMethod, final String className, final String[] argList) {
        this.mainMethod = mainMethod;
        this.className = className;
        this.argList = argList;
    }

    /**
     * Run the main method of a sample class with an empty argument list.
     *
     * @throws Exception a general exception was thrown
     */
    @Test
    public <T extends PrinterJob> void testMainProgram() throws Exception {
        // Set up fake printing, so that anything that tries to print goes to the
        // printer equivalent of /dev/null
        new MockUp<PrintServiceLookup>() {
            @Mock
            PrintService lookupDefaultPrintService() {
                return new FakePrintService();
            }
        };

        // Mock the PrinterJob.getPrinterJob() method to return a TestPrinterJob object
        new MockUp<T>() {
            @Mock
            public PrinterJob getPrinterJob() {
                return new FakePrinterJob();
            }
        };

        // Invoke the main method of that class
        try {
            mainMethod.invoke(null, new Object[] { argList });
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                final Exception causeException = (Exception) cause;
                throw causeException;
            } else {
                throw e;
            }
        }
    }

}

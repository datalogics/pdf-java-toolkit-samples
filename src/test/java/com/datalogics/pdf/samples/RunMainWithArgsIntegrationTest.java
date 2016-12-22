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
import com.datalogics.pdf.samples.manipulation.MergeDocuments;
import com.datalogics.pdf.samples.manipulation.RedactAndSanitizeDocument;
import com.datalogics.pdf.samples.manipulation.RemoveInteractivity;
import com.datalogics.pdf.samples.printing.FakePrintService;
import com.datalogics.pdf.samples.printing.FakePrinterJob;
import com.datalogics.pdf.samples.printing.PrintPdf;
import com.datalogics.pdf.samples.signature.SignDocument;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mockit.Mock;
import mockit.MockUp;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private final String[] argList;
    private final Method mainMethod;
    static final String REQUIRED_DIR = "integration-test-outputs";
    static final String SEP = File.separator;
    static final String OUTPUT_DIR = RunMainWithArgsIntegrationTest.class.getSimpleName() + SEP;

    /**
     * Make sure we clear the output directory of previous output files before testing.
     *
     * @throws IOException an I/O operation failed or was interrupted
     */
    @BeforeClass
    public static void cleanUp() throws Exception {
        final String workingDir = System.getProperty("user.dir");
        if ((new File(workingDir)).getName().equals(REQUIRED_DIR)) {
            // Create output directory if it doesn't exist
            final File outputDir = new File(workingDir + SEP + OUTPUT_DIR);
            if (!outputDir.exists()) {
                if (!outputDir.mkdir()) {
                    throw new IOException("Couldn't create output directory " + outputDir.getName());
                } else {
                    // The directory didn't exist before we created it, so we don't need to clean it out.
                    return;
                }
            }

            // If the directory did exist, clean it out.
            FileUtils.cleanDirectory(outputDir);
        } else {
            throw new Exception("Test is not being run from expected directory.");
        }
    }

    /**
     * Create a test for every class in com.datalogics.pdf.samples that has a <code>main</code> function.
     *
     * @return list of argument lists to construct {@link RunMainWithArgsIntegrationTest} test cases.
     * @throws Exception a general exception was thrown
     */
    @Parameters(name = "mainClass={0}")
    public static Iterable<Object[]> parameters() throws Exception {
        final Set<String> sampleClasses = RunMainMethodsFromJarIntegrationTest.getAllClassNamesInPackage();
        final ArrayList<Object[]> mainArgs = new ArrayList<Object[]>();
        final String resourceDir = System.getProperty("user.dir") + SEP + "inputs" + SEP;

        final Map<String, String[]> argsMap = new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("HelloWorld", new String[] { OUTPUT_DIR + HelloWorld.OUTPUT_PDF_PATH });
                put("MakePdfFromImage", new String[] { OUTPUT_DIR + MakePdfFromImage.OUTPUT_PDF,
                    resourceDir + MakePdfFromImage.INPUT_BMP, resourceDir + MakePdfFromImage.INPUT_GIF });
                put("MakeWhiteFangBook", new String[] { OUTPUT_DIR + MakeWhiteFangBook.OUTPUT_PDF_PATH });
                // The sample input filename here includes a partial path, but we just want the filename itself.
                final String extractInput = TextExtract.INPUT_PDF_PATH
                                .substring(TextExtract.INPUT_PDF_PATH.lastIndexOf("/") + 1);
                put("TextExtract", new String[] { resourceDir + extractInput,
                    OUTPUT_DIR + TextExtract.OUTPUT_TEXT_PATH });
                put("FillForm", new String[] { resourceDir + FillForm.ACROFORM_FDF_INPUT,
                    resourceDir + FillForm.ACROFORM_FDF_DATA, OUTPUT_DIR + FillForm.ACROFORM_FDF_OUTPUT });
                put("ImageDownsampling", new String[] { resourceDir + ImageDownsampling.INPUT_IMAGE_PATH,
                    OUTPUT_DIR + ImageDownsampling.OUTPUT_IMAGE_PATH });
                put("ConvertPdfDocument", new String[] { OUTPUT_DIR + ConvertPdfDocument.OUTPUT_CONVERTED_PDF_PATH });
                put("RemoveInteractivity", new String[] { resourceDir + RemoveInteractivity.INPUT_PDF_PATH,
                    OUTPUT_DIR + RemoveInteractivity.OUTPUT_PDF_PATH });
                put("MergeDocuments", new String[] { OUTPUT_DIR + MergeDocuments.OUTPUT_PDF_PATH });
                put("RedactAndSanitizeDocument", new String[] { resourceDir + RedactAndSanitizeDocument.INPUT_PDF_PATH,
                    OUTPUT_DIR + RedactAndSanitizeDocument.OUTPUT_PDF_PATH,
                    RedactAndSanitizeDocument.SEARCH_PDF_STRING });
                put("PrintPdf", new String[] { resourceDir + PrintPdf.DEFAULT_INPUT });
                put("SignDocument", new String[] { OUTPUT_DIR + SignDocument.OUTPUT_SIGNED_PDF_PATH });
            }
        };

        for (final String className : sampleClasses) {
            final Class<?> c = Class.forName(className);

            @SuppressWarnings("unchecked")
            final Set<Method> mains = getMethods(c, withModifier(Modifier.PUBLIC), withModifier(Modifier.STATIC),
                                                 withName("main"), withParameters(String[].class));
            for (final Method mainMethod : mains) {
                final String simpleName = c.getSimpleName();
                if (argsMap.containsKey(simpleName)) {
                    mainArgs.add(new Object[] { simpleName, mainMethod, argsMap.get(simpleName) });
                }
            }
        }

        return mainArgs;
    }

    /**
     * Construct a test case for running a main program.
     *
     * @param className the name of the class, for documentary purposes
     * @param mainMethod the main method for the class
     * @param argList the arguments to be used to test the class
     * @throws Exception a general exception was thrown
     */
    public RunMainWithArgsIntegrationTest(final String className, final Method mainMethod, final String[] argList)
                    throws Exception {
        this.mainMethod = mainMethod;
        this.argList = argList.clone();
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

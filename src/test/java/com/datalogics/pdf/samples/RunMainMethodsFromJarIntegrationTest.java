/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples;

import static org.reflections.ReflectionUtils.getMethods;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withName;
import static org.reflections.ReflectionUtils.withParameters;

import com.datalogics.pdf.samples.printing.FakePrintService;
import com.datalogics.pdf.samples.printing.FakePrinterJob;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.awt.print.PrinterJob;
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
 * jar, and not the classes directory. Therefore, this test will ensure that all the samples will run correctly
 * when called from the jar with no arguments.
 *
 */
@SuppressFBWarnings(value = { "SIC_INNER_SHOULD_BE_STATIC_ANON", "UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS" },
                    justification = "JMockit coding pattern depends on anonymous classes "
                                    + "and methods with no discernable call site")
@RunWith(Parameterized.class)
public class RunMainMethodsFromJarIntegrationTest {
    final Method mainMethod;

    /**
     * Create a test for every class in com.datalogics.pdf.samples that has a {@code main} function.
     *
     * @return list of argument lists to construct {@link RunMainMethodsFromJarIntegrationTest} test cases.
     * @throws Exception a general exception was thrown
     */
    @Parameters(name = "mainClass={1}")
    public static Iterable<Object[]> parameters() throws Exception {
        final Set<String> classes = getAllClassNamesInPackage();

        // Create the parameters for every main function in the candidate class
        final ArrayList<Object[]> parameters = new ArrayList<>();
        for (final String className : classes) {
            final Class<?> klass = Class.forName(className);

            // Get all the public, static methods in the class that are named "main" and take a String array
            @SuppressWarnings("unchecked")
            final Set<Method> mains = getMethods(klass, withModifier(Modifier.PUBLIC), withModifier(Modifier.STATIC),
                                                 withName("main"), withParameters(String[].class));

            // Make a parameter list for them, and add to the tests
            for (final Method main : mains) {
                parameters.add(new Object[] { main, klass.getSimpleName() });
            }
        }

        return parameters;
    }

    /**
     * Get the names of all classes in the package this class is in.
     *
     * @return a set of strings of the class names
     */
    static Set<String> getAllClassNamesInPackage() {
        final String packageName = RunMainMethodsFromJarIntegrationTest.class.getPackage().getName();
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
    public RunMainMethodsFromJarIntegrationTest(final Method mainMethod, final String className) {
        this.mainMethod = mainMethod;
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
            mainMethod.invoke(null, new Object[] { new String[] {} });
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

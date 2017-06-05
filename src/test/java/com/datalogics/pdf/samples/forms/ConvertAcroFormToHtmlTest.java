/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.datalogics.pdf.samples.SampleTest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * Tests the ConvertAcroFormToHtml sample.
 */
public class ConvertAcroFormToHtmlTest extends SampleTest {
    private static final String HTML_TITLE = "FruitForm_1";
    private static final ArrayList<String> DEFAULT_SAMPLE_OUTPUT_HTML_INPUT_ELEMENTS = new ArrayList<String>() {
        private static final long serialVersionUID = -8806374175991455410L;

        {
            add("accountNumber");
            add("name");
            add("address");
            add("city");
            add("state");
            add("zip");
            add("phone");
            add("email");
            add("quantity");
            add("cost");
            add("deliveryCharge");
            add("total");
        }
    };

    @Test
    public void testConvertAcroFormToHtmlWithDefaultSampleInput() throws Exception {
        final URL inputUrl = ConvertAcroFormToHtml.class.getResource(ConvertAcroFormToHtml.DEFAULT_INPUT);

        final File outputHtmlFile = newOutputFileWithDelete(ConvertAcroFormToHtml.HTML_OUTPUT);
        ConvertAcroFormToHtml.createHtmlForm(inputUrl, outputHtmlFile.toURI().toURL());
        assertTrue(outputHtmlFile.getPath() + " must exist after run", outputHtmlFile.exists());

        final Document htmlDocument = Jsoup.parse(outputHtmlFile, "UTF-8");

        final Elements headTitleElements = htmlDocument.head().getElementsByTag("title");
        assertEquals("Only one title element should be found in the head of the Html",
                     headTitleElements.size(), 1);

        final Element headTitle = headTitleElements.get(0);
        assertEquals("Title of the Html output should be " + HTML_TITLE, HTML_TITLE, headTitle.text());

        final Elements formElements = htmlDocument.body().getElementsByTag("form");
        assertEquals("Only one form element should be found in the body of the Html", formElements.size(), 1);

        final FormElement form = (FormElement) formElements.get(0);
        final Elements formInputElements = form.getElementsByTag("input");
        assertEquals(formInputElements.size(), DEFAULT_SAMPLE_OUTPUT_HTML_INPUT_ELEMENTS.size());

        for (final Element e : formInputElements) {
            assertTrue("Element " + e.attr("name") + " was not found in Html Form",
                       DEFAULT_SAMPLE_OUTPUT_HTML_INPUT_ELEMENTS.contains(e.attr("name")));
        }
    }
}

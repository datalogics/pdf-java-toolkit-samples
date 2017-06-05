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
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Tests the ConvertAcroFormToHtml sample.
 */
public class ConvertAcroFormToHtmlTest extends SampleTest {
    private static final String HTML_TITLE = "FruitForm_1";
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
    }
}

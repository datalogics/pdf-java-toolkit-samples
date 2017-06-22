/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import static org.hamcrest.Matchers.equalTo;

import com.adobe.pdfjt.graphicsDOM.ContentTextItem;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/**
 * Matchers for {@link ContentTextItem}.
 */
public final class ContentTextItemMatchers {

    /**
     * Utility class.
     */
    private ContentTextItemMatchers() {}

    /**
     * Check that the given text item has the desired text.
     *
     * <p>
     * The charCodes are assumed to be convertible to a Java char.
     *
     * @param text the text to match
     * @return matcher
     */
    public static Matcher<ContentTextItem<?, ?>> hasText(final String text) {
        return new FeatureMatcher<ContentTextItem<?, ?>, String>(equalTo(text), "text item contains", "text") {

            @Override
            protected String featureValueOf(final ContentTextItem<?, ?> actual) {
                return actual.getString();
            }
        };
    }
}

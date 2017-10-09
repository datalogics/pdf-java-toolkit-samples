/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;


import static org.apache.logging.log4j.core.Filter.Result.ACCEPT;
import static org.apache.logging.log4j.core.Filter.Result.DENY;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.test.appender.ListAppender;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Collect up {@link LogEvent} objects for testing.
 */
public class LogEventListCollector implements AutoCloseable {
    private static final String CLASS_NAME = MethodHandles.lookup().lookupClass().getSimpleName();

    private ListAppender appender;

    private final LoggerContext loggerContext;

    /**
     * Push a new {@link org.apache.logging.log4j.core.Appender} onto the root logger, and collect up events.
     */
    public LogEventListCollector() {
        // Getting the context this way gets the same context that slf4j uses. The ClassLoader is used to distinguish
        // between different web apps on the same server, but in an effort to simplify things, slf4j always gets a
        // context this way. Thus, getting the default context isn't sufficient; one must get the context via the class
        // loader.
        loggerContext = LoggerContext.getContext(getClass().getClassLoader(), false, null);
        appender = ListAppender.getListAppender(CLASS_NAME);
        if (appender == null) {
            appender = new ListAppender.Builder().setName(CLASS_NAME)
                                                 // If you were to alter the level, it'd happen here.
                                                 .setFilter(ThresholdFilter.createFilter(Level.TRACE, ACCEPT, DENY))
                                                 .build();
        }

        // If we have removed the appender, it gets stopped, so make sure it's started here.
        appender.start();

        // Appenders are persistent, therefore remove any previous events.
        appender.clear();
        loggerContext.getRootLogger().addAppender(appender);
    }

    public List<LogEvent> getEvents() {
        return appender.getEvents();
    }

    @Override
    public void close() throws Exception {
        loggerContext.getRootLogger().removeAppender(appender);
    }
}

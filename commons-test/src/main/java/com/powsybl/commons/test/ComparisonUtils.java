/**
 * Copyright (c) 2022, Stanislao Fidanza
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.test;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Stanislao Fidanza {@literal <stanifidanza98@gmail.com>}
 */
public final class ComparisonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparisonUtils.class);

    private ComparisonUtils() {
    }

    public static void compareXml(InputStream expected, InputStream actual) {
        Diff myDiff = DiffBuilder.compare(expected).withTest(actual).ignoreWhitespace().ignoreComments().build();
        boolean hasDiff = myDiff.hasDifferences();
        if (hasDiff) {
            LOGGER.error("{}", myDiff);
        }
        assertFalse(hasDiff);
    }

    public static void compareTxt(InputStream expected, InputStream actual) {
        try {
            compareTxt(expected, new String(ByteStreams.toByteArray(actual), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void compareBytes(InputStream expected, InputStream actual) {
        try {
            assertArrayEquals(expected.readAllBytes(), actual.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void compareTxt(InputStream expected, InputStream actual, List<Integer> excludedLines) {
        BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expected));
        List<String> expectedLines = expectedReader.lines().toList();
        BufferedReader actualReader = new BufferedReader(new InputStreamReader(actual));
        List<String> actualLines = actualReader.lines().toList();

        for (int i = 0; i < expectedLines.size(); i++) {
            if (!excludedLines.contains(i)) {
                assertEquals(expectedLines.get(i), actualLines.get(i));
            }
        }
    }

    public static void compareTxt(InputStream expected, String actual) {
        try {
            compareTxt(new String(ByteStreams.toByteArray(expected), StandardCharsets.UTF_8), actual);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void compareTxt(String expected, InputStream actual) {
        try {
            compareTxt(expected, new String(ByteStreams.toByteArray(actual), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void compareTxt(String expected, String actual) {
        String expectedStr = TestUtil.normalizeLineSeparator(expected);
        String actualStr = TestUtil.normalizeLineSeparator(actual);
        assertEquals(expectedStr, actualStr);
    }
}

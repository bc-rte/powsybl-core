/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class OutputFileTest {

    @Test
    void test() {
        OutputFile file = new OutputFile("test");
        assertEquals("test", file.getName(1));
        assertFalse(file.dependsOnExecutionNumber());
        assertNull(file.getPostProcessor());
    }

    @Test
    void test2() {
        OutputFile file = new OutputFile("test" + CommandConstants.EXECUTION_NUMBER_PATTERN);
        assertEquals("test1", file.getName(1));
        assertTrue(file.dependsOnExecutionNumber());
        assertNull(file.getPostProcessor());
    }

    @Test
    void test3() {
        OutputFile file = new OutputFile(integer -> "test" + integer, null);
        assertEquals("test1", file.getName(1));
        assertTrue(file.dependsOnExecutionNumber());
        assertNull(file.getPostProcessor());
    }

    @Test
    void test4() {
        OutputFile file = new OutputFile("test.zip", FilePostProcessor.FILE_GZIP);
        assertEquals(FilePostProcessor.FILE_GZIP, file.getPostProcessor());
    }

    @Test
    void test5() {
        List<OutputFile> files = OutputFile.of("test");
        assertEquals(1, files.size());
        assertNotNull(files.get(0));
    }
}

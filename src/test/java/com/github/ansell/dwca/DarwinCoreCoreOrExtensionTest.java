/*
 * Copyright (c) 2016, Peter Ansell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.ansell.dwca;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.ansell.dwca.DarwinCoreCoreOrExtension.CoreOrExtension;

/**
 * Tests for {@link DarwinCoreCoreOrExtension}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreCoreOrExtensionTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private DarwinCoreCoreOrExtension core;
	private DarwinCoreCoreOrExtension extension;

	@Before
	public void setUp() throws Exception {
		core = DarwinCoreCoreOrExtension.newCore();
		extension = DarwinCoreCoreOrExtension.newExtension();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#newCore()}.
	 */
	@Test
	public final void testNewCore() {
		DarwinCoreCoreOrExtension core = DarwinCoreCoreOrExtension.newCore();
		assertEquals(CoreOrExtension.CORE, core.getType());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#newExtension()}.
	 */
	@Test
	public final void testNewExtension() {
		DarwinCoreCoreOrExtension extension = DarwinCoreCoreOrExtension.newExtension();
		assertEquals(CoreOrExtension.EXTENSION, extension.getType());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getIdOrCoreId()}.
	 */
	@Test
	public final void testGetIdOrCoreIdCore() {
		assertNull(core.getIdOrCoreId());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getIdOrCoreId()}.
	 */
	@Test
	public final void testGetIdOrCoreIdExtension() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Extensions must have coreId value set");
		extension.getIdOrCoreId();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setIdOrCoreId(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetIdOrCoreId() {
		core.setIdOrCoreId("test");
		assertEquals("test", core.getIdOrCoreId());
		extension.setIdOrCoreId("test-extension");
		assertEquals("test-extension", extension.getIdOrCoreId());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getRowType()}.
	 */
	@Test
	public final void testGetRowTypeCore() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Did not find value for row type that was required");
		core.getRowType();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getRowType()}.
	 */
	@Test
	public final void testGetRowTypeExtension() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Did not find value for row type that was required");
		extension.getRowType();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setRowType(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetRowType() {
		core.setRowType("test");
		assertEquals("test", core.getRowType());
		extension.setRowType("test-extension");
		assertEquals("test-extension", extension.getRowType());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getFieldsTerminatedBy()}
	 * .
	 */
	@Test
	public final void testGetFieldsTerminatedBy() {
		assertEquals(",", core.getFieldsTerminatedBy());
		assertEquals(",", extension.getFieldsTerminatedBy());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setFieldsTerminatedBy(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetFieldsTerminatedBy() {
		core.setFieldsTerminatedBy("\\t");
		assertEquals("\t", core.getFieldsTerminatedBy());
		extension.setFieldsTerminatedBy("\\b");
		assertEquals("\b", extension.getFieldsTerminatedBy());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getLinesTerminatedBy()}
	 * .
	 */
	@Test
	public final void testGetLinesTerminatedBy() {
		assertEquals("\n", core.getLinesTerminatedBy());
		assertEquals("\n", extension.getLinesTerminatedBy());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setLinesTerminatedBy(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetLinesTerminatedBy() {
		core.setLinesTerminatedBy("\\r\\n");
		assertEquals("\r\n", core.getLinesTerminatedBy());
		extension.setLinesTerminatedBy("\\r\\n");
		assertEquals("\r\n", extension.getLinesTerminatedBy());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getFieldsEnclosedBy()}
	 * .
	 */
	@Test
	public final void testGetFieldsEnclosedBy() {
		assertEquals("\"", core.getFieldsEnclosedBy());
		assertEquals("\"", extension.getFieldsEnclosedBy());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setFieldsEnclosedBy(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetFieldsEnclosedBy() {
		core.setFieldsEnclosedBy("'");
		assertEquals("'", core.getFieldsEnclosedBy());
		extension.setFieldsEnclosedBy("'");
		assertEquals("'", extension.getFieldsEnclosedBy());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getEncoding()}.
	 */
	@Test
	public final void testGetEncoding() {
		assertEquals(StandardCharsets.UTF_8, core.getEncoding());
		assertEquals(StandardCharsets.UTF_8, extension.getEncoding());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setEncoding(java.nio.charset.Charset)}
	 * .
	 */
	@Test
	public final void testSetEncoding() {
		core.setEncoding(StandardCharsets.ISO_8859_1);
		assertEquals(StandardCharsets.ISO_8859_1, core.getEncoding());
		extension.setEncoding(StandardCharsets.UTF_16);
		assertEquals(StandardCharsets.UTF_16, extension.getEncoding());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getIgnoreHeaderLines()}
	 * .
	 */
	@Test
	public final void testGetIgnoreHeaderLines() {
		assertEquals(0, core.getIgnoreHeaderLines());
		assertEquals(0, extension.getIgnoreHeaderLines());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setIgnoreHeaderLines(int)}
	 * .
	 */
	@Test
	public final void testSetIgnoreHeaderLines() {
		core.setIgnoreHeaderLines(1);
		assertEquals(1, core.getIgnoreHeaderLines());
		extension.setIgnoreHeaderLines(15);
		assertEquals(15, extension.getIgnoreHeaderLines());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getDateFormat()}.
	 */
	@Test
	public final void testGetDateFormat() {
		assertEquals(DateTimeFormatter.ISO_LOCAL_DATE, core.getDateFormat());
		assertEquals(DateTimeFormatter.ISO_LOCAL_DATE, extension.getDateFormat());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setDateFormat(java.time.format.DateTimeFormatter)}
	 * .
	 */
	@Test
	public final void testSetDateFormat() {
		core.setDateFormat(DateTimeFormatter.RFC_1123_DATE_TIME);
		assertEquals(DateTimeFormatter.RFC_1123_DATE_TIME, core.getDateFormat());
		extension.setDateFormat(DateTimeFormatter.RFC_1123_DATE_TIME);
		assertEquals(DateTimeFormatter.RFC_1123_DATE_TIME, extension.getDateFormat());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getType()}.
	 */
	@Test
	public final void testGetType() {
		assertEquals(CoreOrExtension.CORE, core.getType());
		assertEquals(CoreOrExtension.EXTENSION, extension.getType());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getFiles()}.
	 */
	@Test
	public final void testGetFilesCore() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Did not find value for files that was required");
		core.getFiles();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getFiles()}.
	 */
	@Test
	public final void testGetFilesExtension() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Did not find value for files that was required");
		extension.getFiles();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#setFiles(com.github.ansell.dwca.DarwinCoreFile)}
	 * .
	 */
	@Test
	public final void testSetFiles() {
		core.setFiles(new DarwinCoreFile());
		extension.setFiles(new DarwinCoreFile());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getFields()}.
	 */
	@Test
	public final void testGetFieldsCore() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("No fields present");
		core.getFields();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#getFields()}.
	 */
	@Test
	public final void testGetFieldsExtension() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("No fields present");
		extension.getFields();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreCoreOrExtension#addField(com.github.ansell.dwca.DarwinCoreField)}
	 * .
	 */
	@Test
	public final void testAddField() {
		core.addField(new DarwinCoreField());
		extension.addField(new DarwinCoreField());
	}

}

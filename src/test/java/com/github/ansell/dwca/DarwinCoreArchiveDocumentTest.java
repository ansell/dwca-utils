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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link DarwinCoreArchiveDocument}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveDocumentTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private DarwinCoreArchiveDocument testDocument;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testDocument = new DarwinCoreArchiveDocument();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#getCore()}.
	 */
	@Test
	public final void testGetCore() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Could not find core in this document");
		testDocument.getCore();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#setCore(com.github.ansell.dwca.DarwinCoreCoreOrExtension)}
	 * .
	 */
	@Test
	public final void testSetCore() {
		testDocument.setCore(DarwinCoreCoreOrExtension.newCore());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#setCore(com.github.ansell.dwca.DarwinCoreCoreOrExtension)}
	 * .
	 */
	@Test
	public final void testSetCoreMultiple() {
		testDocument.setCore(DarwinCoreCoreOrExtension.newCore());
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Multiple core elements found for darwin core archive document");
		testDocument.setCore(DarwinCoreCoreOrExtension.newCore());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#setCore(com.github.ansell.dwca.DarwinCoreCoreOrExtension)}
	 * .
	 */
	@Test
	public final void testSetCoreWithExtension() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("The core must be typed as core");
		testDocument.setCore(DarwinCoreCoreOrExtension.newExtension());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#getExtensions()}.
	 */
	@Test
	public final void testGetExtensions() {
		assertTrue(testDocument.getExtensions().isEmpty());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#addExtension(com.github.ansell.dwca.DarwinCoreCoreOrExtension)}
	 * .
	 */
	@Test
	public final void testAddExtension() {
		assertTrue(testDocument.getExtensions().isEmpty());
		testDocument.addExtension(DarwinCoreCoreOrExtension.newExtension());
		assertEquals(1, testDocument.getExtensions().size());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#addExtension(com.github.ansell.dwca.DarwinCoreCoreOrExtension)}
	 * .
	 */
	@Test
	public final void testAddExtensionWithCore() {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("All extensions must be typed as extension");
		testDocument.addExtension(DarwinCoreCoreOrExtension.newCore());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsFieldsEmpty() {
		testDocument.setCore(DarwinCoreCoreOrExtension.newCore());
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("Core must have fields"), containsString("No fields present")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsFilesEmpty() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.addField(new DarwinCoreField());
		testDocument.setCore(testCore);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("Core must have files set"),
				containsString("Did not find value for files that was required")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsFieldTermNotSet() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.addField(new DarwinCoreField());
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("All fields must have term set"),
				containsString("Term was required for field, but was not set")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsCoreFieldIndexAndDefaultNull() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Fields that do not have indexes must have default values set:");
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsExtensionsWithoutCoreHavingID() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testField.setDefault("test default");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		DarwinCoreCoreOrExtension testExtension = DarwinCoreCoreOrExtension.newExtension();
		testDocument.addExtension(testExtension);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Core must have id set if there are extensions present");
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsExtensionIDNull() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.setIdOrCoreId("test core id");
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testField.setDefault("test default");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		DarwinCoreCoreOrExtension testExtension = DarwinCoreCoreOrExtension.newExtension();
		testDocument.addExtension(testExtension);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("Extensions must have coreId set"),
				containsString("Extensions must have coreId value set")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsExtensionFieldsEmpty() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.setIdOrCoreId("test core id");
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testField.setDefault("test default");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		DarwinCoreCoreOrExtension testExtension = DarwinCoreCoreOrExtension.newExtension();
		testExtension.setIdOrCoreId("test extension id");
		testDocument.addExtension(testExtension);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("Extension must have fields"), containsString("No fields present")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsFieldsExtensionFilesMissing() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.setIdOrCoreId("test core id");
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testField.setDefault("test default");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		DarwinCoreCoreOrExtension testExtension = DarwinCoreCoreOrExtension.newExtension();
		testExtension.setIdOrCoreId("test extension id");
		DarwinCoreField testFieldExtension = new DarwinCoreField();
		testExtension.addField(testFieldExtension);
		testDocument.addExtension(testExtension);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("Extension must have files set"),
				containsString("Did not find value for files that was required")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsExtensionFieldTermMissing() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.setIdOrCoreId("test core id");
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testField.setDefault("test default");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		DarwinCoreCoreOrExtension testExtension = DarwinCoreCoreOrExtension.newExtension();
		testExtension.setIdOrCoreId("test extension id");
		DarwinCoreField testFieldExtension = new DarwinCoreField();
		testExtension.addField(testFieldExtension);
		testExtension.setFiles(new DarwinCoreFile());
		testDocument.addExtension(testExtension);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage(anyOf(containsString("All extension fields must have term set"),
				containsString("Term was required for field, but was not set")));
		testDocument.checkConstraints();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveDocument#checkConstraints()}
	 * .
	 */
	@Test
	public final void testCheckConstraintsExtensionFieldIndexAndDefaultNull() {
		DarwinCoreCoreOrExtension testCore = DarwinCoreCoreOrExtension.newCore();
		testCore.setIdOrCoreId("test core id");
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		testField.setDefault("test default");
		testCore.addField(testField);
		testCore.setFiles(new DarwinCoreFile());
		testDocument.setCore(testCore);
		DarwinCoreCoreOrExtension testExtension = DarwinCoreCoreOrExtension.newExtension();
		testExtension.setIdOrCoreId("test extension id");
		DarwinCoreField testFieldExtension = new DarwinCoreField();
		testFieldExtension.setTerm("test extension");
		testExtension.addField(testFieldExtension);
		testExtension.setFiles(new DarwinCoreFile());
		testDocument.addExtension(testExtension);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Fields that do not have indexes must have default values set:");
		testDocument.checkConstraints();
	}

}

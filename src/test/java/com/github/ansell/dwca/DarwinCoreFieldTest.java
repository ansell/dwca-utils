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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link DarwinCoreField}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreFieldTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Test method for {@link com.github.ansell.dwca.DarwinCoreField#getIndex()}
	 * .
	 */
	@Test
	public final void testGetIndex() {
		DarwinCoreField testField = new DarwinCoreField();
		assertNull(testField.getIndex());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setIndex(java.lang.Integer)}
	 * .
	 */
	@Test
	public final void testSetIndex() {
		DarwinCoreField testField = new DarwinCoreField();
		assertNull(testField.getIndex());
		testField.setIndex(2);
		assertEquals(2, testField.getIndex().intValue());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setIndex(java.lang.Integer)}
	 * .
	 */
	@Test
	public final void testSetIndexMultiple() {
		DarwinCoreField testField = new DarwinCoreField();
		assertNull(testField.getIndex());
		testField.setIndex(2);
		assertEquals(2, testField.getIndex().intValue());
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Cannot specify multiple indexes for a field");
		testField.setIndex(3);
	}

	/**
	 * Test method for {@link com.github.ansell.dwca.DarwinCoreField#getTerm()}.
	 */
	@Test
	public final void testGetTermError() {
		DarwinCoreField testField = new DarwinCoreField();
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Term was required for field, but was not set");
		testField.getTerm();
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setTerm(java.lang.String)}.
	 */
	@Test
	public final void testSetTerm() {
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		assertEquals("test", testField.getTerm());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setTerm(java.lang.String)}.
	 */
	@Test
	public final void testSetTermMultiple() {
		DarwinCoreField testField = new DarwinCoreField();
		testField.setTerm("test");
		assertEquals("test", testField.getTerm());
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Cannot specify multiple terms for a field");
		testField.setTerm("another");
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#getDefault()}.
	 */
	@Test
	public final void testGetDefault() {
		DarwinCoreField testField = new DarwinCoreField();
		assertNull(testField.getDefault());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setDefault(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetDefault() {
		DarwinCoreField testField = new DarwinCoreField();
		testField.setDefault("test");
		assertEquals("test", testField.getDefault());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setDefault(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetDefaultMultiple() {
		DarwinCoreField testField = new DarwinCoreField();
		testField.setDefault("test");
		assertEquals("test", testField.getDefault());
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Cannot specify multiple default values for a field");
		testField.setDefault("another");
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#getVocabulary()}.
	 */
	@Test
	public final void testGetVocabulary() {
		DarwinCoreField testField = new DarwinCoreField();
		assertNull(testField.getVocabulary());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setVocabulary(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetVocabulary() {
		DarwinCoreField testField = new DarwinCoreField();
		testField.setVocabulary("test");
		assertEquals("test", testField.getVocabulary());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreField#setVocabulary(java.lang.String)}
	 * .
	 */
	@Test
	public final void testSetVocabularyMultiple() {
		DarwinCoreField testField = new DarwinCoreField();
		testField.setVocabulary("test");
		assertEquals("test", testField.getVocabulary());
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Cannot specify multiple vocabularies for a field");
		testField.setVocabulary("another");
	}

}

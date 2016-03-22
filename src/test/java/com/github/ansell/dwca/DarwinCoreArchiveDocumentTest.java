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
	@Ignore("TODO: Implement me!")
	@Test
	public final void testCheckConstraints() {
		fail("Not yet implemented"); // TODO
	}

}

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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Tests for {@link DarwinCoreMetadataSaxParser}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreMetadataSaxParserTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataSaxParser#parse(java.io.Reader)}
	 * .
	 */
	@Test
	public final void testParseReader() throws Exception {
		try (Reader inputStreamReader = new InputStreamReader(
				this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"));) {
			DarwinCoreArchiveDocument parse = DarwinCoreMetadataSaxParser.parse(inputStreamReader);
			assertNotNull(parse);
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataSaxParser#parse(java.io.Reader, org.xml.sax.XMLReader)}
	 * .
	 */
	@Test
	public final void testParseReaderXMLReader() throws Exception {
		try (Reader inputStreamReader = new InputStreamReader(
				this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"));) {
			DarwinCoreArchiveDocument parse = DarwinCoreMetadataSaxParser.parse(inputStreamReader,
					XMLReaderFactory.createXMLReader());
			assertNotNull(parse);
		}
	}

}

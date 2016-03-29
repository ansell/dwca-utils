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

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link DarwinCoreMetadataGenerator}
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreMetadataGeneratorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private Path testFile;

	private Path testTempDir;

	private Path testMetadataXml;

	private Path testExtension;

	private Path testExtension2;

	@Before
	public void setUp() throws Exception {
		testTempDir = tempDir.newFolder("dwca-generator-temp").toPath();
		testFile = testTempDir.resolve("types.csv");
		testExtension = testTempDir.resolve("distribution.csv");
		testExtension2 = testTempDir.resolve("specimens.csv");
		testMetadataXml = testTempDir.resolve("metadata.xml");
		Files.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/types.csv"), testFile);
		Files.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/distribution.csv"), testExtension);
		Files.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"), testExtension2);
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataGenerator#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainHelp() throws Exception {
		DarwinCoreMetadataGenerator.main("--help");
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataGenerator#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainSingle() throws Exception {
		DarwinCoreMetadataGenerator.main("--input", testFile.toAbsolutePath().toString(), "--output",
				testMetadataXml.toAbsolutePath().toString());
		Files.readAllLines(testMetadataXml, StandardCharsets.UTF_8).forEach(System.out::println);
		try (Reader input = Files.newBufferedReader(testMetadataXml);) {
			DarwinCoreArchiveDocument archiveDocument = DarwinCoreMetadataSaxParser.parse(input);
			assertNotNull(archiveDocument);
			assertEquals(0, archiveDocument.getExtensions().size());
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataGenerator#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainExtension() throws Exception {
		DarwinCoreMetadataGenerator.main("--input", testFile.toAbsolutePath().toString(), "--output",
				testMetadataXml.toAbsolutePath().toString(), "--extension", testExtension.toAbsolutePath().toString());
		Files.readAllLines(testMetadataXml, StandardCharsets.UTF_8).forEach(System.out::println);
		try (Reader input = Files.newBufferedReader(testMetadataXml);) {
			DarwinCoreArchiveDocument archiveDocument = DarwinCoreMetadataSaxParser.parse(input);
			assertNotNull(archiveDocument);
			assertEquals(1, archiveDocument.getExtensions().size());
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataGenerator#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainMultipleExtensions() throws Exception {
		DarwinCoreMetadataGenerator.main("--input", testFile.toAbsolutePath().toString(), "--output",
				testMetadataXml.toAbsolutePath().toString(), "--extension", testExtension.toAbsolutePath().toString(),
				"--extension", testExtension2.toAbsolutePath().toString());
		Files.readAllLines(testMetadataXml, StandardCharsets.UTF_8).forEach(System.out::println);
		try (Reader input = Files.newBufferedReader(testMetadataXml);) {
			DarwinCoreArchiveDocument archiveDocument = DarwinCoreMetadataSaxParser.parse(input);
			assertNotNull(archiveDocument);
			assertEquals(2, archiveDocument.getExtensions().size());
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreMetadataGenerator#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainMultipleExtensionsShowDefaults() throws Exception {
		DarwinCoreMetadataGenerator.main("--input", testFile.toAbsolutePath().toString(), "--output",
				testMetadataXml.toAbsolutePath().toString(), "--extension", testExtension.toAbsolutePath().toString(),
				"--extension", testExtension2.toAbsolutePath().toString(), "--show-defaults", "true");
		Files.readAllLines(testMetadataXml, StandardCharsets.UTF_8).forEach(System.out::println);
		try (Reader input = Files.newBufferedReader(testMetadataXml);) {
			DarwinCoreArchiveDocument archiveDocument = DarwinCoreMetadataSaxParser.parse(input);
			assertNotNull(archiveDocument);
			assertEquals(2, archiveDocument.getExtensions().size());
		}
	}

}

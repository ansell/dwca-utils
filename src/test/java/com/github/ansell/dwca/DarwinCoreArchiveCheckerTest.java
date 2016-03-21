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

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link DarwinCoreArchiveChecker}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveCheckerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	private Path testFile;

	private Path testTempDir;

	private Path testMetadataXml;

	@Before
	public void setUp() throws Exception {
		testTempDir = tempDir.newFolder("dwca-check-temp").toPath();
		testFile = tempDir.newFolder("dwca-check-input").toPath().resolve("dwca-test.zip");
		try (OutputStream out = Files.newOutputStream(testFile, StandardOpenOption.CREATE);
				ZipOutputStream zipOut = new ZipOutputStream(out, StandardCharsets.UTF_8);) {
			ZipEntry metadataXml = new ZipEntry(DarwinCoreArchiveChecker.METADATA_XML);
			zipOut.putNextEntry(metadataXml);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"), zipOut);
			zipOut.closeEntry();
			ZipEntry specimensCsv = new ZipEntry("specimens.csv");
			zipOut.putNextEntry(specimensCsv);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"), zipOut);
			zipOut.flush();
			zipOut.closeEntry();
			zipOut.flush();
			out.flush();
		}
		testMetadataXml = tempDir.newFolder("dwca-check-unittest").toPath()
				.resolve(DarwinCoreArchiveChecker.METADATA_XML);
		try (Writer out = Files.newBufferedWriter(testMetadataXml)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"), out);
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainHelp() throws Exception {
		DarwinCoreArchiveChecker.main("--help");
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMain() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testFile.toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#checkZip(java.nio.file.Path, java.nio.file.Path)}
	 * .
	 */
	@Test
	public final void testCheckZip() throws Exception {
		Path metadataPath = DarwinCoreArchiveChecker.checkZip(testFile, testTempDir);
		assertNotNull(metadataPath);
		assertTrue(Files.exists(metadataPath));
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#parseMetadataXml(java.nio.file.Path)}
	 * .
	 */
	@Ignore("TODO: Implement parseMetadataXml")
	@Test
	public final void testParseMetadataXml() throws Exception {
		DarwinCoreArchiveChecker.parseMetadataXml(testMetadataXml);
	}

}

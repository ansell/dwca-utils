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
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
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

	private Path testFile2;

	private Path testFileNoMetadata;

	private Path testTempDir;

	private Path testMetadataXml;

	private Path testMetadataXmlWithExtension;

	private Path testMetadataXmlFolder;

	private Path testMetadataXmlSpecimensCsv;

	private Path testMetadataXmlWithExtensionFolder;

	private Path testMetadataXmlWhalesTxt;

	private Path testMetadataXmlTypesCsv;

	private Path testMetadataXmlDistributionCsv;

	private Path testMetadataXmlTsvFolder;

	private Path testMetadataXmlTsv;

	private Path testMetadataXmlSpecimensTsv;

	private Path testMetadataXmlWithDefaults;

	private Path testMetadataXmlWithDefaultsFolder;

	private Path testMetadataXmlWithDefaultsSpecimensCsv;

	@Before
	public void setUp() throws Exception {
		testTempDir = tempDir.newFolder("dwca-check-temp").toPath();
		testFile = tempDir.newFolder("dwca-check-input1").toPath().resolve("dwca-test.zip");
		try (OutputStream out = Files.newOutputStream(testFile, StandardOpenOption.CREATE);
				ZipOutputStream zipOut = new ZipOutputStream(out, StandardCharsets.UTF_8);) {
			ZipEntry metadataXml = new ZipEntry(DarwinCoreArchiveChecker.METADATA_XML);
			zipOut.putNextEntry(metadataXml);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"), zipOut);
			zipOut.closeEntry();
			ZipEntry specimensCsv = new ZipEntry("specimens.csv");
			zipOut.putNextEntry(specimensCsv);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"), zipOut);
			zipOut.flush();
			zipOut.closeEntry();
			zipOut.flush();
			out.flush();
		}
		testFile2 = tempDir.newFolder("dwca-check-input2").toPath().resolve("dwca-test2.zip");
		try (OutputStream out = Files.newOutputStream(testFile2, StandardOpenOption.CREATE);
				ZipOutputStream zipOut = new ZipOutputStream(out, StandardCharsets.UTF_8);) {
			ZipEntry metaXml = new ZipEntry(DarwinCoreArchiveChecker.META_XML);
			zipOut.putNextEntry(metaXml);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"), zipOut);
			zipOut.closeEntry();
			ZipEntry specimensCsv = new ZipEntry("specimens.csv");
			zipOut.putNextEntry(specimensCsv);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"), zipOut);
			zipOut.flush();
			zipOut.closeEntry();
			zipOut.flush();
			out.flush();
		}
		testFileNoMetadata = tempDir.newFolder("dwca-check-input3").toPath().resolve("dwca-test-no-metadata.zip");
		try (OutputStream out = Files.newOutputStream(testFileNoMetadata, StandardOpenOption.CREATE);
				ZipOutputStream zipOut = new ZipOutputStream(out, StandardCharsets.UTF_8);) {
			ZipEntry specimensCsv = new ZipEntry("specimens.csv");
			zipOut.putNextEntry(specimensCsv);
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"), zipOut);
			zipOut.flush();
			zipOut.closeEntry();
			zipOut.flush();
			out.flush();
		}
		testMetadataXmlFolder = tempDir.newFolder("dwca-check-unittest").toPath();
		testMetadataXml = testMetadataXmlFolder.resolve(DarwinCoreArchiveChecker.METADATA_XML);
		testMetadataXmlSpecimensCsv = testMetadataXmlFolder.resolve("specimens.csv");
		try (Writer out = Files.newBufferedWriter(testMetadataXml)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"), out);
		}
		try (Writer out = Files.newBufferedWriter(testMetadataXmlSpecimensCsv)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"), out);
		}
		testMetadataXmlWithExtensionFolder = tempDir.newFolder("dwca-check-unittest-with-extensions").toPath();
		testMetadataXmlWithExtension = testMetadataXmlWithExtensionFolder
				.resolve(DarwinCoreArchiveChecker.METADATA_XML);
		testMetadataXmlWhalesTxt = testMetadataXmlWithExtensionFolder.resolve("whales.txt");
		testMetadataXmlTypesCsv = testMetadataXmlWithExtensionFolder.resolve("types.csv");
		testMetadataXmlDistributionCsv = testMetadataXmlWithExtensionFolder.resolve("distribution.csv");
		try (Writer out = Files.newBufferedWriter(testMetadataXmlWithExtension)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/extensionMetadata.xml"), out);
		}
		try (Writer out = Files.newBufferedWriter(testMetadataXmlWhalesTxt)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/whales.txt"), out);
		}
		try (Writer out = Files.newBufferedWriter(testMetadataXmlTypesCsv)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/types.csv"), out);
		}
		try (Writer out = Files.newBufferedWriter(testMetadataXmlDistributionCsv)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/distribution.csv"), out);
		}
		testMetadataXmlTsvFolder = tempDir.newFolder("dwca-check-unittest-tsv").toPath();
		Files.createDirectories(testMetadataXmlTsvFolder.resolve("subdir"));
		testMetadataXmlTsv = testMetadataXmlTsvFolder.resolve(DarwinCoreArchiveChecker.METADATA_XML);
		testMetadataXmlSpecimensTsv = testMetadataXmlTsvFolder.resolve("subdir").resolve("specimens.tsv");
		try (Writer out = Files.newBufferedWriter(testMetadataXmlTsv)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/tsvmetadata.xml"), out);
		}
		try (Writer out = Files.newBufferedWriter(testMetadataXmlSpecimensTsv)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/subdir/specimens.tsv"), out);
		}
		testMetadataXmlWithDefaultsFolder = tempDir.newFolder("dwca-check-unittest-with-defaults").toPath();
		testMetadataXmlWithDefaults = testMetadataXmlWithDefaultsFolder.resolve(DarwinCoreArchiveChecker.METADATA_XML);
		testMetadataXmlWithDefaultsSpecimensCsv = testMetadataXmlWithDefaultsFolder
				.resolve("specimens-with-missing-counts.csv");
		try (Writer out = Files.newBufferedWriter(testMetadataXmlWithDefaults)) {
			IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata-with-defaults.xml"),
					out);
		}
		try (Writer out = Files.newBufferedWriter(testMetadataXmlWithDefaultsSpecimensCsv)) {
			IOUtils.copy(
					this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens-with-missing-counts.csv"),
					out);
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
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainDebug() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testFile.toAbsolutePath().toString(), "--debug", "true");
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainAlternate() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testFile2.toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainNoMetadata() throws Exception {
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("Did not find a metadata file in ZIP file");
		DarwinCoreArchiveChecker.main("--input", testFileNoMetadata.toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainBasicMetadata() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testMetadataXml.toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainBasicMetadataWithExtension() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlWithExtension.toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainBasicMetadataWithOutput() throws Exception {
		Path testOutput = Files.createTempDirectory(testTempDir, "check-output");
		DarwinCoreArchiveChecker.main("--input", testMetadataXml.toAbsolutePath().toString(), "--output",
				testOutput.toAbsolutePath().toString());
		assertTrue(Files.exists(testOutput.resolve("Statistics-specimens.csv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-specimens.csv")));
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainBasicMetadataWithExtensionWithOutput() throws Exception {
		Path testOutput = Files.createTempDirectory(testTempDir, "check-output");
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlWithExtension.toAbsolutePath().toString(), "--output",
				testOutput.toAbsolutePath().toString());
		assertTrue(Files.exists(testOutput.resolve("Statistics-distribution.csv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-distribution.csv")));
		assertTrue(Files.exists(testOutput.resolve("Statistics-types.csv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-types.csv")));
		assertTrue(Files.exists(testOutput.resolve("Statistics-whales.txt")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-whales.txt")));
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainTsvMetadataWithOutput() throws Exception {
		Path testOutput = Files.createTempDirectory(testTempDir, "check-output");
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlTsv.toAbsolutePath().toString(), "--output",
				testOutput.toAbsolutePath().toString());
		assertTrue(Files.exists(testOutput.resolve("Statistics-specimens.tsv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-specimens.tsv")));
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainOutputWithTempDir() throws Exception {
		Path testOutput = Files.createTempDirectory(testTempDir, "check-output");
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlTsv.toAbsolutePath().toString(), "--output",
				testOutput.toAbsolutePath().toString(), "--temp-dir", testTempDir.toAbsolutePath().toString());
		assertTrue(Files.exists(testOutput.resolve("Statistics-specimens.tsv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-specimens.tsv")));
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainNoOutputWithTempDir() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlTsv.toAbsolutePath().toString(), "--temp-dir",
				testTempDir.toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainNoOutputWithTempDirAsParent() throws Exception {
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlTsv.toAbsolutePath().toString(), "--temp-dir",
				testMetadataXmlTsv.getParent().toAbsolutePath().toString());
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainOutputWithTempDirAsInputParent() throws Exception {
		Path testOutput = Files.createTempDirectory(testTempDir, "check-output");
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlTsv.toAbsolutePath().toString(), "--output",
				testOutput.toAbsolutePath().toString(), "--temp-dir",
				testMetadataXmlTsv.getParent().toAbsolutePath().toString());
		assertTrue(Files.exists(testOutput.resolve("Statistics-specimens.tsv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-specimens.tsv")));
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#main(java.lang.String[])}
	 * .
	 */
	@Test
	public final void testMainOutputWithTempDirAsParent() throws Exception {
		Path testOutput = Files.createTempDirectory(testTempDir, "check-output");
		DarwinCoreArchiveChecker.main("--input", testMetadataXmlTsv.toAbsolutePath().toString(), "--output",
				testOutput.toAbsolutePath().toString(), "--temp-dir",
				testOutput.getParent().toAbsolutePath().toString());
		// Verify that the tempDir was created as a subdirectory and did not
		// wipe out the outputs
		assertTrue(Files.exists(testOutput.resolve("Statistics-specimens.tsv")));
		assertTrue(Files.exists(testOutput.resolve("Mapping-specimens.tsv")));
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
	@Test
	public final void testParseMetadataXmlCoreOnly() throws Exception {
		DarwinCoreArchiveDocument testDocument = DarwinCoreArchiveChecker.parseMetadataXml(testMetadataXml);
		assertNotNull(testDocument);
		assertNotNull(testDocument.getCore());
		assertEquals("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord",
				testDocument.getCore().getRowType());
		assertEquals(1, testDocument.getCore().getIgnoreHeaderLines());
		assertEquals(0, testDocument.getExtensions().size());
		assertNotNull(testDocument.getCore().getFiles());
		assertEquals(1, testDocument.getCore().getFiles().getLocations().size());
		assertEquals("./specimens.csv", testDocument.getCore().getFiles().getLocations().get(0));
		assertEquals(4, testDocument.getCore().getFields().size());
		for (DarwinCoreField field : testDocument.getCore().getFields()) {
			assertTrue(field.getTerm().trim().length() > 0);
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#parseMetadataXml(java.nio.file.Path)}
	 * .
	 */
	@Test
	public final void testCoreIterationNoDefaults() throws Exception {
		DarwinCoreArchiveDocument testDocument = DarwinCoreArchiveChecker.parseMetadataXml(testMetadataXmlWithDefaults);
		assertNotNull(testDocument);
		assertNotNull(testDocument.getCore());
		assertEquals("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord",
				testDocument.getCore().getRowType());
		assertEquals(1, testDocument.getCore().getIgnoreHeaderLines());
		assertEquals(0, testDocument.getExtensions().size());
		assertNotNull(testDocument.getCore().getFiles());
		assertEquals(1, testDocument.getCore().getFiles().getLocations().size());
		assertEquals("./specimens-with-missing-counts.csv", testDocument.getCore().getFiles().getLocations().get(0));
		assertEquals(3, testDocument.getCore().getFields().size());
		for (DarwinCoreField field : testDocument.getCore().getFields()) {
			assertTrue(field.getTerm().trim().length() > 0);
		}

		try (CloseableIterator<List<DarwinCoreRecord>> iterator = testDocument.iterator(false);) {
			assertTrue(iterator.hasNext());
			while (iterator.hasNext()) {
				List<DarwinCoreRecord> next = iterator.next();
				assertNotNull(next);
				assertEquals(3, next.get(0).getCoreOrExtension().getFields().size());
				// Verify that the default values are coming through only when asked for
				assertEquals(Optional.of("1"), next.get(0).valueFor("http://rs.tdwg.org/dwc/terms/individualCount", true));
				assertEquals(Optional.of(""), next.get(0).valueFor("http://rs.tdwg.org/dwc/terms/individualCount", false));
			}
		}
	}

	/**
	 * Test method for
	 * {@link com.github.ansell.dwca.DarwinCoreArchiveChecker#parseMetadataXml(java.nio.file.Path)}
	 * .
	 */
	@Test
	public final void testParseMetadataXmlWithExtensions() throws Exception {
		DarwinCoreArchiveDocument testDocument = DarwinCoreArchiveChecker
				.parseMetadataXml(testMetadataXmlWithExtension);
		assertNotNull(testDocument);
		assertNotNull(testDocument.getCore());
		assertEquals("http://rs.tdwg.org/dwc/terms/Taxon", testDocument.getCore().getRowType());
		assertEquals(1, testDocument.getCore().getIgnoreHeaderLines());
		assertEquals(StandardCharsets.UTF_8, testDocument.getCore().getEncoding());
		assertEquals("\n", testDocument.getCore().getLinesTerminatedBy());
		assertEquals("\t", testDocument.getCore().getFieldsTerminatedBy());
		assertEquals(2, testDocument.getExtensions().size());
		assertNotNull(testDocument.getCore().getFiles());
		assertEquals(1, testDocument.getCore().getFiles().getLocations().size());
		assertEquals("whales.txt", testDocument.getCore().getFiles().getLocations().get(0));
		assertEquals(6, testDocument.getCore().getFields().size());
		for (DarwinCoreField field : testDocument.getCore().getFields()) {
			assertTrue(field.getTerm().trim().length() > 0);
		}

	}

	@Test
	public final void testIteratorCoreOnly() throws Exception {
		DarwinCoreArchiveDocument testDocument = DarwinCoreArchiveChecker.parseMetadataXml(testMetadataXml);
		assertNotNull(testDocument);
		assertNotNull(testDocument.getCore());
		assertEquals("http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord",
				testDocument.getCore().getRowType());
		assertEquals(1, testDocument.getCore().getIgnoreHeaderLines());
		assertEquals(0, testDocument.getExtensions().size());
		assertNotNull(testDocument.getCore().getFiles());
		assertEquals(1, testDocument.getCore().getFiles().getLocations().size());
		assertEquals("./specimens.csv", testDocument.getCore().getFiles().getLocations().get(0));
		assertEquals(4, testDocument.getCore().getFields().size());
		for (DarwinCoreField field : testDocument.getCore().getFields()) {
			assertTrue(field.getTerm().trim().length() > 0);
		}

		for (int replicaIterations = 1; replicaIterations < 100; replicaIterations++) {
			System.out.println("Replica #" + replicaIterations);
			int recordCount = 0;
			try (CloseableIterator<List<DarwinCoreRecord>> iterator = testDocument.iterator()) {
				while (iterator.hasNext()) {
					List<DarwinCoreRecord> nextRecord = iterator.next();
					recordCount++;
					System.out.println(nextRecord.get(0).getCoreOrExtension().getFields());
					// System.out.println(nextRecord.getValues());
				}
			}
			assertEquals("Did not find the expected number of records on replica #" + replicaIterations, 2,
					recordCount);
			System.out.println();
		}
	}
}

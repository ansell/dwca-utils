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

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import joptsimple.OptionException;

/**
 * Tests for {@link DarwinCoreArchiveMerger}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveMergerTest {

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

    @Before
    public void setUp() throws Exception {
        testTempDir = tempDir.newFolder("dwca-check-temp").toPath();
        testFile = tempDir.newFolder("dwca-check-input1").toPath().resolve("dwca-test.zip");
        try (OutputStream out = Files.newOutputStream(testFile, StandardOpenOption.CREATE);
                ZipOutputStream zipOut = new ZipOutputStream(out, StandardCharsets.UTF_8);) {
            ZipEntry metadataXml = new ZipEntry(DarwinCoreArchiveChecker.METADATA_XML);
            zipOut.putNextEntry(metadataXml);
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"),
                    zipOut);
            zipOut.closeEntry();
            ZipEntry specimensCsv = new ZipEntry("specimens.csv");
            zipOut.putNextEntry(specimensCsv);
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"),
                    zipOut);
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
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata-to-merge.xml"),
                    zipOut);
            zipOut.closeEntry();
            
            ZipEntry specimensCsv = new ZipEntry("specimens-to-merge.csv");
            zipOut.putNextEntry(specimensCsv);
            // Copy a different specimens.csv file into this archive, to check for mergeability of simple files
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens-to-merge.csv"),
                    zipOut);
            zipOut.flush();
            zipOut.closeEntry();
            zipOut.flush();
            out.flush();
        }
        testFileNoMetadata = tempDir.newFolder("dwca-check-input3").toPath()
                .resolve("dwca-test-no-metadata.zip");
        try (OutputStream out = Files.newOutputStream(testFileNoMetadata,
                StandardOpenOption.CREATE);
                ZipOutputStream zipOut = new ZipOutputStream(out, StandardCharsets.UTF_8);) {
            ZipEntry specimensCsv = new ZipEntry("specimens.csv");
            zipOut.putNextEntry(specimensCsv);
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"),
                    zipOut);
            zipOut.flush();
            zipOut.closeEntry();
            zipOut.flush();
            out.flush();
        }
        testMetadataXmlFolder = tempDir.newFolder("dwca-check-unittest").toPath();
        testMetadataXml = testMetadataXmlFolder.resolve(DarwinCoreArchiveChecker.METADATA_XML);
        testMetadataXmlSpecimensCsv = testMetadataXmlFolder.resolve("specimens.csv");
        try (Writer out = Files.newBufferedWriter(testMetadataXml)) {
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/metadata.xml"),
                    out, StandardCharsets.UTF_8);
        }
        try (Writer out = Files.newBufferedWriter(testMetadataXmlSpecimensCsv)) {
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/specimens.csv"),
                    out, StandardCharsets.UTF_8);
        }
        testMetadataXmlWithExtensionFolder = tempDir
                .newFolder("dwca-check-unittest-with-extensions").toPath();
        testMetadataXmlWithExtension = testMetadataXmlWithExtensionFolder
                .resolve(DarwinCoreArchiveChecker.METADATA_XML);
        testMetadataXmlWhalesTxt = testMetadataXmlWithExtensionFolder.resolve("whales.txt");
        testMetadataXmlTypesCsv = testMetadataXmlWithExtensionFolder.resolve("types.csv");
        testMetadataXmlDistributionCsv = testMetadataXmlWithExtensionFolder
                .resolve("distribution.csv");
        try (Writer out = Files.newBufferedWriter(testMetadataXmlWithExtension)) {
            IOUtils.copy(this.getClass()
                    .getResourceAsStream("/com/github/ansell/dwca/extensionMetadata.xml"), out, StandardCharsets.UTF_8);
        }
        try (Writer out = Files.newBufferedWriter(testMetadataXmlWhalesTxt)) {
            IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/whales.txt"),
                    out, StandardCharsets.UTF_8);
        }
        try (Writer out = Files.newBufferedWriter(testMetadataXmlTypesCsv)) {
            IOUtils.copy(this.getClass().getResourceAsStream("/com/github/ansell/dwca/types.csv"),
                    out, StandardCharsets.UTF_8);
        }
        try (Writer out = Files.newBufferedWriter(testMetadataXmlDistributionCsv)) {
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/distribution.csv"),
                    out, StandardCharsets.UTF_8);
        }
        testMetadataXmlTsvFolder = tempDir.newFolder("dwca-check-unittest-tsv").toPath();
        Files.createDirectories(testMetadataXmlTsvFolder.resolve("subdir"));
        testMetadataXmlTsv = testMetadataXmlTsvFolder.resolve(DarwinCoreArchiveChecker.METADATA_XML);
        testMetadataXmlSpecimensTsv = testMetadataXmlTsvFolder.resolve("subdir").resolve("specimens.tsv");
        try (Writer out = Files.newBufferedWriter(testMetadataXmlTsv)) {
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/tsvmetadata.xml"),
                    out, StandardCharsets.UTF_8);
        }
        try (Writer out = Files.newBufferedWriter(testMetadataXmlSpecimensTsv)) {
            IOUtils.copy(
                    this.getClass().getResourceAsStream("/com/github/ansell/dwca/subdir/specimens.tsv"),
                    out, StandardCharsets.UTF_8);
        }
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainHelp() throws Exception {
    	DarwinCoreArchiveMerger.main("--help");
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMain() throws Exception {
    	DarwinCoreArchiveMerger.main("--input", testFile.toAbsolutePath().toString(), "--other-input", testFile2.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainDebug() throws Exception {
    	DarwinCoreArchiveMerger.main("--input", testFile.toAbsolutePath().toString(), "--other-input", testFile2.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString(), "--debug", "true");
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainReverse() throws Exception {
    	DarwinCoreArchiveMerger.main("--input", testFile2.toAbsolutePath().toString(), "--other-input", testFile.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainCoreIDDoNotMatch() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Core id field terms must match for archives to be merged");
    	DarwinCoreArchiveMerger.main("--input", testFile.toAbsolutePath().toString(), "--other-input", testMetadataXmlWithExtensionFolder.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainInputMissing() throws Exception {
        thrown.expect(FileNotFoundException.class);
        thrown.expectMessage("Could not find input Darwin Core Archive file or metadata file:");
    	DarwinCoreArchiveMerger.main("--input", Paths.get("does", "not", "exist").toString(), "--other-input", testMetadataXmlWithExtensionFolder.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainOtherInputMissing() throws Exception {
        thrown.expect(FileNotFoundException.class);
        thrown.expectMessage("Could not find other input Darwin Core Archive file or metadata file:");
    	DarwinCoreArchiveMerger.main("--input", testFile.toAbsolutePath().toString(), "--other-input", Paths.get("does", "not", "exist").toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainOutputMissing() throws Exception {
        thrown.expect(FileNotFoundException.class);
        thrown.expectMessage("Could not find output folder:");
    	DarwinCoreArchiveMerger.main("--input", testFile.toAbsolutePath().toString(), "--other-input", testMetadataXmlWithExtensionFolder.toAbsolutePath().toString(), "--output", Paths.get("does", "not", "exist").toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainUnrecognisedOption() throws Exception {
        thrown.expect(OptionException.class);
        thrown.expectMessage("unrecognised-option is not a recognized option");
    	DarwinCoreArchiveMerger.main("--unrecognised-option");
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainCoreIDDoNotMatchDebug() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Core id field terms must match for archives to be merged");
    	DarwinCoreArchiveMerger.main("--input", testFile.toAbsolutePath().toString(), "--other-input", testMetadataXmlWithExtensionFolder.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString(), "--debug",
                "true");
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainCoreIDDoNotMatchReverse() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Core id field terms must match for archives to be merged");
    	DarwinCoreArchiveMerger.main("--input", testMetadataXmlWithExtensionFolder.toAbsolutePath().toString(), "--other-input", testFile.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

    /**
     * Test method for
     * {@link com.github.ansell.dwca.DarwinCoreArchiveMerger#main(java.lang.String[])}
     * .
     */
    @Test
    public final void testMainNoMetadata() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Did not find a metadata file in ZIP file");
        DarwinCoreArchiveMerger.main("--input", testFileNoMetadata.toAbsolutePath().toString(), "--other-input", testFile.toAbsolutePath().toString(), "--output", testTempDir.toAbsolutePath().toString());
    }

}

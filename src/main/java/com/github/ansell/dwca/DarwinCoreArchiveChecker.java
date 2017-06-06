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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.xml.sax.SAXException;

import com.github.ansell.csv.stream.CSVStream;
import com.github.ansell.csv.sum.CSVSummariser;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Checks the contents of
 * <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Archives</a>.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveChecker {

    public static final String METADATA_XML = "metadata.xml";
    public static final String META_XML = "meta.xml";

    /**
     * Private constructor for static only class
     */
    private DarwinCoreArchiveChecker() {
    }

    public static void main(String... args) throws Exception {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Void> help = parser.accepts("help").forHelp();
        final OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class)
                .required()
                .describedAs("The input Darwin Core Archive file or metadata file to be checked.");
        final OptionSpec<File> output = parser.accepts("output").withRequiredArg()
                .ofType(File.class).describedAs(
                        "A directory to output summary and other files to. If this is not set, no output will be preserved.");
        final OptionSpec<Boolean> debugOption = parser.accepts("debug").withRequiredArg()
                .ofType(Boolean.class).defaultsTo(Boolean.FALSE)
                .describedAs("Set to true to debug.");

        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (final OptionException e) {
            System.out.println(e.getMessage());
            parser.printHelpOn(System.out);
            throw e;
        }

        if (options.has(help)) {
            parser.printHelpOn(System.out);
            return;
        }

        final boolean debug = debugOption.value(options);

        final Path inputPath = input.value(options).toPath();
        if (!Files.exists(inputPath)) {
            throw new FileNotFoundException(
                    "Could not find input Darwin Core Archive file or metadata file: "
                            + inputPath.toString());
        }

        final Path tempDir = Files.createTempDirectory("dwca-check-");

        try {
            final Path outputDirPath;
            if (options.has(output)) {
                outputDirPath = output.value(options).toPath();
            } else {
                outputDirPath = tempDir;
            }

            final Path metadataPath;
            if (inputPath.getFileName().toString().contains(".zip")) {
                metadataPath = checkZip(inputPath, tempDir);
                if (metadataPath == null) {
                    throw new IllegalStateException("Did not find a metadata file in the ZIP file: "
                            + inputPath.toAbsolutePath().toString());
                }
            } else {
                metadataPath = inputPath;
            }

            DarwinCoreArchiveDocument archiveDocument = parseMetadataXml(metadataPath);
            if (debug) {
                System.out.println(archiveDocument.toString());
            }

            DarwinCoreCoreOrExtension core = archiveDocument.getCore();
            int headerLineCount = core.getIgnoreHeaderLines();
            List<String> coreFields = core.getFields().stream().map(f -> f.getTerm())
                    .collect(Collectors.toList());
            // Only support a single core file
            String coreFileName = core.getFiles().getLocations().get(0);
            Path coreFilePath = metadataPath.resolveSibling(coreFileName).normalize()
                    .toAbsolutePath();
            try (Reader inputReader = Files.newBufferedReader(coreFilePath, core.getEncoding());
                    Writer summaryWriter = Files.newBufferedWriter(
                            outputDirPath
                                    .resolve("Statistics-" + coreFilePath.getFileName().toString()),
                            core.getEncoding());
                    Writer mappingWriter = Files.newBufferedWriter(
                            outputDirPath
                                    .resolve("Mapping-" + coreFilePath.getFileName().toString()),
                            core.getEncoding());) {
                if (options.has(output)) {
                    // Summarise the core document
                    CSVSummariser.runSummarise(inputReader, CSVStream.defaultMapper(),
                            core.getCsvSchema(), summaryWriter, mappingWriter, 20, true, debug,
                            coreFields, headerLineCount);
                } else {
                    CSVStream.parse(inputReader, h -> {
                    }, (h, l) -> l, l -> {
                    }, coreFields, headerLineCount, CSVStream.defaultMapper(), core.getCsvSchema());
                }
            }
        } finally {
            FileUtils.deleteQuietly(tempDir.toFile());
        }
    }

    /**
     * Checks that the zip file given in inputPath contains a valid structure
     * for a Darwin Core Archive zip file, while extracting it to tempDir.
     * 
     * @param inputPath
     *            The Darwin Core Archive zip file.
     * @param tempDir
     *            The temporary directory to extract the zip file to.
     * @return The path to the extracted metadata.xml file, if it existed,
     *         otherwise null.
     * @throws IOException
     *             If there is an input-output exception.
     */
    public static Path checkZip(Path inputPath, Path tempDir) throws IOException {
        Path metadataPath = null;

        final FileSystemManager fsManager = VFS.getManager();
        final FileObject zipFile = fsManager
                .resolveFile("zip:" + inputPath.toAbsolutePath().toString());

        final FileObject[] children = zipFile.getChildren();
        if (children.length == 0) {
            throw new RuntimeException("No files in zip file: " + inputPath);
        }

        for (FileObject nextFile : children) {
            try (InputStream in = nextFile.getContent().getInputStream();) {
                String baseName = nextFile.getName().getBaseName();
                Path nextTempFile = tempDir.resolve(baseName);
                if (baseName.equalsIgnoreCase(METADATA_XML)
                        || baseName.equalsIgnoreCase(META_XML)) {
                    if (metadataPath != null) {
                        throw new RuntimeException("Duplicate metadata.xml files found: original="
                                + metadataPath + " duplicate=" + baseName);
                    }
                    metadataPath = nextTempFile;
                }

                Files.copy(in, nextTempFile);
            }
        }

        if (metadataPath == null) {
            throw new IllegalStateException("Did not find a metadata file in the ZIP file: "
                    + inputPath.toAbsolutePath().toString());
        }

        return metadataPath;
    }

    /**
     * Parses the metadata.xml file.
     * 
     * @param metadataPath
     *            The path to the metadata.xml file to parse.
     * @return An instance of {@link DarwinCoreArchiveDocument} representing the
     *         parsed document.
     * @throws IOException
     *             If there is an input-output exception.
     * @throws SAXException
     *             If there is an exception parsing the XML document.
     * @throws IllegalStateException
     *             If there is an exception interpreting the context of parts of
     *             the document that violate the state assumptions in the
     *             specification.
     */
    public static DarwinCoreArchiveDocument parseMetadataXml(Path metadataPath)
            throws IOException, SAXException, IllegalStateException {
        try (Reader input = Files.newBufferedReader(metadataPath);) {
            return DarwinCoreMetadataSaxParser.parse(input);
        }
    }

}

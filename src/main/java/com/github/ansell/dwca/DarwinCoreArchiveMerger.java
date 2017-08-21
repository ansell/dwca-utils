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
import com.github.ansell.csv.stream.CSVStreamException;
import com.github.ansell.csv.sum.CSVSummariser;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Merges multiple
 * <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Archives</a> into a single resulting archive.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveMerger {

    public static final String METADATA_XML = "metadata.xml";
    public static final String META_XML = "meta.xml";

    /**
     * Private constructor for static only class
     */
    private DarwinCoreArchiveMerger() {
    }

    public static void main(String... args) throws Exception {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Void> help = parser.accepts("help").forHelp();
        final OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class)
                .required()
                .describedAs("The base input Darwin Core Archive file to be merged.");
        final OptionSpec<File> otherInput = parser.accepts("other-input").withRequiredArg().ofType(File.class)
                .required()
                .describedAs("The other input Darwin Core Archive file to be merged.");
        final OptionSpec<File> output = parser.accepts("output").withRequiredArg()
                .ofType(File.class).required().describedAs(
                        "A directory to output summary and other files to.");
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

        final Path otherInputPath = otherInput.value(options).toPath();
        if (!Files.exists(otherInputPath)) {
            throw new FileNotFoundException(
                    "Could not find other input Darwin Core Archive file or metadata file: "
                            + otherInputPath.toString());
        }

        final Path outputDirPath = output.value(options).toPath();
        if (!Files.exists(outputDirPath)) {
            throw new FileNotFoundException(
                    "Could not find output folder: "
                            + outputDirPath.toString());
        }

        final Path tempDir = Files.createTempDirectory("dwca-merge-");

        try {

            final Path outputArchivePath = outputDirPath.resolve("first-archive");
            final Path inputMetadataPath = openArchive(inputPath, outputArchivePath);
            Files.createDirectories(outputArchivePath);
            final DarwinCoreArchiveDocument inputArchiveDocument = loadArchive(debug, outputArchivePath, inputMetadataPath);
            System.out.println("Found an archive with " + inputArchiveDocument.getCore().getFields().size() + " core fields and " + inputArchiveDocument.getExtensions().size() + " extensions");
            

            final Path otherOutputArchivePath = outputDirPath.resolve("other-archive");
            final Path otherInputMetadataPath = openArchive(otherInputPath, otherOutputArchivePath);
            Files.createDirectories(otherOutputArchivePath);
            final DarwinCoreArchiveDocument otherInputArchiveDocument = loadArchive(debug, otherOutputArchivePath, otherInputMetadataPath);
            System.out.println("Found another archive with " + otherInputArchiveDocument.getCore().getFields().size() + " core fields and " + otherInputArchiveDocument.getExtensions().size() + " extensions");
            
            canArchivesBeMergedDirectly(inputArchiveDocument, otherInputArchiveDocument);
            
        } finally {
            FileUtils.deleteQuietly(tempDir.toFile());
        }
    }

	private static void canArchivesBeMergedDirectly(DarwinCoreArchiveDocument inputArchiveDocument,
			DarwinCoreArchiveDocument otherInputArchiveDocument) {
		System.out.println("coreId: input=" + inputArchiveDocument.getCore().getIdOrCoreId() + " other=" + otherInputArchiveDocument.getCore().getIdOrCoreId());
	}

	private static DarwinCoreArchiveDocument loadArchive(final boolean debug, final Path outputDirPath,
			final Path inputMetadataPath) throws IOException, SAXException, IllegalStateException, CSVStreamException {
		DarwinCoreArchiveDocument inputArchiveDocument = DarwinCoreArchiveChecker.parseMetadataXml(inputMetadataPath);
		if (debug) {
		    System.out.println(inputArchiveDocument.toString());
		}

		DarwinCoreCoreOrExtension core = inputArchiveDocument.getCore();
		DarwinCoreArchiveChecker.checkCoreOrExtension(core, inputMetadataPath, outputDirPath, true, debug);
		for(DarwinCoreCoreOrExtension extension : inputArchiveDocument.getExtensions()) {
		    DarwinCoreArchiveChecker.checkCoreOrExtension(extension, inputMetadataPath, outputDirPath, true, debug);
		}
		return inputArchiveDocument;
	}

	private static Path openArchive(final Path inputPath, final Path tempDir)
			throws IOException, IllegalStateException {
		final Path inputMetadataPath;
		if (inputPath.getFileName().toString().contains(".zip")) {
		    inputMetadataPath = DarwinCoreArchiveChecker.checkZip(inputPath, tempDir);
		    if (inputMetadataPath == null) {
		        throw new IllegalStateException("Did not find a metadata file in the input ZIP file: "
		                + inputPath.toAbsolutePath().toString());
		    }
		} else {
		    inputMetadataPath = DarwinCoreArchiveChecker.checkFolder(inputPath);
		}
		return inputMetadataPath;
	}

    /**
     * @param coreOrExtension
     * @param metadataPath
     * @param outputDirPath
     * @param hasOutput
     * @param debug
     * @throws IOException
     * @throws CSVStreamException
     */
    public static void checkCoreOrExtension(DarwinCoreCoreOrExtension coreOrExtension,
            final Path metadataPath, final Path outputDirPath, boolean hasOutput,
            final boolean debug) throws IOException, CSVStreamException {
        int headerLineCount = coreOrExtension.getIgnoreHeaderLines();
        List<String> coreOrExtensionFields = coreOrExtension.getFields().stream().map(f -> f.getTerm())
                .collect(Collectors.toList());
        // TODO: Only support a single file currently
        String coreOrExtensionFileName = coreOrExtension.getFiles().getLocations().get(0);
        Path coreOrExtensionFilePath = metadataPath.resolveSibling(coreOrExtensionFileName).normalize()
                .toAbsolutePath();
        try (Reader inputReader = Files.newBufferedReader(coreOrExtensionFilePath, coreOrExtension.getEncoding());) {
            if (hasOutput) {
                try (Writer summaryWriter = Files.newBufferedWriter(
                        outputDirPath
                                .resolve("Statistics-" + coreOrExtensionFilePath.getFileName().toString()),
                        coreOrExtension.getEncoding());
                        Writer mappingWriter = Files.newBufferedWriter(
                                outputDirPath.resolve(
                                        "Mapping-" + coreOrExtensionFilePath.getFileName().toString()),
                                coreOrExtension.getEncoding());) {
                    // Summarise the core document
                    CSVSummariser.runSummarise(inputReader, CSVStream.defaultMapper(),
                            coreOrExtension.getCsvSchema(), summaryWriter, mappingWriter, 20, true, debug,
                            coreOrExtensionFields, headerLineCount);
                }
            } else {
                CSVStream.parse(inputReader, h -> {
                }, (h, l) -> l, l -> {
                }, coreOrExtensionFields, headerLineCount, CSVStream.defaultMapper(), coreOrExtension.getCsvSchema());
            }
        }
    }

}

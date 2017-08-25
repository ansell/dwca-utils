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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.jooq.lambda.Unchecked;
import org.xml.sax.SAXException;

import com.github.ansell.csv.stream.CSVStream;
import com.github.ansell.csv.stream.CSVStreamException;
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
		final OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class).required()
				.describedAs("The input Darwin Core Archive file or metadata file to be checked.");
		final OptionSpec<File> output = parser.accepts("output").withRequiredArg().ofType(File.class).describedAs(
				"A directory to output summary and other files to. If this is not set, no output will be preserved.");
		final OptionSpec<Boolean> debugOption = parser.accepts("debug").withRequiredArg().ofType(Boolean.class)
				.defaultsTo(Boolean.FALSE).describedAs("Set to true to debug.");

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
					"Could not find input Darwin Core Archive file or metadata file: " + inputPath.toString());
		}

		final Path tempDir = Files.createTempDirectory("dwca-check-");

		try {
			final Path outputDirPath;
			boolean hasOutput = options.has(output);
			if (hasOutput) {
				outputDirPath = output.value(options).toPath();
			} else {
				outputDirPath = tempDir;
			}

			final Path metadataPath;
			if (inputPath.getFileName().toString().contains(".zip")) {
				metadataPath = checkZip(inputPath, tempDir);
				if (metadataPath == null) {
					throw new IllegalStateException(
							"Did not find a metadata file in the ZIP file: " + inputPath.toAbsolutePath().toString());
				}
			} else {
				metadataPath = inputPath;
			}

			DarwinCoreArchiveDocument archiveDocument = parseMetadataXml(metadataPath);
			if (debug) {
				System.out.println(archiveDocument.toString());
			}

			DarwinCoreCoreOrExtension core = archiveDocument.getCore();
			checkCoreOrExtension(core, metadataPath, outputDirPath, hasOutput, debug);
			for (DarwinCoreCoreOrExtension extension : archiveDocument.getExtensions()) {
				checkCoreOrExtension(extension, metadataPath, outputDirPath, hasOutput, debug);
			}
		} finally {
			FileUtils.deleteQuietly(tempDir.toFile());
		}
	}

	/**
	 * Parses and summarises, if output is required, the files for a
	 * {@link DarwinCoreCoreOrExtension}.
	 * 
	 * @param coreOrExtension
	 *            The core or extension to parse
	 * @param metadataPath
	 *            The path to the metadata, which is used to relatively resolve
	 *            the data file locations.
	 * @param outputDirPath
	 *            The output directory path if output is required
	 * @param hasOutput
	 *            True to generate statistical output and false to simply
	 *            attempt to parse the file to determine if it is syntactically
	 *            valid.
	 * @param debug
	 *            True to emit debug messages
	 * @throws IOException
	 *             If there are issues accessing or reading the files.
	 * @throws CSVStreamException
	 *             If there are CSV syntax errors.
	 */
	public static void checkCoreOrExtension(DarwinCoreCoreOrExtension coreOrExtension, final Path metadataPath,
			final Path outputDirPath, boolean hasOutput, final boolean debug) throws IOException, CSVStreamException {
		int headerLineCount = coreOrExtension.getIgnoreHeaderLines();
		List<String> coreOrExtensionFields = coreOrExtension.getFields().stream().map(f -> f.getTerm())
				.collect(Collectors.toList());
		// TODO: Only support a single file currently
		String coreOrExtensionFileName = coreOrExtension.getFiles().getLocations().get(0);
		Path coreOrExtensionFilePath = metadataPath.resolveSibling(coreOrExtensionFileName).normalize()
				.toAbsolutePath();
		Consumer<Reader> summariseFunction = Unchecked.consumer(inputReader -> {
			try (Writer summaryWriter = Files.newBufferedWriter(
					outputDirPath.resolve("Statistics-" + coreOrExtensionFilePath.getFileName().toString()),
					coreOrExtension.getEncoding());
					Writer mappingWriter = Files.newBufferedWriter(
							outputDirPath.resolve("Mapping-" + coreOrExtensionFilePath.getFileName().toString()),
							coreOrExtension.getEncoding());) {
				// Summarise the core document
				CSVSummariser.runSummarise(inputReader, CSVStream.defaultMapper(), coreOrExtension.getCsvSchema(),
						summaryWriter, mappingWriter, 20, true, debug, coreOrExtensionFields, headerLineCount);
			}
		});
		Consumer<Reader> parseFunction = Unchecked.consumer(inputReader -> {
			CSVStream.parse(inputReader, h -> {
			}, (h, l) -> l, l -> {
			}, coreOrExtensionFields, headerLineCount, CSVStream.defaultMapper(), coreOrExtension.getCsvSchema());
		});
		// Parse the core or extension, either using the summarise or parse
		// function as necessary to generate output or otherwise
		parseCoreOrExtension(coreOrExtension, metadataPath, hasOutput ? summariseFunction : parseFunction);
	}

	/**
	 * Parses and summarises, if output is required, the files for a
	 * {@link DarwinCoreCoreOrExtension}.
	 * 
	 * @param coreOrExtension
	 *            The core or extension to parse
	 * @param metadataPath
	 *            The path to the metadata, which is used to relatively resolve
	 *            the data file locations.
	 * @param parseFunction
	 *            The {@link Consumer} which is used to parse the core or
	 *            extension.
	 * @throws IOException
	 *             If there are issues accessing or reading the files.
	 */
	public static void parseCoreOrExtension(DarwinCoreCoreOrExtension coreOrExtension, Path metadataPath,
			Consumer<Reader> parseFunction) throws IOException {
		List<String> coreOrExtensionFields = coreOrExtension.getFields().stream().map(f -> f.getTerm())
				.collect(Collectors.toList());
		// TODO: Only support a single file currently
		String coreOrExtensionFileName = coreOrExtension.getFiles().getLocations().get(0);
		Path coreOrExtensionFilePath = metadataPath.resolveSibling(coreOrExtensionFileName).normalize()
				.toAbsolutePath();
		try (Reader inputReader = Files.newBufferedReader(coreOrExtensionFilePath, coreOrExtension.getEncoding());) {
			parseFunction.accept(inputReader);
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
	 * @throws IllegalStateException
	 *             If there is not exactly one file named either meta.xml or
	 *             metadata.xml
	 */
	public static Path checkZip(Path inputPath, Path tempDir) throws IOException {
		Path metadataPath = null;

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject zipFile = fsManager.resolveFile("zip:" + inputPath.toAbsolutePath().toString());

		final FileObject[] children = zipFile.getChildren();
		if (children.length == 0) {
			throw new RuntimeException("No files in zip file: " + inputPath);
		}

		for (FileObject nextFile : children) {
			try (InputStream in = nextFile.getContent().getInputStream();) {
				String baseName = nextFile.getName().getBaseName();
				String pathName = nextFile.getName().getPath();
				Path nextTempFile = tempDir.resolve("./" + pathName).toAbsolutePath().normalize();
				System.out.println("nextFile=" + nextFile.toString() + " baseName=" + baseName + " pathName=" + pathName
						+ " nextTempFile=" + nextTempFile);
				if (baseName.equalsIgnoreCase(METADATA_XML) || baseName.equalsIgnoreCase(META_XML)) {
					if (metadataPath != null) {
						throw new IllegalStateException("Duplicate metadata.xml files found in ZIP file: first="
								+ metadataPath + " duplicate=" + baseName);
					}
					metadataPath = nextTempFile;
				}

				Files.createDirectories(nextTempFile.getParent());
				Files.copy(in, nextTempFile);
			}
		}

		if (metadataPath == null) {
			throw new IllegalStateException(
					"Did not find a metadata file in ZIP file: " + inputPath.toAbsolutePath().toString());
		}

		return metadataPath;
	}

	/**
	 * Checks that the folder given in inputPath contains a valid structure for
	 * a Darwin Core Archive folder.
	 * 
	 * @param inputPath
	 *            The Darwin Core Archive zip file.
	 * @return The path to the archive metadata.xml file, if it existed,
	 *         otherwise null.
	 * @throws IOException
	 *             If there is an input-output exception.
	 * @throws IllegalStateException
	 *             If there is not exactly one file named either meta.xml or
	 *             metadata.xml
	 */
	public static Path checkFolder(Path inputPath) throws IOException {
		List<Path> metadataFound = Files.walk(inputPath).filter(
				p -> p.getFileName().toString().equals(META_XML) || p.getFileName().toString().equals(METADATA_XML))
				.collect(Collectors.toList());

		if (metadataFound.isEmpty()) {
			throw new IllegalStateException(
					"Did not find a metadata file in folder: " + inputPath.toAbsolutePath().toString());
		} else if (metadataFound.size() > 1) {
			throw new IllegalStateException("Duplicate metadata files found in folder: first=" + metadataFound.get(0)
					+ " duplicate=" + metadataFound.get(1));
		}

		return metadataFound.get(0);
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

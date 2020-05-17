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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.jooq.lambda.Unchecked;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.ansell.csv.sort.CSVSorter;
import com.github.ansell.csv.sort.StringList;
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
		final OptionSpec<File> tempDirOption = parser.accepts("temp-dir").withRequiredArg().ofType(File.class).describedAs(
				"A directory to to write temporary files to.");
		final OptionSpec<Boolean> includeDefaultsOption = parser.accepts("include-defaults").withRequiredArg()
				.ofType(Boolean.class).defaultsTo(Boolean.TRUE)
				.describedAs("Whether to include default values from the meta.xml file in each archive.");
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

		final boolean includeDefaults = includeDefaultsOption.value(options);

		final Path inputPath = input.value(options).toPath().toAbsolutePath().normalize();
		if (!Files.exists(inputPath)) {
			throw new FileNotFoundException(
					"Could not find input Darwin Core Archive file or metadata file: " + inputPath.toString());
		}

		final Path tempDir;
		if (options.has(tempDirOption)) {
			tempDir = Files.createTempDirectory(tempDirOption.value(options).toPath(), "dwca-check-").toAbsolutePath().normalize();
		} else {
			tempDir = Files.createTempDirectory("dwca-check-").toAbsolutePath().normalize();
		}

		try {
			final Path outputDirPath;
			boolean hasOutput = options.has(output);
			if (hasOutput) {
				outputDirPath = output.value(options).toPath();
			} else {
				outputDirPath = tempDir;
			}

			// Override java.io.tmpdir system property so everything thinks this is the temporary directory for this JVM
			// This includes cases where we used the standard temp dir, but want to remove all files reliably before returning for other applications that also use temp files
			System.setProperty("java.io.tmpdir", tempDir.toAbsolutePath().toString());

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
			checkCoreOrExtension(core, metadataPath, outputDirPath, hasOutput, debug, includeDefaults);
			for (DarwinCoreCoreOrExtension extension : archiveDocument.getExtensions()) {
				checkCoreOrExtension(extension, metadataPath, outputDirPath, hasOutput, debug, includeDefaults);
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
	public static void checkCoreOrExtension(final DarwinCoreCoreOrExtension coreOrExtension, final Path metadataPath,
			final Path outputDirPath, final boolean hasOutput, final boolean debug, final boolean includeDefaults)
			throws IOException, CSVStreamException {
		final Consumer<Reader> summariseFunction = createSummariseFunction(coreOrExtension, metadataPath, outputDirPath,
				debug, includeDefaults);
		final Consumer<Reader> parseFunction = createParseFunction(coreOrExtension, includeDefaults);
		// Parse the core or extension, either using the summarise or parse
		// function as necessary to generate output or otherwise
		parseCoreOrExtension(coreOrExtension, metadataPath, hasOutput ? summariseFunction : parseFunction);
	}

	/**
	 * Creates a summarising function, processing all of the lines to validate
	 * the CSV syntax and summarise the field contents.
	 * 
	 * @param coreOrExtension
	 *            The {@link DarwinCoreCoreOrExtension} to parse and summarise.
	 * @param metadataPath
	 *            The path to the metadata file, to resolve the location and
	 *            file name of the extension files, to create names for the
	 *            statistics files.
	 * @param outputDirPath
	 *            The path to contain the output.
	 * @param debug
	 *            Whether to emit debugging statements.
	 * @return A {@link Consumer} that can accept a Reader containing the CSV
	 *         file to parse the content of the given core or extension.
	 */
	private static Consumer<Reader> createSummariseFunction(final DarwinCoreCoreOrExtension coreOrExtension,
			final Path metadataPath, final Path outputDirPath, final boolean debug, final boolean includeDefaults) {
		final List<String> coreOrExtensionFields = coreOrExtension.getFields().stream().map(f -> f.getTerm())
				.collect(Collectors.toList());
		return Unchecked.consumer(inputReader -> {
			// FIXME: We only support a single file currently
			final String coreOrExtensionFileName = coreOrExtension.getFiles().getLocations().get(0);
			final Path coreOrExtensionFilePath = metadataPath.resolveSibling(coreOrExtensionFileName).normalize()
					.toAbsolutePath();
			try (final Writer summaryWriter = Files.newBufferedWriter(
					outputDirPath.resolve("Statistics-" + coreOrExtensionFilePath.getFileName().toString()),
					coreOrExtension.getEncoding());
					final Writer mappingWriter = Files.newBufferedWriter(
							outputDirPath.resolve("Mapping-" + coreOrExtensionFilePath.getFileName().toString()),
							coreOrExtension.getEncoding());) {
				// Summarise the core document
				CSVSummariser.runSummarise(inputReader, CSVStream.defaultMapper(), coreOrExtension.getCsvSchema(),
						summaryWriter, mappingWriter, 20, true, debug, coreOrExtensionFields,
						includeDefaults ? coreOrExtension.getDefaultValues() : Collections.emptyList(), coreOrExtension.getIgnoreHeaderLines());
			}
		});
	}

	/**
	 * Creates a pure parse function, without processing any of the lines, in
	 * order to validate the CSV syntax, but not the content, apart from
	 * checking that line lengths are consistent.
	 * 
	 * @param coreOrExtension
	 *            The {@link DarwinCoreCoreOrExtension} to parse.
	 * @return A {@link Consumer} that can accept a Reader containing the CSV
	 *         file to parse the content of the given core or extension.
	 */
	public static Consumer<Reader> createParseFunction(final DarwinCoreCoreOrExtension coreOrExtension, boolean includeDefaults) {
		// Null implementations of the three CSVStream.parse functions to just
		// validate the syntax and line lengths
		Consumer<List<String>> headersValidator = h -> {
		};
		BiFunction<List<String>, List<String>, List<String>> lineConverter = (h, l) -> l;
		Consumer<List<String>> resultConsumer = l -> {
		};
		return createParseFunction(coreOrExtension, headersValidator, lineConverter, resultConsumer, includeDefaults);
	}

	/**
	 * Creates a pure parse function, without processing any of the lines, in
	 * order to validate the CSV syntax, but not the content, apart from
	 * checking that line lengths are consistent.
	 * 
	 * @param coreOrExtension
	 *            The {@link DarwinCoreCoreOrExtension} to parse.
	 * @return A {@link Consumer} that can accept a Reader containing the CSV
	 *         file to parse the content of the given core or extension.
	 */
	public static <T> Consumer<Reader> createParseFunction(final DarwinCoreCoreOrExtension coreOrExtension,
			final Consumer<List<String>> headersValidator,
			final BiFunction<List<String>, List<String>, T> lineConverter, final Consumer<T> resultConsumer,
			final boolean includeDefaults) {
		final List<String> coreOrExtensionFields = coreOrExtension.getFields().stream().map(f -> f.getTerm())
				.collect(Collectors.toList());
		return Unchecked.consumer(inputReader -> {
			CSVStream.parse(inputReader, headersValidator, lineConverter, resultConsumer, coreOrExtensionFields,
					includeDefaults ? coreOrExtension.getDefaultValues() : Collections.emptyList(),
					coreOrExtension.getIgnoreHeaderLines(), CSVStream.defaultMapper(), coreOrExtension.getCsvSchema());
		});
	}

	/**
	 * Create a parse function to parse an entire document.
	 * 
	 * @param document
	 *            The {@link DarwinCoreArchiveDocument} to parse.
	 * @return A {@link Consumer} that can accept a Reader containing the merged
	 *         CSV file to parse the content of the document as a whole.
	 */
	public static <T> Consumer<Reader> createParseFunction(final DarwinCoreArchiveDocument document,
			final Consumer<List<String>> headersValidator,
			final BiFunction<List<String>, List<String>, T> lineConverter, final Consumer<T> resultConsumer) {
		throw new UnsupportedOperationException("TODO: Implement me after merging is implemented");
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
	public static void parseCoreOrExtension(final DarwinCoreCoreOrExtension coreOrExtension, final Path metadataPath,
			final Consumer<Reader> parseFunction) throws IOException {
		// TODO: Only support a single file currently
		final String coreOrExtensionFileName = coreOrExtension.getFiles().getLocations().get(0);
		final Path coreOrExtensionFilePath = metadataPath.resolveSibling(coreOrExtensionFileName).normalize()
				.toAbsolutePath();
		try (final Reader inputReader = Files.newBufferedReader(coreOrExtensionFilePath,
				coreOrExtension.getEncoding());) {
			parseFunction.accept(inputReader);
		}
	}

	/**
	 * Parses and summarises, if output is required, the files for a
	 * {@link DarwinCoreCoreOrExtension}, after sorting the input.
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
	public static void parseCoreOrExtensionSorted(final DarwinCoreCoreOrExtension coreOrExtension,
			final Path metadataPath, final Consumer<Reader> parseFunction, boolean debug) throws IOException {
		Function<DarwinCoreCoreOrExtension, Comparator<StringList>> comparator = core -> CSVSorter
				.getComparator(Arrays.asList(Integer.parseInt(core.getIdOrCoreId() == null ? "0" : core.getIdOrCoreId())));
		parseCoreOrExtensionSorted(coreOrExtension, metadataPath, parseFunction, comparator, debug);
	}

	/**
	 * Parses and summarises, if output is required, the files for a
	 * {@link DarwinCoreCoreOrExtension}, after sorting the input.
	 * 
	 * @param coreOrExtension
	 *            The core or extension to parse
	 * @param metadataPath
	 *            The path to the metadata, which is used to relatively resolve
	 *            the data file locations.
	 * @param parseFunction
	 *            The {@link Consumer} which is used to parse the core or
	 *            extension.
	 * @param comparator
	 *            Function to generate a {@link Comparator} which is used to
	 *            compare primary keys.
	 * @throws IOException
	 *             If there are issues accessing or reading the files.
	 */
	public static void parseCoreOrExtensionSorted(final DarwinCoreCoreOrExtension coreOrExtension,
			final Path metadataPath, final Consumer<Reader> parseFunction,
			Function<DarwinCoreCoreOrExtension, Comparator<StringList>> comparator, boolean debug) throws IOException {
		// TODO: Only support a single file currently
		final String coreOrExtensionFileName = coreOrExtension.getFiles().getLocations().get(0);
		final Path coreOrExtensionFilePath = metadataPath.resolveSibling(coreOrExtensionFileName).normalize()
				.toAbsolutePath();
		final Path sortedCoreOrExtensionFilePath = coreOrExtensionFilePath
				.resolveSibling("sorted-" + coreOrExtensionFilePath.getFileName().toString());

		// Delete the sorted file if it exists and recreate it
		Files.deleteIfExists(sortedCoreOrExtensionFilePath);

		try (final Reader otherInputReader = Files.newBufferedReader(coreOrExtensionFilePath,
				coreOrExtension.getEncoding())) {
			CsvSchema csvSchema = coreOrExtension.getCsvSchema();
			CSVSorter.runSorter(otherInputReader, sortedCoreOrExtensionFilePath,
					coreOrExtension.getIgnoreHeaderLines(), csvSchema, comparator.apply(coreOrExtension), debug);
		}

		try (final Reader inputReader = Files.newBufferedReader(sortedCoreOrExtensionFilePath,
				coreOrExtension.getEncoding());) {
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
	/**
	 * @param inputPath
	 * @param tempDir
	 * @return
	 * @throws IOException
	 */
	public static Path checkZip(Path inputPath, Path tempDir) throws IOException {
		Path metadataPath = null;

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject zipFile = fsManager.resolveFile("zip:" + inputPath.toAbsolutePath().toString());

		final FileObject[] children = zipFile.getChildren();
		if (children.length == 0) {
			throw new RuntimeException("No files in zip file: " + inputPath);
		}

		metadataPath = copyChildrenRecursive(tempDir, metadataPath, children);

		if (metadataPath == null) {
			throw new IllegalStateException(
					"Did not find a metadata file in ZIP file: " + inputPath.toAbsolutePath().toString());
		}

		return metadataPath;
	}

    private static Path copyChildrenRecursive(Path tempDir, Path metadataPath,
            final FileObject[] children) throws IOException, FileSystemException {
        for (FileObject nextFile : children) {
            String pathName = nextFile.getName().getPath();
            Path nextTempFile = tempDir.resolve("./" + pathName).toAbsolutePath().normalize();
            Files.createDirectories(nextTempFile.getParent());
		    if(nextFile.isFolder()) {
		        Files.createDirectories(nextTempFile.getFileName());
		        copyChildrenRecursive(tempDir, metadataPath, nextFile.getChildren());
		    } else if(nextFile.isFile()) {
    			try (InputStream in = nextFile.getContent().getInputStream();) {
    				String baseName = nextFile.getName().getBaseName();
    				System.out.println("nextFile=" + nextFile.toString() + " baseName=" + baseName + " pathName=" + pathName
    						+ " nextTempFile=" + nextTempFile);
    				if (baseName.equalsIgnoreCase(METADATA_XML) || baseName.equalsIgnoreCase(META_XML)) {
    					if (metadataPath != null) {
    						throw new IllegalStateException("Duplicate metadata.xml files found in ZIP file: first="
    								+ metadataPath + " duplicate=" + baseName);
    					}
    					metadataPath = nextTempFile;
    				}
    
    				Files.copy(in, nextTempFile);
    			}
		    }
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
			DarwinCoreArchiveDocument result = DarwinCoreMetadataSaxParser.parse(input);
			result.setMetadataXMLPath(metadataPath);
			return result;
		}
	}

}

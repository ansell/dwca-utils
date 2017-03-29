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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.NullWriter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import com.github.ansell.csv.stream.CSVStream;
import com.github.ansell.csv.sum.CSVSummariser;
import com.github.ansell.csv.util.CSVUtil;
import com.github.ansell.jdefaultdict.JDefaultDict;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Parses the headers from a CSV file and creates a metadata.xml file from the
 * fields that are found
 * <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Archives</a>.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreMetadataGenerator {

	public static final String METADATA_XML = "metadata.xml";

	/**
	 * Private constructor for static only class
	 */
	private DarwinCoreMetadataGenerator() {
	}

	public static void main(String... args) throws Exception {
		final OptionParser parser = new OptionParser();

		final OptionSpec<Void> help = parser.accepts("help").forHelp();
		final OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class).required()
				.describedAs("The input CSV file to be parsed.");
		final OptionSpec<File> overrideHeadersFile = parser.accepts("override-headers-file").withRequiredArg()
				.ofType(File.class).describedAs(
						"A file whose first line contains the headers to use, to override those found in the file.");
		final OptionSpec<Integer> headerLineCount = parser.accepts("header-line-count").withRequiredArg()
				.ofType(Integer.class)
				.describedAs(
						"The number of header lines present in the file. Can be used in conjunction with override-headers-file to substitute a different set of headers")
				.defaultsTo(1);
		final OptionSpec<File> extensionOption = parser.accepts("extension").withRequiredArg().ofType(File.class)
				.describedAs("Extension CSV files to be included. May be used multiple times if needed.");
		final OptionSpec<File> output = parser.accepts("output").withRequiredArg().ofType(File.class).required()
				.describedAs("The output metadata.xml file to be generated.");
		final OptionSpec<Boolean> showDefaults = parser.accepts("show-defaults").withOptionalArg().ofType(Boolean.class)
				.defaultsTo(Boolean.FALSE).describedAs(
						"Show default values that should be assumed from the spec but may cause bugs with some implementations.");
		final OptionSpec<Boolean> debug = parser.accepts("debug").withRequiredArg().ofType(Boolean.class)
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

		final Path inputPath = input.value(options).toPath();
		if (!Files.exists(inputPath)) {
			throw new FileNotFoundException(
					"Could not find input Darwin Core Archive file or metadata file: " + inputPath.toString());
		}

		final Path outputPath = output.value(options).toPath();
		if (Files.exists(outputPath)) {
			throw new IllegalStateException("Output file already exists, not overwriting it: " + outputPath.toString());
		}

		List<Path> extensionPaths = new ArrayList<>();
		for (File nextExtensionFile : extensionOption.values(options)) {
			Path nextExtensionPath = nextExtensionFile.toPath();
			if (Files.exists(nextExtensionPath)) {
				extensionPaths.add(nextExtensionPath);
			}
		}

		int headerLineCountInt = headerLineCount.value(options);
		boolean debugBoolean = debug.value(options);
		
		// Defaults to null, with any strings in the file overriding that
		AtomicReference<List<String>> overrideHeadersList = new AtomicReference<>();
		if(options.has(overrideHeadersFile)) {
			try (final BufferedReader newBufferedReader = Files
					.newBufferedReader(overrideHeadersFile.value(options).toPath());) {
				CSVStream.parse(newBufferedReader, h -> {
					overrideHeadersList.set(h);
				}, (h, l) -> {
					return l;
				}, l -> {
				}, null, CSVStream.DEFAULT_HEADER_COUNT);
			}
		}

		try(Reader inputReader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);) {
			CSVSummariser.runSummarise(inputReader, new OutputStreamWriter(System.out, StandardCharsets.UTF_8), new NullWriter(), 20, true, debugBoolean, overrideHeadersList.get(), headerLineCountInt);
		}
		
		Map<String, Map<String, List<IRI>>> vocabMap = new JDefaultDict<>(
				k -> new JDefaultDict<>(l -> new ArrayList<>()));

		// Darwin Core
		String pathToDWCRDF = "/dwcterms.rdf";
		String iriForDWCRDF = DarwinCoreArchiveConstants.DWC_TERMS;
		parseRDF(pathToDWCRDF, iriForDWCRDF, vocabMap, RDFFormat.RDFXML);

		// Dublin Core
		String pathToDCTERMSRDF = "/dcterms.rdf";
		String iriForDCTERMSRDF = DCTERMS.NAMESPACE;
		parseRDF(pathToDCTERMSRDF, iriForDCTERMSRDF, vocabMap, RDFFormat.RDFXML);

		// Audubon Core
		String pathToACRDF = "/acterms.ttl";
		String iriForACRDF = DarwinCoreArchiveConstants.AC_TERMS;
		parseRDF(pathToACRDF, iriForACRDF, vocabMap, RDFFormat.TURTLE);

		// Global Names Architecture
		String pathToGNARDF = "/gna.rdf";
		String iriForGNARDF = DarwinCoreArchiveConstants.GNA_TERMS;
		parseRDF(pathToGNARDF, iriForGNARDF, vocabMap, RDFFormat.RDFXML);

		DarwinCoreArchiveDocument result = new DarwinCoreArchiveDocument();
		DarwinCoreCoreOrExtension core = DarwinCoreCoreOrExtension.newCore();
		core.setRowType(DarwinCoreArchiveConstants.SIMPLE_DARWIN_RECORD);
		core.setIdOrCoreId("0");
		core.setIgnoreHeaderLines(1);
		core.setLinesTerminatedBy("\n");
		core.setFieldsTerminatedBy(",");
		result.setCore(core);
		DarwinCoreFile coreFile = new DarwinCoreFile();
		coreFile.addLocation(inputPath.getFileName().toString());
		result.getCore().setFiles(coreFile);

		populateFields(inputPath, vocabMap, core);

		for (Path nextExtensionPath : extensionPaths) {
			DarwinCoreCoreOrExtension nextExtension = DarwinCoreCoreOrExtension.newExtension();
			nextExtension.setRowType(DarwinCoreArchiveConstants.MULTIMEDIA_RECORD);
			// TODO: Choose this from a predefined list such as "catalogNumber"
			// Could also csvsum to get likely primary keys based on uniqueness
			nextExtension.setIdOrCoreId("0");
			nextExtension.setIgnoreHeaderLines(1);
			DarwinCoreFile nextExtensionFile = new DarwinCoreFile();
			nextExtensionFile.addLocation(nextExtensionPath.getFileName().toString());
			nextExtension.setFiles(nextExtensionFile);

			populateFields(nextExtensionPath, vocabMap, nextExtension);
			// We completely ignore empty files
			if (!nextExtension.getFields().isEmpty()) {
				result.addExtension(nextExtension);
			}
		}

		try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE_NEW);) {
			result.toXML(writer, showDefaults.value(options));
		}

		// Parse the result to make sure that it is valid
		DarwinCoreArchiveDocument archiveDocument = DarwinCoreArchiveChecker.parseMetadataXml(outputPath);
		archiveDocument.checkConstraints();
	}

	/**
	 * Parse an RDF vocabulary into the given vocabMap.
	 * 
	 * @param pathToVocab
	 *            The path to the vocabulary on the classpath
	 * @param iriForVocab
	 *            The base IRI for the vocabulary file
	 * @param vocabMap
	 *            The map to parse the vocabulary into
	 * @param rdfFormat
	 *            The RDF format that the vocabulary file is in.
	 * @throws IOException
	 *             If there is an IO exception accessing the file on the
	 *             classpath.
	 * @throws RDFParseException
	 *             If there is an exception while parsing the file as RDF.
	 * @throws UnsupportedRDFormatException
	 *             If the given RDF format does not have a parser on the
	 *             classpath.
	 */
	public static void parseRDF(String pathToVocab, String iriForVocab, Map<String, Map<String, List<IRI>>> vocabMap,
			RDFFormat rdfFormat) throws IOException, RDFParseException, UnsupportedRDFormatException {
		Model model = Rio.parse(DarwinCoreMetadataGenerator.class.getResourceAsStream(pathToVocab), iriForVocab,
				rdfFormat);
		Predicate<Resource> iriPredicate = r -> {
			return r instanceof IRI;
		};
		Function<Resource, IRI> iriMap = r -> (IRI) r;
		Predicate<IRI> iriInNamespace = iri -> iri.getNamespace().equals(iriForVocab);
		Set<IRI> dwcIRIs = model.subjects().stream().filter(iriPredicate).map(iriMap).filter(iriInNamespace)
				.collect(Collectors.toSet());
		dwcIRIs.stream().forEach(i -> vocabMap.get(iriForVocab).get(i.getLocalName()).add(i));
	}

	/**
	 * Populate field names for a core or extension from the given file
	 * 
	 * @param inputPath
	 *            The path to the CSV file that contains the field names as
	 *            headers
	 * @param vocabMap
	 *            The map containing the vocabularies that will be used to map
	 *            the field names to IRIs.
	 * @param coreOrExtension
	 *            The core or extension representing the given file which we
	 *            need to add the fields to.
	 * @throws IOException
	 *             If there was an IO exception accessing the file
	 */
	public static void populateFields(final Path inputPath, Map<String, Map<String, List<IRI>>> vocabMap,
			DarwinCoreCoreOrExtension coreOrExtension) throws IOException {
		List<String> headers = new ArrayList<>();
		try (Reader inputStreamReader = Files.newBufferedReader(inputPath);) {
			CSVStream.parse(inputStreamReader, h -> headers.addAll(h), (h, l) -> l, l -> {
			});
		}

		for (int i = 0; i < headers.size(); i++) {
			String nextHeader = headers.get(i);
			DarwinCoreField nextField = new DarwinCoreField();
			// Check if the field maps to DWC
			// If it is DWC, give it that vocabulary
			nextField.setIndex(i);
			String vocabulary = null;
			IRI fullIRI = null;
			for (String nextVocabulary : vocabMap.keySet()) {
				for (Entry<String, List<IRI>> nextLocalNameIRI : vocabMap.get(nextVocabulary).entrySet()) {
					if (nextLocalNameIRI.getKey().equals(nextHeader)) {
						vocabulary = nextVocabulary;
						fullIRI = nextLocalNameIRI.getValue().iterator().next();
						break;
					}
				}
				if (vocabulary != null) {
					break;
				}
			}
			if (vocabulary != null) {
				nextField.setVocabulary(vocabulary);
			}
			if (fullIRI != null) {
				nextField.setTerm(fullIRI.toString());
			}
			// Else add it without vocabulary
			else {
				nextField.setTerm(nextHeader);
			}
			coreOrExtension.addField(nextField);
		}
	}
}

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
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.github.ansell.csv.stream.CSVStream;
import com.github.ansell.csv.stream.CSVStreamException;
import com.github.ansell.csv.sum.CSVSummariser;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.github.ansell.csv.sort.CSVSorter;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Merges two <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core
 * Archives</a> into a single resulting archive.
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
		final OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class).required()
				.describedAs("The base input Darwin Core Archive file to be merged.");
		final OptionSpec<File> otherInput = parser.accepts("other-input").withRequiredArg().ofType(File.class)
				.required().describedAs("The other input Darwin Core Archive file to be merged.");
		final OptionSpec<File> output = parser.accepts("output").withRequiredArg().ofType(File.class).required()
				.describedAs("A directory to output summary and other files to.");
		final OptionSpec<Boolean> includeDefaultsOption = parser.accepts("include-defaults").withRequiredArg()
				.ofType(Boolean.class).defaultsTo(Boolean.TRUE)
				.describedAs("Whether to include default values from the meta.xml file in each archive when merging.");
		final OptionSpec<Boolean> debugOption = parser.accepts("debug").withRequiredArg().ofType(Boolean.class)
				.defaultsTo(Boolean.FALSE).describedAs("Set to true to debug.");
		final OptionSpec<Boolean> filterNonVocabularyTermsOption = parser.accepts("remove-non-vocabulary-terms")
				.withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE)
				.describedAs("Remove terms that do not match vocabularies when merging.");

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

		final boolean filterNonVocabularyTerms = filterNonVocabularyTermsOption.value(options);

		final boolean includeDefaults = includeDefaultsOption.value(options);

		final Path inputPath = input.value(options).toPath();
		if (!Files.exists(inputPath)) {
			throw new FileNotFoundException(
					"Could not find input Darwin Core Archive file or metadata file: " + inputPath.toString());
		}

		final Path otherInputPath = otherInput.value(options).toPath();
		if (!Files.exists(otherInputPath)) {
			throw new FileNotFoundException("Could not find other input Darwin Core Archive file or metadata file: "
					+ otherInputPath.toString());
		}

		final Path outputDirPath = output.value(options).toPath();
		if (!Files.exists(outputDirPath)) {
			throw new FileNotFoundException("Could not find output folder: " + outputDirPath.toString());
		}

		final Path tempDir = Files.createTempDirectory("dwca-merge-");

		try {

			final Path outputArchivePath = outputDirPath.resolve("first-archive");
			final Path inputMetadataPath = openArchive(inputPath, outputArchivePath);
			Files.createDirectories(outputArchivePath);
			final DarwinCoreArchiveDocument inputArchiveDocument = loadArchive(debug, outputArchivePath,
					inputMetadataPath, includeDefaults);
			if (debug) {
				System.out.println("Found an archive with " + inputArchiveDocument.getCore().getFields().size()
						+ " core fields and " + inputArchiveDocument.getExtensions().size() + " extensions");
			}

			final Path otherOutputArchivePath = outputDirPath.resolve("other-archive");
			final Path otherInputMetadataPath = openArchive(otherInputPath, otherOutputArchivePath);
			Files.createDirectories(otherOutputArchivePath);
			final DarwinCoreArchiveDocument otherInputArchiveDocument = loadArchive(debug, otherOutputArchivePath,
					otherInputMetadataPath, includeDefaults);
			if (debug) {
				System.out.println("Found another archive with "
						+ otherInputArchiveDocument.getCore().getFields().size() + " core fields and "
						+ otherInputArchiveDocument.getExtensions().size() + " extensions");
			}

			canArchivesBeMergedDirectly(inputArchiveDocument, otherInputArchiveDocument);

			// This is the list of fields that will be in the final document,
			// the indexes represent the final document indexes, not the indexes
			// in the original fields
			final Path mergedOutputArchivePath = outputDirPath.resolve("merged-archive").normalize().toAbsolutePath();
			final Path mergedOutputMetadataPath = mergedOutputArchivePath.resolve(DarwinCoreArchiveChecker.META_XML);
			Files.createDirectories(mergedOutputArchivePath);
			final DarwinCoreArchiveDocument mergedArchiveDocument = mergeFieldSets(inputArchiveDocument,
					otherInputArchiveDocument, filterNonVocabularyTerms, debug);
			DarwinCoreFile mergedOutputCoreDarwinCoreFile = new DarwinCoreFile();
			mergedArchiveDocument.getCore().setFiles(mergedOutputCoreDarwinCoreFile);
			Path mergedOutputCorePath = mergedOutputArchivePath
					.resolve(inputArchiveDocument.getCore().getFiles().getLocations().get(0)).normalize()
					.toAbsolutePath();
			// Second stage to rename the file with "Merged-" in front
			// Does not work if done above as the normalize needs to occur first
			// to ensure we get the final path segment renamed
			mergedOutputCorePath = mergedOutputCorePath
					.resolveSibling("Merged-" + mergedOutputCorePath.getFileName().toString()).toAbsolutePath();
			// Ensure that if there are new subdirectories in the merged path
			// that we create them
			Files.createDirectories(mergedOutputCorePath.getParent());
			mergedOutputCoreDarwinCoreFile
					.addLocation(mergedOutputArchivePath.relativize(mergedOutputCorePath).toString());
			mergedArchiveDocument.setMetadataXMLPath(mergedOutputMetadataPath);
			mergedArchiveDocument.getCore().setIgnoreHeaderLines(1);
			try (final Writer mergedMetadataWriter = Files.newBufferedWriter(mergedOutputMetadataPath,
					StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);) {
				mergedArchiveDocument.toXML(mergedMetadataWriter, true);
			}

			int originalCoreIDField = Integer.parseInt(inputArchiveDocument.getCore().getIdOrCoreId());
			int otherOriginalCoreIDField = Integer.parseInt(otherInputArchiveDocument.getCore().getIdOrCoreId());
			int mergedCoreIDField = Integer.parseInt(mergedArchiveDocument.getCore().getIdOrCoreId());
			DarwinCoreField mergedCoreIndexField = null;
			for (DarwinCoreField nextMergedField : mergedArchiveDocument.getCore().getFields()) {
				if (nextMergedField.getIndex() == mergedCoreIDField) {
					mergedCoreIndexField = nextMergedField;
					break;
				}
			}
			if (mergedCoreIndexField == null) {
				throw new IllegalStateException(
						"Did not find the id field for the merged document using its index: " + mergedCoreIDField);
			}

			try (final Writer outputCoreWriter = Files.newBufferedWriter(mergedOutputCorePath, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE_NEW);
					final SequenceWriter outputCoreCsvWriter = CSVStream.newCSVWriter(outputCoreWriter,
							mergedArchiveDocument.getCore().getCsvSchema());) {
				outputCoreCsvWriter.write(mergedArchiveDocument.getCore().getFields().stream()
						.map(DarwinCoreField::getTerm).collect(Collectors.toList()));
			}
			try (final CloseableIterator<DarwinCoreRecord> inputIterator = inputArchiveDocument
					.iterator(false);
					final CloseableIterator<DarwinCoreRecord> otherInputIterator = otherInputArchiveDocument
							.iterator(false);
					final Writer outputCoreWriter = Files.newBufferedWriter(mergedOutputCorePath,
							StandardCharsets.UTF_8, StandardOpenOption.APPEND);
					final SequenceWriter outputCoreCsvWriter = CSVStream.newCSVWriter(outputCoreWriter,
							mergedArchiveDocument.getCore().getCsvSchema());) {
				DarwinCoreRecord nextOtherInputRecord = null;
				// Merge the two iterators before exhausting the other iterator
				// if it didn't match
				// Note, both iterators must represent commonly sorted sets in
				// terms of the id field values
				// The specific sort order does not matter as long as it is
				// common to both
				while (inputIterator.hasNext()) {
					DarwinCoreRecord nextInputRecord = inputIterator.next();
					// If we matched last time, we replace the "other" input
					// record with a new copy this time, otherwise leave it as
					// it is to be matched later
					if (nextOtherInputRecord == null) {
						if (otherInputIterator.hasNext()) {
							nextOtherInputRecord = otherInputIterator.next();
						}
					}

					List<String> nextMergedValues = getNewValuesList(mergedArchiveDocument.getCore(), includeDefaults);

					// Find the two key values to check if they are the same
					// before determining what to do next
					String nextInputKey = nextInputRecord.valueFor(mergedCoreIndexField.getTerm(), includeDefaults)
							.orElse(null);
					String nextOtherInputKey = null;

					if (nextInputKey == null) {
						throw new IllegalStateException("Did not find a value for the id field in the input record");
					}

					if (nextOtherInputRecord != null) {
						nextOtherInputKey = nextOtherInputRecord
								.valueFor(mergedCoreIndexField.getTerm(), includeDefaults).orElse(null);
						if (nextOtherInputKey == null) {
							throw new IllegalStateException(
									"Did not find a value for the id field in the other input record");
						}
					}

					if (nextInputKey.equals(nextOtherInputKey)) {
						// Found a match, merge the other record into this one!
						for (int i = 0; i < mergedArchiveDocument.getCore().getFields().size(); i++) {
							String nextMergedRecordTerm = mergedArchiveDocument.getCore().getFields().get(i).getTerm();
							String nextMergedValue = nextInputRecord.valueFor(nextMergedRecordTerm, includeDefaults)
									.orElse(null);
							// If the original record didn't have a value, check
							// the other record
							if (nextMergedValue == null || nextMergedValue.isEmpty()) {
								if (nextOtherInputRecord != null) {
									nextMergedValue = nextOtherInputRecord
											.valueFor(nextMergedRecordTerm, includeDefaults).orElse(null);
								}
							}
							if (nextMergedValue != null) {
								nextMergedValues.set(i, nextMergedValue);
							}
						}
					} else {
						// Else emit the nextInputRecord as the results for this
						for (int i = 0; i < mergedArchiveDocument.getCore().getFields().size(); i++) {
							String nextMergedRecordTerm = mergedArchiveDocument.getCore().getFields().get(i).getTerm();
							String nextMergedValue = nextInputRecord.valueFor(nextMergedRecordTerm, includeDefaults)
									.orElse(null);
							if (nextMergedValue != null) {
								nextMergedValues.set(i, nextMergedValue);
							}
						}
					}

					DarwinCoreRecordImpl nextMergedRecord = new DarwinCoreRecordImpl(mergedArchiveDocument,
							mergedArchiveDocument.getCore().getFields(), nextMergedValues);
					outputCoreCsvWriter.write(nextMergedValues);
				}
				// Emit an unmatched record from the loop above if applicable,
				// and then go through the rest of the other input iterator
				if (nextOtherInputRecord != null) {
					List<String> nextMergedValues = getNewValuesList(mergedArchiveDocument.getCore(), includeDefaults);
					for (int i = 0; i < mergedArchiveDocument.getCore().getFields().size(); i++) {
						String nextMergedRecordTerm = mergedArchiveDocument.getCore().getFields().get(i).getTerm();
						String nextMergedValue = nextOtherInputRecord.valueFor(nextMergedRecordTerm, includeDefaults)
								.orElse(null);
						if (nextMergedValue != null) {
							nextMergedValues.set(i, nextMergedValue);
						}
					}
					DarwinCoreRecordImpl nextMergedRecord = new DarwinCoreRecordImpl(mergedArchiveDocument,
							mergedArchiveDocument.getCore().getFields(), nextMergedValues);
					outputCoreCsvWriter.write(nextMergedValues);
				}
				// Deal with any records that were not matched during the loop
				// above by simply adding them to the result
				while (otherInputIterator.hasNext()) {
					nextOtherInputRecord = otherInputIterator.next();
					List<String> nextMergedValues = getNewValuesList(mergedArchiveDocument.getCore(), includeDefaults);
					for (int i = 0; i < mergedArchiveDocument.getCore().getFields().size(); i++) {
						String nextMergedRecordTerm = mergedArchiveDocument.getCore().getFields().get(i).getTerm();
						String nextMergedValue = nextOtherInputRecord.valueFor(nextMergedRecordTerm, includeDefaults)
								.orElse(null);
						if (nextMergedValue != null) {
							nextMergedValues.set(i, nextMergedValue);
						}
					}
					DarwinCoreRecordImpl nextMergedRecord = new DarwinCoreRecordImpl(mergedArchiveDocument,
							mergedArchiveDocument.getCore().getFields(), nextMergedValues);
					outputCoreCsvWriter.write(nextMergedValues);
				}
			}
			if (debug) {
				System.out.println("Merged output:");
				Files.readAllLines(mergedOutputCorePath, StandardCharsets.UTF_8).stream()
						.forEachOrdered(System.out::println);
				System.out.println("End of merged output");
			}
		} finally {
			FileUtils.deleteQuietly(tempDir.toFile());
		}
	}

	private static List<String> getNewValuesList(DarwinCoreCoreOrExtension core, boolean includeDefaults) {
		if (includeDefaults) {
			return new ArrayList<>(core.getDefaultValues());
		} else {
			List<String> nextMergedValues = new ArrayList<>(core.getFields().size());
			for (int initialSetup = 0; initialSetup < core.getFields().size(); initialSetup++) {
				// Setup all of the initial merged values to the empty
				// string
				nextMergedValues.add("");
			}
			return nextMergedValues;
		}
	}

	/**
	 * Merge the descriptions of two documents and create a description of a new
	 * merged document, where the field indexes in the new document reflect
	 * those in the merged document. <br>
	 * IMPORTANT:
	 * {@link #canArchivesBeMergedDirectly(DarwinCoreArchiveDocument, DarwinCoreArchiveDocument)}
	 * must be called without error before calling this method
	 * 
	 * @param inputArchiveDocument
	 *            The reference archive to merge.
	 * @param otherInputArchiveDocument
	 *            The archive to merge into the reference archive.
	 * @param filterNonVocabularyTerms
	 *            True to filter out non vocabulary terms or false to keep terms
	 *            even if they never matched vocabulary terms.
	 * @param debug
	 *            True to verbosely debug and false otherwise.
	 * @return A merged description of a document that has merged the field sets
	 *         from both documents.
	 */
	private static DarwinCoreArchiveDocument mergeFieldSets(DarwinCoreArchiveDocument inputArchiveDocument,
			DarwinCoreArchiveDocument otherInputArchiveDocument, boolean filterNonVocabularyTerms, boolean debug) {
		DarwinCoreArchiveDocument result = new DarwinCoreArchiveDocument();

		DarwinCoreCoreOrExtension resultCore = DarwinCoreCoreOrExtension.newCore();
		resultCore.setRowType(inputArchiveDocument.getCore().getRowType());
		resultCore.setDateFormat(inputArchiveDocument.getCore().getDateFormat());
		// First check the ID field, as it is common for it not to be in the
		// list of fields (who doesn't define the name for the id field?!?!,
		// Anyway, its common so have to deal with it), and we will need to add
		// it manually otherwise
		DarwinCoreField originalIDField = null;
		int inputCoreID = Integer.parseInt(inputArchiveDocument.getCore().getIdOrCoreId());
		for (DarwinCoreField nextField : inputArchiveDocument.getCore().getFields()) {
			if (nextField.getIndex() != null && nextField.getIndex().equals(inputCoreID)) {
				originalIDField = nextField;
				break;
			}
		}
		DarwinCoreField originalOtherIDField = null;
		int otherInputCoreID = Integer.parseInt(otherInputArchiveDocument.getCore().getIdOrCoreId());
		for (DarwinCoreField nextField : otherInputArchiveDocument.getCore().getFields()) {
			if (nextField.getIndex() != null && nextField.getIndex().equals(otherInputCoreID)) {
				originalOtherIDField = nextField;
				break;
			}
		}
		DarwinCoreField resultCoreField = new DarwinCoreField();
		// Always put the coreID field in index 0 in the result for everyones
		// sanity
		resultCoreField.setIndex(0);
		resultCore.setIdOrCoreId("0");
		if (originalIDField == null) {
			if (originalOtherIDField != null) {
				// If the other document had the term specified for its id
				// field, then use it instead
				resultCoreField.setTerm(originalOtherIDField.getTerm());
				resultCoreField.setVocabulary(originalOtherIDField.getVocabulary());
				resultCoreField.setDefault(originalOtherIDField.getDefault());
				resultCoreField.setDelimitedBy(originalOtherIDField.getDelimitedBy());
			} else {
				// Discourage people from using this bad practice by creating a
				// large field name....
				resultCoreField.setTerm("dwcaUtilsAutomaticallyAssignedCoreIDField");
			}
		} else {
			resultCoreField.setTerm(originalIDField.getTerm());
			resultCoreField.setVocabulary(originalIDField.getVocabulary());
			resultCoreField.setDefault(originalIDField.getDefault());
			resultCoreField.setDelimitedBy(originalIDField.getDelimitedBy());
		}
		// Even if they don't define the id field in the list, we do, its
		// smarter this way
		resultCore.addField(resultCoreField);

		// Go back through the list adding the other fields in order
		int nextResultCoreFieldIndex = 1;
		for (DarwinCoreField nextField : inputArchiveDocument.getCore().getFields()) {
			if (nextField.getIndex() != null && nextField.getIndex().equals(inputCoreID)) {
				// Skip the coreID field this time through
				continue;
			}

			if (filterNonVocabularyTerms && nextField.getVocabulary() == null) {
				// If the vocabulary is missing and they want to filter it out,
				// then ignore it at this point
				continue;
			}

			DarwinCoreField nextResultField = new DarwinCoreField();
			// Map the index to what would be in a merged result
			nextResultField.setIndex(nextResultCoreFieldIndex);
			nextResultField.setTerm(nextField.getTerm());
			nextResultField.setVocabulary(nextField.getVocabulary());
			nextResultField.setDefault(nextField.getDefault());
			nextResultField.setDelimitedBy(nextField.getDelimitedBy());
			resultCore.addField(nextResultField);

			nextResultCoreFieldIndex++;
		}

		// Go through the other input archive document adding fields to the
		// result core
		for (DarwinCoreField nextField : otherInputArchiveDocument.getCore().getFields()) {
			if (debug) {
				System.out.println("Merging other input field: " + nextField.toString());
			}
			if (nextField.getIndex() != null && nextField.getIndex().equals(otherInputCoreID)) {
				// Skip the other documents coreID field, which will be
				// represented in the original core ID for this field
				if (debug) {
					System.out.println("Skipping coreID field from other document as it is merged into original input");
				}
				continue;
			}

			if (nextField.getIndex() == null && nextField.getDefault() != null) {
				if (debug) {
					System.out.println("Found field with no index but has a default: " + nextField);
				}
			}

			if (filterNonVocabularyTerms && nextField.getVocabulary() == null) {
				// If the vocabulary is missing and they want to filter it out,
				// then ignore it at this point
				continue;
			}

			boolean alreadyInList = false;
			for (DarwinCoreField nextAssignedResultField : resultCore.getFields()) {
				if (nextAssignedResultField.getTerm().equals(nextField.getTerm())) {
					// Add in vocabulary/default/delimitedBy from the other
					// archive if it was missing in the reference
					if (nextAssignedResultField.getVocabulary() == null && nextField.getVocabulary() != null) {
						nextAssignedResultField.setVocabulary(nextField.getVocabulary());
					}
					if (nextAssignedResultField.getDefault() == null && nextField.getDefault() != null) {
						nextAssignedResultField.setDefault(nextField.getDefault());
					}
					if (nextAssignedResultField.getDelimitedBy() == null && nextField.getDelimitedBy() != null) {
						nextAssignedResultField.setDelimitedBy(nextField.getDelimitedBy());
					}
					alreadyInList = true;
					break;
				}
			}
			if (!alreadyInList) {
				if (debug) {
					System.out.println("Found a field not in the list already: " + nextField);
				}
				DarwinCoreField nextResultField = new DarwinCoreField();
				// Map the index to what would be in a merged result
				nextResultField.setIndex(nextResultCoreFieldIndex);
				nextResultField.setTerm(nextField.getTerm());
				nextResultField.setVocabulary(nextField.getVocabulary());
				nextResultField.setDefault(nextField.getDefault());
				nextResultField.setDelimitedBy(nextField.getDelimitedBy());
				resultCore.addField(nextResultField);

				nextResultCoreFieldIndex++;
			}
		}

		if (nextResultCoreFieldIndex != resultCore.getFields().size()) {
			throw new IllegalStateException(
					"Result core does not contain the expected number of merged fields, expected: "
							+ nextResultCoreFieldIndex + ", found: " + resultCore.getFields().size());
		}

		result.setCore(resultCore);

		return result;
	}

	/**
	 * Check to ensure that we are only allowing trivial merges that won't cause
	 * data loss or other unexpected effects. In future this method may be
	 * trimmed down when other features are added.
	 * 
	 * @param inputArchiveDocument
	 *            The first input document
	 * @param otherInputArchiveDocument
	 *            The other input document
	 */
	private static void canArchivesBeMergedDirectly(DarwinCoreArchiveDocument inputArchiveDocument,
			DarwinCoreArchiveDocument otherInputArchiveDocument) {
		System.out.println("coreId: input=" + inputArchiveDocument.getCore().getIdOrCoreId() + " other="
				+ otherInputArchiveDocument.getCore().getIdOrCoreId());
		if (inputArchiveDocument.getCore().getIdOrCoreId() == null
				|| otherInputArchiveDocument.getCore().getIdOrCoreId() == null) {
			throw new IllegalStateException("Both archives need to have the core id field defined to be merged");
		}
		try {
			int inputCoreID = Integer.parseInt(inputArchiveDocument.getCore().getIdOrCoreId());
			if (inputCoreID < 0) {
				throw new IllegalStateException(
						"Core id must be a non-negative integer: " + inputArchiveDocument.getCore().getIdOrCoreId());
			}
			int otherInputCoreID = Integer.parseInt(otherInputArchiveDocument.getCore().getIdOrCoreId());
			if (otherInputCoreID < 0) {
				throw new IllegalStateException(
						"Core id must be a non-negative integer: " + inputArchiveDocument.getCore().getIdOrCoreId());
			}

			DarwinCoreField inputCoreIDField = inputArchiveDocument.getCore().findField(inputCoreID)
					.orElseThrow(() -> new IllegalStateException("The core ID did not match a field index"));

			DarwinCoreField otherInputCoreIDField = otherInputArchiveDocument.getCore().findField(otherInputCoreID)
					.orElseThrow(() -> new IllegalStateException("The core ID did not match a field index"));

			if (!inputCoreIDField.getTerm().equals(otherInputCoreIDField.getTerm())) {
				throw new IllegalStateException("Core id field terms must match for archives to be merged: "
						+ inputCoreIDField + " " + otherInputCoreIDField);
			}

			if (!inputArchiveDocument.getCore().getRowType().equals(otherInputArchiveDocument.getCore().getRowType())) {
				throw new IllegalStateException("Can only merge archives where the core row types are equal: "
						+ inputArchiveDocument.getCore().getRowType() + " "
						+ otherInputArchiveDocument.getCore().getRowType());
			}

			if (!inputArchiveDocument.getCore().getEncoding()
					.equals(otherInputArchiveDocument.getCore().getEncoding())) {
				throw new IllegalStateException("Can only merge archives where the same encoding is used: "
						+ inputArchiveDocument.getCore().getEncoding() + " "
						+ otherInputArchiveDocument.getCore().getEncoding());
			}

			// TODO: Check that the default values do not conflict, and fail
			// early if they do

		} catch (NumberFormatException e) {
			throw new IllegalStateException("Core id must be an integer", e);
		}
	}

	private static DarwinCoreArchiveDocument loadArchive(final boolean debug, final Path outputDirPath,
			final Path inputMetadataPath, final boolean includeDefaults)
			throws IOException, SAXException, IllegalStateException, CSVStreamException {
		DarwinCoreArchiveDocument inputArchiveDocument = DarwinCoreArchiveChecker.parseMetadataXml(inputMetadataPath);
		if (debug) {
			System.out.println(inputArchiveDocument.toString());
		}

		DarwinCoreCoreOrExtension core = inputArchiveDocument.getCore();
		DarwinCoreArchiveChecker.checkCoreOrExtension(core, inputMetadataPath, outputDirPath, true, debug,
				includeDefaults);
		for (DarwinCoreCoreOrExtension extension : inputArchiveDocument.getExtensions()) {
			DarwinCoreArchiveChecker.checkCoreOrExtension(extension, inputMetadataPath, outputDirPath, true, debug,
					includeDefaults);
		}
		return inputArchiveDocument;
	}

	private static Path openArchive(final Path inputPath, final Path tempDir)
			throws IOException, IllegalStateException {
		final Path inputMetadataPath;
		if (inputPath.getFileName().toString().contains(".zip")) {
			inputMetadataPath = DarwinCoreArchiveChecker.checkZip(inputPath, tempDir);
			if (inputMetadataPath == null) {
				throw new IllegalStateException(
						"Did not find a metadata file in the input ZIP file: " + inputPath.toAbsolutePath().toString());
			}
		} else {
			inputMetadataPath = DarwinCoreArchiveChecker.checkFolder(inputPath);
		}
		return inputMetadataPath;
	}

	public static void checkCoreOrExtension(DarwinCoreCoreOrExtension coreOrExtension, final Path metadataPath,
			final Path outputDirPath, final boolean hasOutput, final boolean debug, final boolean includeDefaults)
			throws IOException, CSVStreamException {
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
						outputDirPath.resolve("Statistics-" + coreOrExtensionFilePath.getFileName().toString()),
						coreOrExtension.getEncoding());
						Writer mappingWriter = Files.newBufferedWriter(
								outputDirPath.resolve("Mapping-" + coreOrExtensionFilePath.getFileName().toString()),
								coreOrExtension.getEncoding());) {
					// Summarise the core document
					CSVSummariser.runSummarise(inputReader, CSVStream.defaultMapper(), coreOrExtension.getCsvSchema(),
							summaryWriter, mappingWriter, 20, true, debug, coreOrExtensionFields,
							includeDefaults ? coreOrExtension.getDefaultValues() : Collections.emptyList(),
							headerLineCount);
				}
			} else {
				CSVStream.parse(inputReader, h -> {
				}, (h, l) -> l, l -> {
				}, coreOrExtensionFields, headerLineCount, CSVStream.defaultMapper(), coreOrExtension.getCsvSchema());
			}
		}
	}

}

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
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * 
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveChecker {

	/**
	 * Private constructor for static only class
	 */
	private DarwinCoreArchiveChecker() {
	}

	public static void main(String... args) throws Exception {
		final OptionParser parser = new OptionParser();

		final OptionSpec<Void> help = parser.accepts("help").forHelp();
		final OptionSpec<File> input = parser.accepts("input").withRequiredArg().ofType(File.class).required()
				.describedAs("The input Darwin Core Archive file to be checked.");

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
			throw new FileNotFoundException("Could not find input Darwin Core Archive file: " + inputPath.toString());
		}

		if (!inputPath.getFileName().endsWith(".zip")) {
			throw new RuntimeException("Only working on validating .zip files right now: " + inputPath);
		}

		checkZip(inputPath);
	}

	private static void checkZip(Path inputPath) throws IOException {
		Path tempDir = Files.createTempDirectory("dwca-check-");

		Path metadataPath = tempDir.resolve("metadata.xml");

		final FileSystemManager fsManager = VFS.getManager();
		final FileObject zipFile = fsManager.resolveFile("zip:" + inputPath.toAbsolutePath().toString());

		final FileObject[] children = zipFile.getChildren();
		if (children.length == 0) {
			throw new RuntimeException("No files in zip file: " + inputPath);
		}

		for (FileObject nextFile : children) {
			try (InputStream in = nextFile.getContent().getInputStream();) {
				Path nextTempFile = tempDir.resolve(nextFile.getName().toString());
				if (nextFile.getName().toString().equalsIgnoreCase("metadata.xml")) {
					if (metadataPath != null) {
						throw new RuntimeException("Duplicate metadata.xml files found: original=" + metadataPath
								+ " duplicate=" + nextFile.getName().toString());
					}
					metadataPath = nextTempFile;
				}

				Files.copy(in, nextTempFile);
			}
		}

		parseMetadataXml(metadataPath);
	}

	private static void parseMetadataXml(Path metadataPath) {
		throw new UnsupportedOperationException("TODO: Implement parseMetadataXml!");
	}

}

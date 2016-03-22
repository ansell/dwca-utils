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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A non-threadsafe SAX based parser for Darwin Core Metadata XML files
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class DarwinCoreMetadataSaxParser extends DefaultHandler {

	private final XMLReader xmlReader;

	private final StringBuilder buffer = new StringBuilder(1024);

	private boolean startArchiveFound;

	private boolean foundCore;

	private boolean inCore;

	private boolean inExtension;

	private boolean inFiles;

	private boolean inLocation;

	private boolean foundLocationInFile;

	private boolean endArchiveFound;

	private DarwinCoreFile currentFile;

	private DarwinCoreArchiveDocument dwcaDocument;

	private DarwinCoreCoreOrExtension currentCoreOrExtension;

	private DarwinCoreMetadataSaxParser() throws SAXException {
		this(XMLReaderFactory.createXMLReader());
	}

	private DarwinCoreMetadataSaxParser(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	/**
	 * Parse a Darwin Core Archive metadata XML document using the default
	 * {@link XMLReader} implementation provided by
	 * {@link XMLReaderFactory#createXMLReader()}.
	 * 
	 * @param reader
	 *            The document to parse.
	 * @return An instance of {@link DarwinCoreArchiveDocument} containing the
	 *         parsed details.
	 * @throws IOException
	 *             If there is an input-output exception.
	 * @throws SAXException
	 *             If there is an exception parsing the XML document.
	 * @throws IllegalStateException
	 *             If there is an exception interpreting the context of parts of
	 *             the document that violate the state assumptions in the
	 *             specification.
	 */
	public static DarwinCoreArchiveDocument parse(Reader reader) throws IOException, SAXException {
		return new DarwinCoreMetadataSaxParser().parseInternal(reader);
	}

	/**
	 * Parse a Darwin Core Archive metadata XML document using a specific
	 * {@link XMLReader} implementation.
	 * 
	 * @param reader
	 *            The document to parse.
	 * @param xmlReaderToUse
	 *            Specify a particular implementation of {@link XMLReader} to
	 *            use to parse this document.
	 * @return An instance of {@link DarwinCoreArchiveDocument} containing the
	 *         parsed details.
	 * @throws IOException
	 *             If there is an input-output exception.
	 * @throws SAXException
	 *             If there is an exception parsing the XML document.
	 * @throws IllegalStateException
	 *             If there is an exception interpreting the context of parts of
	 *             the document that violate the state assumptions in the
	 *             specification.
	 */
	public static DarwinCoreArchiveDocument parse(Reader reader, XMLReader xmlReaderToUse)
			throws IOException, SAXException, IllegalStateException {
		return new DarwinCoreMetadataSaxParser(xmlReaderToUse).parseInternal(reader);
	}

	private DarwinCoreArchiveDocument parseInternal(Reader reader) throws IOException, SAXException {
		return parseInternal(new InputSource(reader));
	}

	private DarwinCoreArchiveDocument parseInternal(InputSource inputSource) throws IOException, SAXException {
		reset();
		xmlReader.setContentHandler(this);
		xmlReader.parse(inputSource);
		DarwinCoreArchiveDocument result = this.dwcaDocument;
		reset();
		return result;
	}

	private void reset() {
		buffer.setLength(0);
		startArchiveFound = false;
		foundCore = false;
		inCore = false;
		inExtension = false;
		inFiles = false;
		inLocation = false;
		foundLocationInFile = false;
		endArchiveFound = false;
		currentFile = null;
		dwcaDocument = null;
	}

	@Override
	public void startDocument() throws SAXException {
		System.out.println("SAX: startDocument");
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("SAX: endDocument");
		if (!(startArchiveFound && endArchiveFound)) {
			throw new SAXException("Did not find a full archive element");
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		System.out.println("SAX: startPrefixMapping: " + prefix + " " + uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		System.out.println("SAX: endPrefixMapping: " + prefix);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		System.out.println("SAX: startElement: " + uri + " " + localName + " " + qName);
		buffer.setLength(0);

		if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.ARCHIVE.equals(localName)) {
			if (startArchiveFound) {
				throw new SAXException("Only a single archive element is allowed.");
			}
			startArchiveFound = true;
			dwcaDocument = new DarwinCoreArchiveDocument();
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.CORE.equals(localName)) {
			if (!startArchiveFound) {
				throw new SAXException("Did not find an archive element before the core element.");
			}
			if (inExtension) {
				throw new SAXException("Found core element inside of an extension element.");
			}
			if (inFiles) {
				throw new SAXException("Found core element nested in files element.");
			}
			if (inLocation) {
				throw new SAXException("Found core element nested in location element.");
			}
			currentCoreOrExtension = DarwinCoreCoreOrExtension.newCore(attributes);
			dwcaDocument.setCore(currentCoreOrExtension);
			inCore = true;
			foundCore = true;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.EXTENSION.equals(localName)) {
			if (!startArchiveFound) {
				throw new SAXException("Did not find an archive element before the extension element.");
			}
			if (inCore) {
				throw new SAXException("Found extension element inside of a core element.");
			}
			if (inFiles) {
				throw new SAXException("Found extension element nested in files element.");
			}
			if (inLocation) {
				throw new SAXException("Found extension element nested in location element.");
			}
			currentCoreOrExtension = DarwinCoreCoreOrExtension.newExtension(attributes);
			dwcaDocument.addExtension(currentCoreOrExtension);
			inExtension = true;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.FILES.equals(localName)) {
			if (!startArchiveFound) {
				throw new SAXException("Did not find an archive element before the files element.");
			}
			if (!(inCore || inExtension)) {
				throw new SAXException("Found files element outside of core or extension elements.");
			}
			if (inLocation) {
				throw new SAXException("Found files element inside of location element.");
			}
			currentFile = new DarwinCoreFile();
			inFiles = true;
			foundLocationInFile = false;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.LOCATION.equals(localName)) {
			if (!startArchiveFound) {
				throw new SAXException("Did not find an archive element before the location element.");
			}
			if (!(inCore || inExtension)) {
				throw new SAXException("Found location element outside of core or extension elements.");
			}
			if (!inFiles) {
				throw new SAXException("Found location element outside of files elements.");
			}
			inLocation = true;
			foundLocationInFile = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		System.out.println("SAX: endElement: " + uri + " " + localName + " " + qName);
		System.out.println(buffer.toString());

		if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.ARCHIVE.equals(localName)) {
			if (!startArchiveFound) {
				throw new SAXException("Found closing archive tag without a starting archive tag.");
			}
			endArchiveFound = true;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.CORE.equals(localName)) {
			if (!(foundCore && inCore)) {
				throw new SAXException("Found end tag for core without an opening core tag.");
			}
			inCore = false;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.EXTENSION.equals(localName)) {
			if (!inExtension) {
				throw new SAXException("Found end tag for extension without an opening extension tag.");
			}
			inExtension = false;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.FILES.equals(localName)) {
			if (!inFiles) {
				throw new SAXException("Found end tag for files without an opening files tag.");
			}
			if (currentFile.getLocations().isEmpty()) {
				throw new SAXException("Did not find locations for file in "
						+ currentCoreOrExtension.getType().toString().toLowerCase());
			}
			if (inCore || inExtension) {
				currentCoreOrExtension.setFiles(currentFile);
			}
			inFiles = false;
			foundLocationInFile = false;
			currentFile = null;
		} else if (DarwinCoreArchiveVocab.DWC.equals(uri) && DarwinCoreArchiveVocab.LOCATION.equals(localName)) {
			if (!(foundLocationInFile && inLocation)) {
				throw new SAXException("Found end tag for location without an opening location tag.");
			}
			currentFile.addLocation(buffer.toString());
			inLocation = false;
		}

		buffer.setLength(0);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		buffer.append(ch, start, length);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		System.out.println("SAX: warning: " + e.getMessage());
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		System.out.println("SAX: error: " + e.getMessage());
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		System.out.println("SAX: fatalError: " + e.getMessage());
		throw e;
	}

}

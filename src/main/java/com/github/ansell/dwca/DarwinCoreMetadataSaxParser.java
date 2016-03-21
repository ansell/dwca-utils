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

import org.apache.http.annotation.NotThreadSafe;
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
@NotThreadSafe
public class DarwinCoreMetadataSaxParser extends DefaultHandler {

	private final XMLReader xmlReader;

	private final StringBuilder buffer = new StringBuilder(1024);

	public DarwinCoreMetadataSaxParser() throws SAXException {
		this(XMLReaderFactory.createXMLReader());
	}

	public DarwinCoreMetadataSaxParser(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	public void parse(Reader reader) throws IOException, SAXException {
		parse(new InputSource(reader));
	}

	public void parse(InputSource inputSource) throws IOException, SAXException {
		xmlReader.setContentHandler(this);
		xmlReader.parse(inputSource);
	}

	@Override
	public void startDocument() throws SAXException {
		System.out.println("SAX: startDocument");
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("SAX: startDocument");
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
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		System.out.println("SAX: endElement: " + uri + " " + localName + " " + qName);
		System.out.println(buffer.toString());
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

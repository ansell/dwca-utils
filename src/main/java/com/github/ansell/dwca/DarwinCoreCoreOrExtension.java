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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;

/**
 * Represents either a core or an extension element in a Darwin Core Archive XML
 * file.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public class DarwinCoreCoreOrExtension {

	public static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

	public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

	public static final int DEFAULT_IGNORE_HEADER_LINES = 0;

	public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

	public static final String DEFAULT_FIELDS_ENCLOSED_BY = "\"";

	public static final String DEFAULT_LINES_TERMINATED_BY = "\n";

	public static final String DEFAULT_FIELDS_TERMINATED_BY = ",";

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DarwinCoreCoreOrExtension [");
		if (idOrCoreId != null) {
			builder.append("idOrCoreId=");
			builder.append(idOrCoreId);
			builder.append(", ");
		}
		if (rowType != null) {
			builder.append("rowType=");
			builder.append(rowType);
			builder.append(", ");
		}
		if (fieldsTerminatedBy != null) {
			builder.append("fieldsTerminatedBy=");
			builder.append(fieldsTerminatedBy.replace("\t", "\\t").replace("\b", "\\b"));
			builder.append(", ");
		}
		if (linesTerminatedBy != null) {
			builder.append("linesTerminatedBy=");
			builder.append(linesTerminatedBy.replace("\r", "\\r").replace(DEFAULT_LINES_TERMINATED_BY, "\\n"));
			builder.append(", ");
		}
		if (fieldsEnclosedBy != null) {
			builder.append("fieldsEnclosedBy=");
			builder.append(fieldsEnclosedBy);
			builder.append(", ");
		}
		if (encoding != null) {
			builder.append("encoding=");
			builder.append(encoding);
			builder.append(", ");
		}
		builder.append("ignoreHeaderLines=");
		builder.append(ignoreHeaderLines);
		builder.append(", ");
		if (dateFormatPattern != null) {
			builder.append("dateFormatPattern=");
			builder.append(dateFormatPattern);
			builder.append(", ");
		}
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if (files != null) {
			builder.append("files=");
			builder.append(files);
			builder.append(", ");
		}
		if (fields != null) {
			builder.append("fields=");
			builder.append(fields.subList(0, Math.min(fields.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

	public enum CoreOrExtension {
		CORE,

		EXTENSION
	}

	/**
	 * This is only required if extensions are used.
	 */
	private String idOrCoreId;

	/**
	 * Spec says that this is required, but also gives a default, so leaving it
	 * empty by default to check for its existence.
	 */
	private String rowType;

	private String fieldsTerminatedBy = DEFAULT_FIELDS_TERMINATED_BY;

	private String linesTerminatedBy = DEFAULT_LINES_TERMINATED_BY;

	private String fieldsEnclosedBy = DEFAULT_FIELDS_ENCLOSED_BY;

	private Charset encoding = DEFAULT_ENCODING;

	private int ignoreHeaderLines = DEFAULT_IGNORE_HEADER_LINES;

	private DateTimeFormatter dateFormatter = DEFAULT_DATE_FORMAT;

	private String dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN;

	private final CoreOrExtension type;

	private DarwinCoreFile files;

	private final List<DarwinCoreField> fields = new ArrayList<>();

	private DarwinCoreCoreOrExtension(CoreOrExtension type) {
		this.type = type;
	}

	private DarwinCoreCoreOrExtension(CoreOrExtension type, Attributes attributes) {
		this(type);
		for (int i = 0; i < attributes.getLength(); i++) {
			String namespace = attributes.getURI(i);
			String localName = attributes.getLocalName(i);
			if (DarwinCoreArchiveVocab.ROW_TYPE.equals(localName)) {
				this.setRowType(attributes.getValue(i));
			} else if (DarwinCoreArchiveVocab.FIELDS_TERMINATED_BY.equals(localName)) {
				this.setFieldsTerminatedBy(attributes.getValue(i));
			} else if (DarwinCoreArchiveVocab.LINES_TERMINATED_BY.equals(localName)) {
				this.setLinesTerminatedBy(attributes.getValue(i));
			} else if (DarwinCoreArchiveVocab.FIELDS_ENCLOSED_BY.equals(localName)) {
				this.setFieldsEnclosedBy(attributes.getValue(i));
			} else if (DarwinCoreArchiveVocab.ENCODING.equals(localName)) {
				this.setEncoding(Charset.forName(attributes.getValue(i)));
			} else if (DarwinCoreArchiveVocab.IGNORE_HEADER_LINES.equals(localName)) {
				this.setIgnoreHeaderLines(Integer.parseInt(attributes.getValue(i)));
			} else if (DarwinCoreArchiveVocab.DATE_FORMAT.equals(localName)) {
				// Need to change capital D and Y from spec into lower-case
				// d and y for DateTimeFormatter
				String nextValue = attributes.getValue(i).replaceAll("D", "d").replaceAll("Y", "y");
				this.setDateFormat(nextValue);
			} else {
				System.out.println("Found unrecognised Darwin Core attribute for " + this.type.toString().toLowerCase()
						+ " : " + localName);
			}
		}
	}

	public static DarwinCoreCoreOrExtension newCore() {
		return new DarwinCoreCoreOrExtension(CoreOrExtension.CORE);
	}

	public static DarwinCoreCoreOrExtension newExtension() {
		return new DarwinCoreCoreOrExtension(CoreOrExtension.EXTENSION);
	}

	public static DarwinCoreCoreOrExtension newCore(Attributes attributes) {
		return new DarwinCoreCoreOrExtension(CoreOrExtension.CORE, attributes);
	}

	public static DarwinCoreCoreOrExtension newExtension(Attributes attributes) {
		return new DarwinCoreCoreOrExtension(CoreOrExtension.EXTENSION, attributes);
	}

	public String getIdOrCoreId() {
		if (this.idOrCoreId == null && this.type == CoreOrExtension.EXTENSION) {
			throw new IllegalStateException("Extensions must have coreId value set.");
		}
		return idOrCoreId;
	}

	public void setIdOrCoreId(String idOrCoreId) {
		if (this.idOrCoreId != null && !this.idOrCoreId.equals(idOrCoreId)) {
			throw new IllegalStateException("Multiple values found for id/coreId");
		}
		this.idOrCoreId = idOrCoreId;
	}

	public String getRowType() {
		if (this.rowType == null) {
			throw new IllegalStateException("Did not find value for row type that was required");
		}
		return rowType;
	}

	public void setRowType(String rowType) {
		if (this.rowType != null && !this.rowType.equals(rowType)) {
			throw new IllegalStateException("Multiple values found for row type");
		}
		this.rowType = rowType;
	}

	public String getFieldsTerminatedBy() {
		return fieldsTerminatedBy;
	}

	public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
		this.fieldsTerminatedBy = fieldsTerminatedBy.replace("\\t", "\t").replace("\\b", "\b");
	}

	public String getLinesTerminatedBy() {
		return linesTerminatedBy;
	}

	public void setLinesTerminatedBy(String linesTerminatedBy) {
		this.linesTerminatedBy = linesTerminatedBy.replace("\\n", DEFAULT_LINES_TERMINATED_BY).replace("\\r", "\r");
	}

	public String getFieldsEnclosedBy() {
		return fieldsEnclosedBy;
	}

	public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
		this.fieldsEnclosedBy = fieldsEnclosedBy;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public int getIgnoreHeaderLines() {
		return ignoreHeaderLines;
	}

	public void setIgnoreHeaderLines(int ignoreHeaderLines) {
		this.ignoreHeaderLines = ignoreHeaderLines;
	}

	public DateTimeFormatter getDateFormatter() {
		return dateFormatter;
	}

	public String getDateFormat() {
		return this.dateFormatPattern;
	}

	public void setDateFormat(String nextDateFormatPattern) {
		this.dateFormatPattern = nextDateFormatPattern;
		this.dateFormatter = DateTimeFormatter.ofPattern(nextDateFormatPattern);
	}

	public CoreOrExtension getType() {
		return type;
	}

	public DarwinCoreFile getFiles() {
		if (this.files == null) {
			throw new IllegalStateException("Did not find value for files that was required");
		}
		return files;
	}

	public void setFiles(DarwinCoreFile files) {
		if (this.files != null) {
			throw new IllegalStateException("Only a single files is allowed for each core or extension.");
		}
		this.files = files;
	}

	public List<DarwinCoreField> getFields() {
		if (this.fields.isEmpty()) {
			throw new IllegalStateException("No fields present");
		}
		return Collections.unmodifiableList(this.fields);
	}

	public void addField(DarwinCoreField nextField) {
		this.fields.add(nextField);
	}

	public void toXML(XMLStreamWriter writer) throws XMLStreamException, IOException {
		if (this.type == CoreOrExtension.CORE) {
			writer.writeStartElement(DarwinCoreArchiveVocab.CORE);
		} else if (this.type == CoreOrExtension.EXTENSION) {
			writer.writeStartElement(DarwinCoreArchiveVocab.EXTENSION);
		}
		writer.writeAttribute(DarwinCoreArchiveVocab.ROW_TYPE, this.getRowType());
		if (this.getFieldsTerminatedBy() != DEFAULT_FIELDS_TERMINATED_BY) {
			writer.writeAttribute(DarwinCoreArchiveVocab.FIELDS_TERMINATED_BY, this.getFieldsTerminatedBy());
		}
		if (this.getLinesTerminatedBy() != DEFAULT_LINES_TERMINATED_BY) {
			writer.writeAttribute(DarwinCoreArchiveVocab.LINES_TERMINATED_BY,
					this.getLinesTerminatedBy().replace("\r", "\\r").replace("\n", "\\n"));
		}
		if (this.getFieldsEnclosedBy() != DEFAULT_FIELDS_ENCLOSED_BY) {
			writer.writeAttribute(DarwinCoreArchiveVocab.FIELDS_ENCLOSED_BY, this.getFieldsEnclosedBy());
		}
		if (this.getEncoding() != DEFAULT_ENCODING) {
			writer.writeAttribute(DarwinCoreArchiveVocab.ENCODING, this.getEncoding().displayName(Locale.ENGLISH));
		}
		if (this.getIgnoreHeaderLines() != DEFAULT_IGNORE_HEADER_LINES) {
			writer.writeAttribute(DarwinCoreArchiveVocab.IGNORE_HEADER_LINES,
					Integer.toString(this.getIgnoreHeaderLines()));
		}
		if (this.getDateFormat() != DEFAULT_DATE_FORMAT_PATTERN) {
			writer.writeAttribute(DarwinCoreArchiveVocab.DATE_FORMAT,
					this.getDateFormat().replace("d", "D").replace("y", "Y"));
		}
		writer.writeStartElement(DarwinCoreArchiveVocab.FILES);
		for (String nextLocation : this.getFiles().getLocations()) {
			writer.writeStartElement(DarwinCoreArchiveVocab.LOCATION);
			writer.writeCharacters(nextLocation);
			// end location
			writer.writeEndElement();
		}
		// end files
		writer.writeEndElement();
		String coreId = this.getIdOrCoreId();
		if (coreId != null) {
			if (this.type == CoreOrExtension.CORE) {
				writer.writeStartElement(DarwinCoreArchiveVocab.ID);
			} else if (this.type == CoreOrExtension.EXTENSION) {
				writer.writeStartElement(DarwinCoreArchiveVocab.COREID);
			}
			writer.writeAttribute(DarwinCoreArchiveVocab.INDEX, coreId);
			// end id
			writer.writeEndElement();
		}
		for (DarwinCoreField nextField : this.getFields()) {
			writer.writeStartElement(DarwinCoreArchiveVocab.FIELD);
			if (nextField.getIndex() != null) {
				writer.writeAttribute(DarwinCoreArchiveVocab.INDEX, nextField.getIndex().toString());
			}
			writer.writeAttribute(DarwinCoreArchiveVocab.TERM, nextField.getTerm());
			if (nextField.getDefault() != null) {
				writer.writeAttribute(DarwinCoreArchiveVocab.DEFAULT, nextField.getDefault());
			}
			if (nextField.getVocabulary() != null) {
				writer.writeAttribute(DarwinCoreArchiveVocab.VOCABULARY, nextField.getVocabulary());
			}
			// end field
			writer.writeEndElement();
		}
		// end core
		writer.writeEndElement();
	}
}

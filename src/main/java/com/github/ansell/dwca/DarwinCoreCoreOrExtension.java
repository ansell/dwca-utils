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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents either a core or an extension element in a Darwin Core Archive XML
 * file.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public class DarwinCoreCoreOrExtension {

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

	private String fieldsTerminatedBy = ",";

	private String linesTerminatedBy = "\n";

	private String fieldsEnclosedBy = "\"";

	private Charset encoding = StandardCharsets.UTF_8;

	private int ignoreHeaderLines = 0;

	private DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;

	private final CoreOrExtension type;

	private DarwinCoreFile files;

	private DarwinCoreCoreOrExtension(CoreOrExtension type) {
		this.type = type;
	}

	public static DarwinCoreCoreOrExtension newCore() {
		return new DarwinCoreCoreOrExtension(CoreOrExtension.CORE);
	}

	public static DarwinCoreCoreOrExtension newExtension() {
		return new DarwinCoreCoreOrExtension(CoreOrExtension.EXTENSION);
	}

	public String getIdOrCoreId() {
		return idOrCoreId;
	}

	public void setIdOrCoreId(String idOrCoreId) {
		this.idOrCoreId = idOrCoreId;
	}

	public String getRowType() {
		return rowType;
	}

	public void setRowType(String rowType) {
		this.rowType = rowType;
	}

	public String getFieldsTerminatedBy() {
		return fieldsTerminatedBy;
	}

	public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
		this.fieldsTerminatedBy = fieldsTerminatedBy;
	}

	public String getLinesTerminatedBy() {
		return linesTerminatedBy;
	}

	public void setLinesTerminatedBy(String linesTerminatedBy) {
		this.linesTerminatedBy = linesTerminatedBy;
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

	public DateTimeFormatter getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateTimeFormatter dateFormat) {
		this.dateFormat = dateFormat;
	}

	public CoreOrExtension getType() {
		return type;
	}

	public DarwinCoreFile getFiles() {
		return files;
	}

	public void setFiles(DarwinCoreFile files) {
		if (this.files != null) {
			throw new IllegalStateException("Only a single files is allowed for each core or extension.");
		}
		this.files = files;
	}
}

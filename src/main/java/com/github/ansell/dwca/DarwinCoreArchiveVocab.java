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

/**
 * The terms referenced in the Darwin Core Archive vocabulary.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public final class DarwinCoreArchiveVocab {

	public static final String DWC = "http://rs.tdwg.org/dwc/text/";
	public static final String DWC_TERMS = "http://rs.tdwg.org/dwc/terms/";
	public static final String AC_TERMS = "http://rs.tdwg.org/ac/terms/";
	public static final String GNA_TERMS = "http://rs.gbif.org/terms/1.0/";
	public static final String ROW_TYPE = "rowType";
	public static final String FIELDS_TERMINATED_BY = "fieldsTerminatedBy";
	public static final String LINES_TERMINATED_BY = "linesTerminatedBy";
	public static final String FIELDS_ENCLOSED_BY = "fieldsEnclosedBy";
	public static final String ENCODING = "encoding";
	public static final String IGNORE_HEADER_LINES = "ignoreHeaderLines";
	public static final String DATE_FORMAT = "dateFormat";
	public static final String ARCHIVE = "archive";
	public static final String CORE = "core";
	public static final String FILES = "files";
	public static final String LOCATION = "location";
	public static final String FIELD = "field";
	public static final String EXTENSION = "extension";
	public static final String ID = "id";
	public static final String COREID = "coreId";
	public static final String INDEX = "index";
	public static final String TERM = "term";
	public static final String DEFAULT = "default";
	public static final String VOCABULARY = "vocabulary";
	public static final String SIMPLE_DARWIN_RECORD = "http://rs.tdwg.org/dwc/xsd/simpledarwincore/SimpleDarwinRecord";
	public static final String MULTIMEDIA_RECORD = "http://rs.gbif.org/terms/1.0/Multimedia";

	/**
	 * Private constructor for static-only class.
	 */
	private DarwinCoreArchiveVocab() {
	}
}

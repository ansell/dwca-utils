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

import org.xml.sax.Attributes;

/**
 * A field that may be specified for a core or extension in a Darwin Core
 * Archive metadata XML file.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public class DarwinCoreField {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DarwinCoreField [");
		if (index != null) {
			builder.append("index=");
			builder.append(index);
			builder.append(", ");
		}
		if (term != null) {
			builder.append("term=");
			builder.append(term);
			builder.append(", ");
		}
		if (defaultValue != null) {
			builder.append("defaultValue=");
			builder.append(defaultValue);
			builder.append(", ");
		}
		if (vocabulary != null) {
			builder.append("vocabulary=");
			builder.append(vocabulary);
		}
		builder.append("]");
		return builder.toString();
	}

	private Integer index;
	private String term;
	private String defaultValue;
	private String vocabulary;

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		if (this.index != null) {
			throw new IllegalStateException("Cannot specify multiple indexes for a field.");
		}
		this.index = index;
	}

	public String getTerm() {
		if (this.term == null) {
			throw new IllegalStateException("Term was required for field, but was not set.");
		}
		return term;
	}

	public void setTerm(String term) {
		if (this.term != null) {
			throw new IllegalStateException("Cannot specify multiple terms for a field.");
		}
		this.term = term;
	}

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		if (this.defaultValue != null) {
			throw new IllegalStateException("Cannot specify multiple default values for a field.");
		}
		this.defaultValue = defaultValue;
	}

	public String getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(String vocabularyUri) {
		if (this.vocabulary != null) {
			throw new IllegalStateException("Cannot specify multiple vocabularies for a field.");
		}
		this.vocabulary = vocabularyUri;
	}

	public static DarwinCoreField fromAttributes(Attributes attributes) {
		DarwinCoreField result = new DarwinCoreField();
		for (int i = 0; i < attributes.getLength(); i++) {
			String namespace = attributes.getURI(i);
			String localName = attributes.getLocalName(i);
			if (DarwinCoreArchiveVocab.INDEX.equals(localName)) {
				result.setIndex(Integer.parseInt(attributes.getValue(i)));
			} else if (DarwinCoreArchiveVocab.TERM.equals(localName)) {
				result.setTerm(attributes.getValue(i));
			} else if (DarwinCoreArchiveVocab.DEFAULT.equals(localName)) {
				result.setDefault(attributes.getValue(i));
			} else if (DarwinCoreArchiveVocab.VOCABULARY.equals(localName)) {
				result.setVocabulary(attributes.getValue(i));
			} else {
				System.out.println("Found unrecognised Darwin Core attribute for field " + " : " + localName);
			}
		}
		return result;
	}

}

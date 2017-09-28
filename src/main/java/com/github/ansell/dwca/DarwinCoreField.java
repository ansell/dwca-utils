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
public class DarwinCoreField implements ConstraintChecked, Comparable<DarwinCoreField> {

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
		if (delimitedBy != null) {
			builder.append("delimitedBy=");
			builder.append(delimitedBy);
		}
		builder.append("]");
		return builder.toString();
	}

	private Integer index;
	private String term;
	private String defaultValue;
	private String vocabulary;
	private String delimitedBy;

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		if (this.index != null) {
			throw new IllegalStateException("Cannot specify multiple indexes for a field: " + this.toString());
		}
		this.index = index;
	}

	public boolean hasTerm() {
		return this.term != null;
	}

	public String getTerm() {
		if (this.term == null) {
			throw new IllegalStateException("Term was required for field, but was not set: " + this.toString());
		}
		return term;
	}

	public void setTerm(String term) {
		if (this.term != null && !this.term.equals(term)) {
			throw new IllegalStateException("Cannot specify multiple terms for a field: " + this.toString());
		}
		this.term = term;
	}

	public boolean hasDefault() {
		return defaultValue != null;
	}

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		if (this.defaultValue != null && !this.defaultValue.equals(defaultValue)) {
			throw new IllegalStateException("Cannot specify multiple default values for a field: " + this.toString());
		}
		this.defaultValue = defaultValue;
	}

	public String getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(String vocabularyUri) {
		if (this.vocabulary != null && !this.vocabulary.equals(vocabularyUri)) {
			throw new IllegalStateException("Cannot specify multiple vocabularies for a field: " + this.toString());
		}
		this.vocabulary = vocabularyUri;
	}

	public String getDelimitedBy() {
		return delimitedBy;
	}

	public void setDelimitedBy(String delimitedBy) {
		if (this.delimitedBy != null && !this.delimitedBy.equals(delimitedBy)) {
			throw new IllegalStateException(
					"Cannot specify multiple delimitedBy values for a field: " + this.toString());
		}
		this.delimitedBy = delimitedBy;
	}

	public static DarwinCoreField fromAttributes(Attributes attributes) {
		DarwinCoreField result = new DarwinCoreField();
		for (int i = 0; i < attributes.getLength(); i++) {
			// String namespace = attributes.getURI(i);
			String localName = attributes.getLocalName(i);
			if (DarwinCoreArchiveConstants.INDEX.equals(localName)) {
				result.setIndex(Integer.parseInt(attributes.getValue(i)));
			} else if (DarwinCoreArchiveConstants.TERM.equals(localName)) {
				result.setTerm(attributes.getValue(i));
			} else if (DarwinCoreArchiveConstants.DEFAULT.equals(localName)) {
				result.setDefault(attributes.getValue(i));
			} else if (DarwinCoreArchiveConstants.VOCABULARY.equals(localName)) {
				result.setVocabulary(attributes.getValue(i));
			} else if (DarwinCoreArchiveConstants.DELIMITED_BY.equals(localName)) {
				result.setDelimitedBy(attributes.getValue(i));
			} else {
				System.out.println("Found unrecognised Darwin Core attribute for field, skipping : " + localName);
			}
		}
		return result;
	}

	@Override
	public void checkConstraints() {
		if (getTerm() == null) {
			throw new IllegalStateException("All fields must have term set: " + this.toString());
		}
		if (getIndex() == null && getDefault() == null) {
			throw new IllegalStateException(
					"Fields that do not have indexes must have default values set: " + this.toString());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((delimitedBy == null) ? 0 : delimitedBy.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		result = prime * result + ((vocabulary == null) ? 0 : vocabulary.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DarwinCoreField)) {
			return false;
		}
		DarwinCoreField other = (DarwinCoreField) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null) {
				return false;
			}
		} else if (!defaultValue.equals(other.defaultValue)) {
			return false;
		}
		if (delimitedBy == null) {
			if (other.delimitedBy != null) {
				return false;
			}
		} else if (!delimitedBy.equals(other.delimitedBy)) {
			return false;
		}
		if (index == null) {
			if (other.index != null) {
				return false;
			}
		} else if (!index.equals(other.index)) {
			return false;
		}
		if (term == null) {
			if (other.term != null) {
				return false;
			}
		} else if (!term.equals(other.term)) {
			return false;
		}
		if (vocabulary == null) {
			if (other.vocabulary != null) {
				return false;
			}
		} else if (!vocabulary.equals(other.vocabulary)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(DarwinCoreField o) {
		// Ensure we only compare valid objects
		this.checkConstraints();
		o.checkConstraints();
		if(this.getIndex() == null) {
			if(o.getIndex() == null) {
				return this.getTerm().compareTo(o.getTerm());
			} else {
				return 1;
			}
		} else if (o.getIndex() == null) {
			return -1;
		} else {
			return this.getIndex().compareTo(o.getIndex());
		}
	}

}

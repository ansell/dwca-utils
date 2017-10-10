/*
 * Copyright (c) 2017, Peter Ansell
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link DarwinCoreRecord}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class DarwinCoreRecordImpl implements DarwinCoreRecord {

	private final DarwinCoreArchiveDocument document;
	private final List<DarwinCoreField> fields;
	private final List<String> values;

	/**
	 * Create a DarwinCoreRecord based on a document, an ordered list of fields,
	 * and a matching ordered set of values.
	 * 
	 * @param document
	 *            The document that this record is based on.
	 * @param fields
	 *            An ordered set of fields.
	 * @param values
	 *            An ordered set of values which matches the ordered set of
	 *            fields.
	 */
	public DarwinCoreRecordImpl(DarwinCoreArchiveDocument document, List<DarwinCoreField> fields, List<String> values) {
		this.document = Objects.requireNonNull(document, "Document cannot be null");
		this.fields = Objects.requireNonNull(fields, "Fields cannot be null");
		this.values = Objects.requireNonNull(values, "Values cannot be null");
		if (this.fields.size() != this.values.size()) {
			throw new IllegalArgumentException("Fields and values lists must be the same size: fields size="
					+ fields.size() + " values size=" + values.size());
		}
	}

	@Override
	public DarwinCoreArchiveDocument getDocument() {
		return this.document;
	}

	@Override
	public List<DarwinCoreField> getFields() {
		return this.fields;
	}

	@Override
	public Optional<String> valueFor(String term, boolean includeDefaults) {
		if (term == null) {
			throw new IllegalArgumentException("Cannot get a value for a null term");
		}
		for (int i = 0; i < values.size(); i++) {
			if (fields.get(i).getTerm().equals(term)) {
				String result = values.get(i);
				if (result == null || result.isEmpty()) {
					if (includeDefaults && fields.get(i).hasDefault()) {
						return Optional.of(fields.get(i).getDefault());
					} else {
						// Null should not occur, but if it does,
						// wrap it with empty string
						return Optional.of("");
					}
				} else {
					return Optional.of(result);
				}
			}
		}
		// Optional.empty is reserved for when the term did not
		// appear in the list, otherwise it gets empty string
		return Optional.empty();
	}

}

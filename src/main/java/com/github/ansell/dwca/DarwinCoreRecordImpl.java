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
import java.util.Optional;

/**
 * Implementation of {@link DarwinCoreRecord}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreRecordImpl implements DarwinCoreRecord {

	private final DarwinCoreArchiveDocument document;
	private final List<DarwinCoreField> fields;
	private final List<String> values;

	public DarwinCoreRecordImpl(DarwinCoreArchiveDocument document, List<DarwinCoreField> fields, List<String> values) {
		this.document = document;
		this.fields = fields;
		this.values = values;
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
	public List<String> getValues() {
		return this.values;
	}

	@Override
	public Optional<String> valueFor(String term, boolean includeDefaults) {
		List<DarwinCoreField> fields = getFields();
		List<String> values = getValues();
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

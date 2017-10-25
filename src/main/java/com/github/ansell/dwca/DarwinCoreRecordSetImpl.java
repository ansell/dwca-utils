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

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link DarwinCoreRecordSet}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreRecordSetImpl implements DarwinCoreRecordSet {

	private final DarwinCoreArchiveDocument document;
	private final DarwinCoreRecord coreRecord;
	private final Map<DarwinCoreCoreOrExtension, DarwinCoreRecord> extensionRecords;

	public DarwinCoreRecordSetImpl(DarwinCoreArchiveDocument document, DarwinCoreRecord coreRecord,
			Map<DarwinCoreCoreOrExtension, DarwinCoreRecord> extensionRecords) {
		this.document = Objects.requireNonNull(document, "Document cannot be null");
		this.coreRecord = Objects.requireNonNull(coreRecord, "Core record cannot be null");
		this.extensionRecords = Objects.requireNonNull(extensionRecords, "Extension records cannot be null");
	}

	@Override
	public DarwinCoreArchiveDocument getDocument() {
		return this.document;
	}

	@Override
	public DarwinCoreRecord getCoreRecord() {
		return this.coreRecord;
	}

	@Override
	public Map<DarwinCoreCoreOrExtension, DarwinCoreRecord> getExtensionRecords() {
		return this.extensionRecords;
	}

}

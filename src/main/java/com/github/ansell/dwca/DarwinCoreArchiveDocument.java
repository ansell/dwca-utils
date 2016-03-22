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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an entire Darwin Core Archive document as parsed into memory.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public class DarwinCoreArchiveDocument {

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DarwinCoreArchiveDocument [core=");
		builder.append(core);
		builder.append(", extensions=");
		builder.append(extensions != null ? extensions.subList(0, Math.min(extensions.size(), maxLen)) : null);
		builder.append("]");
		return builder.toString();
	}

	private DarwinCoreCoreOrExtension core;

	private final List<DarwinCoreCoreOrExtension> extensions = new ArrayList<>();

	public DarwinCoreCoreOrExtension getCore() {
		return core;
	}

	public void setCore(DarwinCoreCoreOrExtension core) {
		if (this.core != null) {
			throw new IllegalStateException("Multiple core elements found for darwin core archive document");
		}
		this.core = core;
	}

	public List<DarwinCoreCoreOrExtension> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public void addExtension(DarwinCoreCoreOrExtension extension) {
		this.extensions.add(extension);
	}

	/**
	 * Checks that semantic constraints are enforced and throws an exception if
	 * they are found not to be enforced.
	 * 
	 * @throws IllegalStateException
	 *             If semantic constraints are not enforced.
	 */
	public void checkConstraints() throws IllegalStateException {
		if (core.getFields().isEmpty()) {
			throw new IllegalStateException("Core must have fields.");
		}
		if (core.getFiles() == null) {
			throw new IllegalStateException("Core must have files set.");
		}
		if (!this.getExtensions().isEmpty() && this.getCore().getIdOrCoreId() == null) {
			throw new IllegalStateException("Core must have id set if there are extensions present.");
		}
		for (DarwinCoreCoreOrExtension extension : getExtensions()) {
			if (extension.getIdOrCoreId() == null) {
				throw new IllegalStateException("Extensions must have coreId set.");
			}
			if (extension.getFields().isEmpty()) {
				throw new IllegalStateException("Extension must have fields.");
			}
			if (extension.getFiles() == null) {
				throw new IllegalStateException("Extension must have files set.");
			}
		}
	}

}

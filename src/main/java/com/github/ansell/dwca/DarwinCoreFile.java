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
 * An object representing a file in a Darwin Core Archive XML file.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreFile {

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DarwinCoreFile [");
		if (locations != null) {
			builder.append("locations=");
			builder.append(locations.subList(0, Math.min(locations.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locations == null) ? 0 : locations.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DarwinCoreFile)) {
			return false;
		}
		DarwinCoreFile other = (DarwinCoreFile) obj;
		if (locations == null) {
			if (other.locations != null) {
				return false;
			}
		} else if (!locations.equals(other.locations)) {
			return false;
		}
		return true;
	}

	private final List<String> locations = new ArrayList<>();

	public List<String> getLocations() {
		return Collections.unmodifiableList(locations);
	}

	public void addLocation(String nextLocation) {
		this.locations.add(nextLocation);
	}
}

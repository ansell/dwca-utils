/**
 * 
 */
package com.github.ansell.dwca.schemas;

import org.eclipse.rdf4j.model.IRI;

import com.github.ansell.rdf4j.schemagenerator.Schema;

/**
 * A container for schemas used in the process of generating and validating DWCA
 * archives.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface DarwinCoreArchiveSchema extends Schema {

	/**
	 * 
	 * @return The IRI used as the base for this schema.
	 */
	IRI getIRI();
	
}

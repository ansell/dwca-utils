package com.github.ansell.dwca.schemas;

import com.github.ansell.rdf4j.schemagenerator.SchemaRegistry;

/**
 * A container for schemas used in the process of generating and validating DWCA
 * archives.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DarwinCoreArchiveSchemaRegistry extends SchemaRegistry<DarwinCoreArchiveSchema> {

	private static final long serialVersionUID = -3214407281190239170L;

	private static final DarwinCoreArchiveSchemaRegistry DEFAULT_INSTANCE = new DarwinCoreArchiveSchemaRegistry();
	
	public DarwinCoreArchiveSchemaRegistry() {
		super(DarwinCoreArchiveSchema.class, DarwinCoreArchiveSchema::getIRI);
	}

	public static DarwinCoreArchiveSchemaRegistry getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

}

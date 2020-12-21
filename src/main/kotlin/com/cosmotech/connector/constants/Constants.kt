package com.cosmotech.connector.constants

// ############################
// ## Azure Digital Twins
// ############################

/**
 * Probe suffix used to filter Probe Digital Twins
 */
const val PROBE_SUFFIX = "Probe"

/**
 * Environment variable in which the ADT endpoint is stored
 */
const val ENVVAR_ADT_INSTANCE_URL = "ADT_INSTANCE_URL"

/**
 * Environment variable in which the absolute export path for CSVs is stored
 */
const val ENVVAR_ABSOLUTE_PATH_EXPORT_CSV_FILE = "EXPORT_CSV_FILE_ABSOLUTE_PATH"

/**
 * Default header cell name for digital twins
 */
val digitalTwinDefaultHeader = mutableListOf("id")

/**
 * Default header cell name for relations
 */
val relationshipDefaultHeader = mutableListOf("source", "target", "name")




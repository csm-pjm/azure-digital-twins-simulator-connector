package com.cosmotech.connector.constants

// ############################
// ## Azure Digital Twins
// ############################

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
val modelDefaultProperties = mutableMapOf("id" to "string")

/**
 * Default header cell name for relations
 */
val relationshipDefaultHeader = mutableMapOf("source" to "string","target" to "string","name" to "string")




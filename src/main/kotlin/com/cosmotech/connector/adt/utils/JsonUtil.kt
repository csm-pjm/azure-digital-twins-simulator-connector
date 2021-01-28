package com.cosmotech.connector.adt.utils

import com.beust.klaxon.JsonObject
import com.cosmotech.connector.adt.constants.*

/**
 * Utility class for JSON object
 */
class JsonUtil {

    companion object Reader {

        /**
         * Read the 'contents' array of a model data
         * @param jsonModel the JSON Object representing a model
         * @return the list of property names
         */
        @JvmStatic
        fun readPropertiesName(jsonModel: JsonObject) =
            jsonModel.array<JsonObject>(DTDL_CONTENTS_KEY)
                ?.filter { it.getOrDefault(DTDL_TYPE_KEY, "")!! == DTDL_PROPERTY_KEY }
                ?.map { it[DTDL_NAME_KEY].toString() }

        /**
         * Read the 'contents' array of a model data
         * @param jsonModel the JSON Object representing a model
         * @return the list of property names
         */
        @JvmStatic
        fun readPropertiesNameAndType(jsonModel: JsonObject): MutableMap<String,String> {
            val result = mutableMapOf<String, String>()
            jsonModel.array<JsonObject>(DTDL_CONTENTS_KEY)
                ?.filter { it.getOrDefault(DTDL_TYPE_KEY, "")!! == DTDL_PROPERTY_KEY }
                ?.forEach {
                    if( it[DTDL_SCHEMA_KEY] !is JsonObject )
                        result[it[DTDL_NAME_KEY].toString()] = it[DTDL_SCHEMA_KEY].toString()
                    else
                        result[it[DTDL_NAME_KEY].toString()] = "CompositeType"
                }

            return result
        }

        /**
         * Read the key "extends" in an JSON Object
         * If exist returns a pair of { true, "extends_value"}
         * where "extends_value" contains the model_id of the root model
         * If not returns a pair of {false,""}
         * @return a Pair(Boolean,String) containing minimal extension information
         */
        @JvmStatic
        fun isExtension(jsonModel: JsonObject):Pair<Boolean,String> {
            return if( jsonModel.containsKey(DTDL_EXTENDS_KEY) )
                Pair(true,jsonModel[DTDL_EXTENDS_KEY].toString())
            else
                Pair(false,"")
        }
        
    }

}
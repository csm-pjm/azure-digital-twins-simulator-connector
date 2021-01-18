package com.cosmotech.connector.utils

import com.beust.klaxon.JsonObject

/**
 * class Utility for JSON object
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
            jsonModel.array<JsonObject>("contents")
                ?.filter { it.getOrDefault("@type", "")!! == "Property" }
                ?.map { it["name"].toString() }

        /**
         * Read the 'contents' array of a model data
         * @param jsonModel the JSON Object representing a model
         * @return the list of property names
         */
        @JvmStatic
        fun readPropertiesNameAndType(jsonModel: JsonObject): MutableMap<String,String> {
            val result = mutableMapOf<String, String>()
            jsonModel.array<JsonObject>("contents")
                ?.filter { it.getOrDefault("@type", "")!! == "Property" }
                ?.forEach {
                    if( it["schema"] !is JsonObject )
                        result[it["name"].toString()] = it["schema"].toString()
                    else
                        result[it["name"].toString()] = "CompositeType"
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
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun isExtension(jsonModel: JsonObject):Pair<Boolean,String> =
            if(jsonModel.containsKey("extends")) Pair(true,jsonModel["extends"].toString()) else Pair(false,"")


    }

}
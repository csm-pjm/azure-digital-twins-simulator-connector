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
        fun readPropertiesNameAndType(jsonModel: JsonObject) =
            jsonModel.array<JsonObject>("contents")
                ?.filter { it.getOrDefault("@type", "")!! == "Property" }
                ?.map { Pair(it["name"].toString(), it["schema"].toString()) }
    }



}
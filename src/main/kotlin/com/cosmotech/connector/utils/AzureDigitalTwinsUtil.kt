package com.cosmotech.connector.utils

import com.azure.digitaltwins.core.BasicDigitalTwin
import com.azure.digitaltwins.core.BasicRelationship
import com.azure.digitaltwins.core.DigitalTwinsClient
import com.cosmotech.connector.commons.pojo.CsvData
import com.cosmotech.connector.constants.modelDefaultProperties
import com.cosmotech.connector.constants.relationshipDefaultHeader
import com.cosmotech.connector.extensions.getModelNameFromModelId
import com.fasterxml.jackson.databind.util.RawValue

/**
 * Utility class to handle Azure Digital Twins objects
 */
class AzureDigitalTwinsUtil {

    companion object Builder {
        /**
         * Construct a digital twin row to print into a CSV
         * @param defaultHeaderValues the default row values
         * @param headers the list of header cell name
         * @param digitalTwin a digital twin
         * @return the data row to print
         */
        @JvmStatic
        fun constructDigitalTwinRowValue(
            defaultHeaderValues: ArrayList<String>,
            headers: MutableMap<String,String>,
            digitalTwin: BasicDigitalTwin
        ): ArrayList<String> {

            val digitalTwinRow = ArrayList(defaultHeaderValues)

            headers.forEach { (headerCellName, _) ->
                if (!modelDefaultProperties.contains(headerCellName)) {
                    val specificCustomProperties = digitalTwin.contents[headerCellName]
                    var specificCustomPropertiesValue = specificCustomProperties.toString()
                    if (specificCustomProperties is RawValue) {
                        specificCustomPropertiesValue = specificCustomProperties.rawValue().toString()
                    }
                    digitalTwinRow.add(specificCustomPropertiesValue)
                }
            }
            return digitalTwinRow
        }

        /**
         * Construct a relation data row
         * @param relation a relation
         * @return a relationship data row
         *
         */
        @JvmStatic
        fun constructRelationshipRowValue(relation: BasicRelationship): ArrayList<String> {
            val rowValues = ArrayList<String>()
            rowValues.add(relation.sourceId)
            rowValues.add(relation.targetId)
            rowValues.add(relation.name)
            relation.properties.values.forEach {
                rowValues.add(it?.toString() ?: "")
            }
            return rowValues
        }

        /**
         * Construct a digital twin row to print into a CSV
         * @param digitalTwin a digital twin
         * @param digitalTwinsToExport digital twin data rows
         * @param digitalTwinHeaderNameAndType CSV digital twin header names
         * @return the list of digital twin data rows
         */
        @JvmStatic
        fun constructDigitalTwinInformation(
            digitalTwin: BasicDigitalTwin,
            digitalTwinHeaderNameAndType: MutableMap<String,String>,
            dtHeaderDefaultValues: ArrayList<String>,
            digitalTwinsToExport: MutableList<CsvData>
        ) {
            val modelName = digitalTwin.metadata.modelId.getModelNameFromModelId()
            val csvData = digitalTwinsToExport.find { it.fileName == modelName }
            if (null != csvData) {
                val rowValue =
                    constructDigitalTwinRowValue(dtHeaderDefaultValues, csvData.headerNameAndType, digitalTwin)
                csvData.rows.add(rowValue)
            } else {
                val digitalTwinRowValue = constructDigitalTwinRowValue(
                    dtHeaderDefaultValues,
                    digitalTwinHeaderNameAndType,
                    digitalTwin
                )
                val rows = mutableListOf<MutableList<String>>()
                rows.add(digitalTwinRowValue)
                digitalTwinsToExport.add(CsvData(modelName, digitalTwinHeaderNameAndType, rows))
            }
        }

        /**
         * Construct and fill the Triple object containing all information for relations
         * @param client an ADT client
         * @param digitalTwin the digital twin that owns the relations
         * @param relationshipsToExport the Triple object to fill
         */
        @JvmStatic
        fun constructRelationshipInformation(
            client: DigitalTwinsClient,
            digitalTwin: BasicDigitalTwin,
            relationshipsToExport: MutableList<CsvData>
        ) {
            val currentRelationships =
                client.listRelationships(digitalTwin.id, BasicRelationship::class.java).toList()

            val relationshipGroupByName = currentRelationships.groupBy { it.name }

            relationshipGroupByName.entries.forEach { (relationName, basicRelationships) ->
                val relationInformation = relationshipsToExport.find { it.fileName == relationName }
                if (null != relationInformation) {
                    basicRelationships.forEach { relation ->
                        relationInformation.rows.add(constructRelationshipRowValue(relation))
                    }
                } else {
                    val relationshipHeaderName = HashMap(relationshipDefaultHeader)
                    if (basicRelationships.isNotEmpty()) {
                        //TODO Handle relationship properties correctly
                        /*basicRelationships[0].properties.keys.forEach {
                            relationshipHeaderName[it] = "string"
                        }*/
                        val rows = mutableListOf<MutableList<String>>()
                        basicRelationships.forEach { relation ->
                            rows.add(constructRelationshipRowValue(relation))
                        }
                        relationshipsToExport.add(CsvData(relationName, relationshipHeaderName,rows))
                    }
                }
            }
        }
    }


}
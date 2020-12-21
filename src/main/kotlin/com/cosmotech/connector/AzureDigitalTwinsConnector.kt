package com.cosmotech.connector

import com.azure.digitaltwins.core.*
import com.azure.identity.DefaultAzureCredentialBuilder
import com.beust.klaxon.Klaxon
import com.cosmotech.connector.commons.Connector
import com.cosmotech.connector.commons.pojo.CsvData
import com.cosmotech.connector.constants.*
import com.cosmotech.connector.extensions.getModelNameFromModelId
import com.cosmotech.connector.utils.AzureDigitalTwinsUtil
import com.cosmotech.connector.utils.JsonUtil
import java.io.StringReader

/**
 * Connector for Azure Digital Twin
 */
class AzureDigitalTwinsConnector() : Connector<DigitalTwinsClient> {

    private var adtInstanceUrl: String
    private var exportCsvFolderPath: String

    init {
        adtInstanceUrl = System.getenv()[ENVVAR_ADT_INSTANCE_URL].toString()
        exportCsvFolderPath = System.getenv()[ENVVAR_ABSOLUTE_PATH_EXPORT_CSV_FILE].toString()
    }

    constructor(
        adtInstanceUrlValue: String,
        exportCsvFolderPathValue: String
    ) : this() {
        adtInstanceUrl = adtInstanceUrlValue
        exportCsvFolderPath = exportCsvFolderPathValue
    }


    override fun buildClient(): DigitalTwinsClient {
        return DigitalTwinsClientBuilder()
            .credential(
                DefaultAzureCredentialBuilder().build()
            )
            .endpoint(adtInstanceUrl)
            .serviceVersion(DigitalTwinsServiceVersion.getLatest())
            .buildClient()
    }

    override fun constructSimulatorData(client: DigitalTwinsClient): List<CsvData> {
        val listModels = client.listModels()
        val dataToExport = mutableListOf<CsvData>()

        listModels
            .filter { !it.modelId.getModelNameFromModelId().endsWith(PROBE_SUFFIX) }
            .forEach { modelData ->
                // Model Information
                val modelId = modelData.modelId
                val model = client.getModel(modelId).dtdlModel
                val jsonModel = Klaxon().parseJsonObject(StringReader(model))

                // DT Information
                val digitalTwinHeaderName = ArrayList(digitalTwinDefaultHeader)
                val propertiesName = JsonUtil.readPropertiesName(jsonModel)
                if (propertiesName != null) {
                    digitalTwinHeaderName.addAll(propertiesName.toMutableList())
                }

                val digitalTwinInModel = client.query(
                    "SELECT * FROM DIGITALTWINS WHERE IS_OF_MODEL('$modelId')",
                    BasicDigitalTwin::class.java
                )

                digitalTwinInModel.forEach { digitalTwin ->
                    val dtHeaderDefaultValues = ArrayList<String>()
                    dtHeaderDefaultValues.add(digitalTwin.id)
                    AzureDigitalTwinsUtil.constructDigitalTwinInformation(
                        digitalTwin,
                        digitalTwinHeaderName,
                        dtHeaderDefaultValues,
                        dataToExport
                    )
                    AzureDigitalTwinsUtil.constructRelationshipInformation(client, digitalTwin, dataToExport)
                }
            }
        return dataToExport
    }

    override fun process() {
        val client = this.buildClient()
        val processedData = this.constructSimulatorData(client)
        processedData.forEach {
            // Uncomment it if you want to use the EXPORT_CSV_FILE_ABSOLUTE_PATH environment variable
            // it.exportDirectory = exportCsvFolderPath
            it.exportData()
        }
    }

}

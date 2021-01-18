package com.cosmotech.connector

import com.azure.digitaltwins.core.*
import com.azure.identity.DefaultAzureCredentialBuilder
import com.beust.klaxon.Klaxon
import com.cosmotech.connector.commons.Connector
import com.cosmotech.connector.commons.pojo.CsvData
import com.cosmotech.connector.constants.*
import com.cosmotech.connector.pojos.DTDLModelInformation
import com.cosmotech.connector.utils.AzureDigitalTwinsUtil
import com.cosmotech.connector.utils.JsonUtil
import java.io.StringReader

/**
 * Connector for Azure Digital Twin
 */
class AzureDigitalTwinsConnector() : Connector<DigitalTwinsClient,List<CsvData>> {

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
        val modelInformationList = mutableListOf<DTDLModelInformation>()
        // Retrieve model Information
        listModels
            .forEach { modelData ->
                // DTDL Model Information
                val modelId = modelData.modelId
                val model = client.getModel(modelId).dtdlModel
                val jsonModel = Klaxon().parseJsonObject(StringReader(model))
                // DT Information
                val propertiesModel = HashMap(modelDefaultProperties)
                val propertiesName = JsonUtil.readPropertiesNameAndType(jsonModel)
                propertiesModel.putAll(propertiesName)
                //TODO maybe handle multiple extend
                // For the moment only one level is managed
                val extensionInfo = JsonUtil.isExtension(jsonModel)
                modelInformationList.add(
                    DTDLModelInformation(modelId,extensionInfo.first,extensionInfo.second,propertiesModel,model)
                )
            }
        // Fill the missing properties for all extension model
        modelInformationList
            .filter { it.isExtension }
            .forEach { information ->
                val extendedModel = modelInformationList.find { it.id == information.extensionModelId }
                extendedModel!!.properties.forEach {
                        (key,value) -> information.properties.putIfAbsent(key,value)
                }
            }

        val digitalTwinInstances = mutableListOf<BasicDigitalTwin>()
        // Construct DT information list
        modelInformationList.forEach { modelInformation ->
            val digitalTwinInModel = client.query(
                "SELECT * FROM DIGITALTWINS WHERE IS_OF_MODEL('${modelInformation.id}')",
                BasicDigitalTwin::class.java
            )
            digitalTwinInModel.forEach {
                digitalTwinInstances.add(it)
            }
        }
        val digitalTwinInformation = mutableListOf<Pair<DTDLModelInformation,BasicDigitalTwin>>()
        digitalTwinInstances.forEach { dtInstance ->
            val modelMatched = modelInformationList.first { it.id == dtInstance.metadata.modelId }
            digitalTwinInformation.add(Pair(modelMatched,dtInstance))
        }

        digitalTwinInformation.forEach { (modelInformation,dtInstance) ->
            val dtHeaderDefaultValues = ArrayList<String>()
            dtHeaderDefaultValues.add(dtInstance.id)
            AzureDigitalTwinsUtil
                .constructDigitalTwinInformation(
                    dtInstance,
                    modelInformation.properties,
                    dtHeaderDefaultValues,
                    dataToExport
                )
            AzureDigitalTwinsUtil
                .constructRelationshipInformation(
                    client,
                    dtInstance,
                    dataToExport
                )
        }

        return dataToExport
    }

    override fun process() {
        val client = this.buildClient()
        val processedData = this.constructSimulatorData(client)
        processedData.forEach {
            // Uncomment it if you want to use the EXPORT_CSV_FILE_ABSOLUTE_PATH environment variable
            //it.exportDirectory = exportCsvFolderPath
            it.exportData()
        }
    }

}

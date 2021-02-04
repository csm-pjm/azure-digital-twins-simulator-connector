package com.cosmotech.connector.adt

import com.azure.digitaltwins.core.*
import com.azure.identity.ClientSecretCredentialBuilder
import com.beust.klaxon.Klaxon
import com.cosmotech.connector.adt.constants.modelDefaultProperties
import com.cosmotech.connector.adt.pojos.DTDLModelInformation
import com.cosmotech.connector.adt.utils.AzureDigitalTwinsUtil
import com.cosmotech.connector.adt.utils.JsonUtil
import com.cosmotech.connector.commons.Connector
import com.cosmotech.connector.commons.pojo.CsvData
import java.io.StringReader

/**
 * Connector for Azure Digital Twin
 */
class AzureDigitalTwinsConnector : Connector<DigitalTwinsClient,List<CsvData>,List<CsvData>> {

    override fun createClient(): DigitalTwinsClient {
        return DigitalTwinsClientBuilder()
            .credential(
                ClientSecretCredentialBuilder()
                    .clientId(AzureDigitalTwinsUtil.getAzureClientId())
                    .tenantId(AzureDigitalTwinsUtil.getAzureTenantId())
                    .clientSecret(AzureDigitalTwinsUtil.getAzureClientSecret())
                    .build()
            )
            .endpoint(AzureDigitalTwinsUtil.getInstanceUrl())
            .serviceVersion(DigitalTwinsServiceVersion.getLatest())
            .buildClient()
    }

    override fun prepare(client: DigitalTwinsClient): List<CsvData> {
        val listModels = client.listModels()
        val dataToExport = mutableListOf<CsvData>()
        var modelInformationList = mutableListOf<DTDLModelInformation>()
        // Retrieve model Information
        listModels
            .sortedBy { it.modelId }
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

        modelInformationList = AzureDigitalTwinsUtil.retrievePropertiesFromExtendedModel(modelInformationList)

        val digitalTwinInstances = constructDigitalTwinInstances(modelInformationList, client)

        val digitalTwinInformation = mutableListOf<Pair<DTDLModelInformation,BasicDigitalTwin>>()
        digitalTwinInstances
            .sortedBy { it.id }
            .forEach { dtInstance ->
            val modelMatched = modelInformationList.first { it.id == dtInstance.metadata.modelId }
            digitalTwinInformation.add(Pair(modelMatched,dtInstance))
        }

        digitalTwinInformation.forEach { (modelInformation,dtInstance) ->
            val dtHeaderDefaultValues = mutableListOf<String>(dtInstance.id)
            AzureDigitalTwinsUtil
                .constructDigitalTwinInformation(
                    dtInstance,
                    modelInformation.properties,
                    dtHeaderDefaultValues,
                    dataToExport
                )

            val currentRelationships =
                client
                    .listRelationships(dtInstance.id, BasicRelationship::class.java)
                    .toList()
                    .groupBy { it.name }

            AzureDigitalTwinsUtil
                .constructRelationshipInformation(
                    currentRelationships,
                    dataToExport
                )
        }

        return dataToExport
    }

    override fun process(): List<CsvData> {
        val client = this.createClient()
        val preparedData = this.prepare(client)
        preparedData.forEach {
            // Uncomment it if you want to use the EXPORT_CSV_FILE_ABSOLUTE_PATH environment variable
            it.exportDirectory = AzureDigitalTwinsUtil.getExportCsvFilesPath()
            it.writeFile()
        }
        return preparedData
    }

    /**
     * Fetch and regroup all existing digital twin instances
     * @param modelInformationList the existing DT model list
     * @param client the Azure Digital Twin client
     * @return the list of Digital Twin instances
     */
    private fun constructDigitalTwinInstances(
        modelInformationList: MutableList<DTDLModelInformation>,
        client: DigitalTwinsClient
    ): MutableList<BasicDigitalTwin> {
        val digitalTwinInstances = mutableListOf<BasicDigitalTwin>()
        // Construct DT information list
        modelInformationList.forEach { modelInformation ->
            val digitalTwinInModel = client.query(
                "SELECT * FROM DIGITALTWINS WHERE IS_OF_MODEL('${modelInformation.id}')",
                BasicDigitalTwin::class.java
            )
            digitalTwinInModel
                .forEach {
                    digitalTwinInstances.add(it)
                }
        }
        return digitalTwinInstances
    }
}

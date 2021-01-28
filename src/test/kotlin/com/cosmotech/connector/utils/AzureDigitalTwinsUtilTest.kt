package com.cosmotech.connector.utils

import com.azure.digitaltwins.core.BasicDigitalTwin
import com.azure.digitaltwins.core.BasicDigitalTwinMetadata
import com.azure.digitaltwins.core.BasicRelationship
import com.azure.digitaltwins.core.DigitalTwinPropertyMetadata
import com.cosmotech.connector.AbstractUnitTest
import com.cosmotech.connector.adt.pojos.DTDLModelInformation
import com.cosmotech.connector.adt.utils.AzureDigitalTwinsUtil
import com.fasterxml.jackson.databind.util.RawValue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AzureDigitalTwinsUtilTest: AbstractUnitTest() {

    private val defaultDigitalTwinIdTest = "DigitalTwinIdTest"
    private val defaultHeaderValues = mutableListOf(defaultDigitalTwinIdTest)
    private val defaultHeaderNameAndType=
        mutableMapOf(
            "StockName" to "string",
            "CurrentStock" to "double",
            "MaximalStock" to "integer",
            "IsProportionType" to "boolean",
            "StartingDate" to "dateTime",
            "_id" to "string")
    private val defaultHeaderNameAndTypeForRelationship=
        mutableMapOf(
            "name" to "string",
            "source" to "string",
            "target" to "string")
    private val defaultRowValue = listOf(
        "StockName",
        "2.7182818284",
        "123456789",
        "true",
        "2020-09-01T00:00:00+00:00",
        defaultDigitalTwinIdTest
    )
    private val dtWithExtendDTDLModel: String =
        getResourceFile("utils/dt-with-extends.json")
        .toFile()
        .readText(Charsets.UTF_8)
    private val dtWithoutExtendDTDLModel: String =
        getResourceFile("utils/dt-with-no-extends.json")
            .toFile()
            .readText(Charsets.UTF_8)

    @Test
    fun test_constructDigitalTwinRowValue() {
        val digitalTwin = constructBasicDigitalTwinForTest()
        val rowValue = AzureDigitalTwinsUtil
            .constructDigitalTwinRowValue(
                defaultHeaderValues,
                defaultHeaderNameAndType,
                digitalTwin
            )
        assertEquals(
            defaultRowValue,
            rowValue)

    }

    @Test
    fun test_constructRelationshipRowValue() {
        val rowValue = AzureDigitalTwinsUtil
            .constructRelationshipRowValue(
                createBasicRelationshipForTest(
                    defaultDigitalTwinIdTest,
                    "targetDigitalTwinId",
                    "relationshipName"
                )
            )
        assertEquals(
            listOf(defaultDigitalTwinIdTest,
                "targetDigitalTwinId",
                "relationshipName")
            ,rowValue
        )
    }

    @Test
    fun test_constructDigitalTwinInformation() {
        val digitalTwin = constructBasicDigitalTwinForTest()
        val digitalTwinsToExport = AzureDigitalTwinsUtil
            .constructDigitalTwinInformation(
                digitalTwin,
                defaultHeaderNameAndType,
                defaultHeaderValues,
                mutableListOf()
            )
        assertEquals(1,digitalTwinsToExport.size,"Size's list should be 1")
        val csvData = digitalTwinsToExport[0]
        assertEquals("BasicDigitalTwin",csvData.fileName)
        assertEquals(defaultHeaderNameAndType,csvData.headerNameAndType)
        assertEquals(1,csvData.rows.size)
        assertTrue(csvData.rows[0].containsAll(defaultRowValue))
    }

    @Test
    fun test_constructRelationshipInformation() {
        val relationships = mapOf<String, List<BasicRelationship>>(
            "contains_DTB" to createBasicRelationshipsForTest("DTA","DTB","contains_DTB",5),
            "contains_DTC" to createBasicRelationshipsForTest("DTB","DTC","contains_DTC",4),
            "ToDTA" to createBasicRelationshipsForTest("DTC","DTA","ToDTA",8)
        )

        val dataToExport = AzureDigitalTwinsUtil
            .constructRelationshipInformation(
                relationships,
                mutableListOf()
            )
        assertNotNull(dataToExport)
        assertEquals(3,dataToExport.size)
        for (csvData in dataToExport)
            assertEquals(defaultHeaderNameAndTypeForRelationship,csvData.headerNameAndType)

        assertEquals("contains_DTB",dataToExport[0].fileName)
        val containsDTBRows = dataToExport[0].rows
        assertEquals(5, containsDTBRows.size)
        for (i in 0 until 5) {
            assertTrue(containsDTBRows.contains(listOf("DTA$i","DTB$i","contains_DTB$i")))
        }

        assertEquals("contains_DTC",dataToExport[1].fileName)
        val containsDTCRows = dataToExport[1].rows
        assertEquals(4, containsDTCRows.size)
        for (i in 0 until 4) {
            assertTrue(containsDTCRows.contains(listOf("DTB$i","DTC$i","contains_DTC$i")))
        }

        assertEquals("ToDTA",dataToExport[2].fileName)
        val toDTARows = dataToExport[2].rows
        assertEquals(8, toDTARows.size)
        for (i in 0 until 4) {
            assertTrue(toDTARows.contains(listOf("DTC$i","DTA$i","ToDTA$i")))
        }

    }

    @Test
    fun test_constructRelationshipInformation_noRelationship() {
        val dataToExport = AzureDigitalTwinsUtil
            .constructRelationshipInformation(
                mapOf(),
                mutableListOf()
            )
        assertTrue(dataToExport.isEmpty())
    }

    @Test
    fun test_retrievePropertiesFromExtendedModel() {
        val extensionProperties = mutableMapOf(
            "TransportDuration" to "integer",
            "StockBeingProcessedAsString" to "string",
            "StockBeingProcessedValuesAsString" to "string"
        )
        val mainDTProperties = mutableMapOf(
            "OutputType" to "string"
        )
        val extensionDT = DTDLModelInformation(
            "dtmi:com:cosmotech:supply:TransportOperation;1",
            true,
            "dtmi:com:cosmotech:supply:Operation;1",
            extensionProperties,
            dtWithExtendDTDLModel
        )
        val mainDT = DTDLModelInformation(
            "dtmi:com:cosmotech:supply:Operation;1",
            false,
            null,
            mainDTProperties,
            dtWithoutExtendDTDLModel
        )
        val modelInformationList = mutableListOf(
            extensionDT,
            mainDT
        )

        assertFalse(extensionDT.properties.keys.containsAll(mainDTProperties.keys))
        val completedModelInformationList =
            AzureDigitalTwinsUtil.retrievePropertiesFromExtendedModel(modelInformationList)
        assertEquals(2,completedModelInformationList.size)
        assertTrue(completedModelInformationList.contains(mainDT))
        assertTrue(completedModelInformationList.contains(extensionDT))
        assertNotNull(extensionDT.properties)
        assertEquals(
            4,
            extensionDT.properties.size)
        assertTrue(extensionDT.properties.keys.containsAll(mainDTProperties.keys))
        for ((key,value) in mainDTProperties)
            assertEquals(extensionDT.properties[key], value)
    }



    private fun constructBasicDigitalTwinForTest():BasicDigitalTwin {
        val defaultDigitalTwinContents=
            mutableMapOf(
                "StockName" to RawValue("StockName"),
                "CurrentStock" to 2.7182818284,
                "MaximalStock" to 123456789,
                "IsProportionType" to true,
                "StartingDate" to "2020-09-01T00:00:00+00:00")

        val digitalTwinForTest =
            BasicDigitalTwin(defaultDigitalTwinIdTest)
                .setETag("W/\"b5860694-3f74-4c98-9f9f-4534ad8b6d11\"")
                .setMetadata(createBasicDigitalTwinMetadataForTest())

        digitalTwinForTest.contents.putAll(defaultDigitalTwinContents)

        return digitalTwinForTest
    }

    private fun createBasicDigitalTwinMetadataForTest(): BasicDigitalTwinMetadata {
        val result = BasicDigitalTwinMetadata()
            .setModelId("dtmi:com:cosmotech:supply:BasicDigitalTwin;1")

        val metadataProperties = mutableMapOf(
            "StockName" to DigitalTwinPropertyMetadata().setLastUpdatedOn(OffsetDateTime.now()),
            "CurrentStock" to DigitalTwinPropertyMetadata().setLastUpdatedOn(OffsetDateTime.now()),
            "MaximalStock" to DigitalTwinPropertyMetadata().setLastUpdatedOn(OffsetDateTime.now()),
            "StartingDate" to DigitalTwinPropertyMetadata().setLastUpdatedOn(OffsetDateTime.now()),
            "IsProportionType" to DigitalTwinPropertyMetadata().setLastUpdatedOn(OffsetDateTime.now())
        )
        result.propertyMetadata.putAll(metadataProperties)
        return result
    }

    // TODO handle properties on relationship
    private fun createBasicRelationshipForTest(source:String,target:String,name:String): BasicRelationship {
        return BasicRelationship(
                UUID.randomUUID().toString(),
                source,
                target,
                name)
    }

    // TODO handle properties on relationship
    private fun createBasicRelationshipsForTest(source:String,target:String,name:String,number:Int): MutableList<BasicRelationship> {
        if (number < 0)
            throw IllegalArgumentException("number should be greater than 0")

        val result = mutableListOf<BasicRelationship>()
        for (i in 0 until number) {
            result.add(
                createBasicRelationshipForTest(
                    source.plus(i),
                    target.plus(i),
                    name.plus(i))
            )
         }

        return result
    }


}
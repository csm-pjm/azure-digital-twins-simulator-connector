package com.cosmotech.connector.utils

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.cosmotech.connector.CsmUnitTest
import org.junit.Test
import java.io.StringReader
import kotlin.test.assertEquals

/**
 * Test class for JsonUtil methods
 */
class JsonUtilTest: CsmUnitTest() {

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private val dtJsonModel: JsonObject = Klaxon()
        .parseJsonObject(
            StringReader(
                getTestResource("utils/dt-model-template.json")
                    ?.readText(Charsets.UTF_8)
            )
        )

    @Test
    fun test_readPropertiesName() {
        val readPropertiesName = JsonUtil.Reader.readPropertiesName(dtJsonModel)
        assertEquals(
            readPropertiesName,
            listOf(
                "TimeStepDuration",
                "TimeStepPerCycle",
                "NumberOfCycle",
                "StartingDate",
                "ManageBacklogQuantities",
                "EnforceProductionPlan"
            ),
            "Check that the properties list is the expected one"
        )
    }

    @Test
    fun test_readPropertiesNameAndType() {
        val readPropertiesNameAndType = JsonUtil.Reader.readPropertiesNameAndType(dtJsonModel)
        assertEquals(
            readPropertiesNameAndType,
            listOf(
                Pair("TimeStepDuration","string"),
                Pair("TimeStepPerCycle","integer"),
                Pair("NumberOfCycle","integer"),
                Pair("StartingDate","dateTime"),
                Pair("ManageBacklogQuantities","boolean"),
                Pair("EnforceProductionPlan","boolean")
            ),
            "Check that the list of Pair if the expected one"
        )
    }
}

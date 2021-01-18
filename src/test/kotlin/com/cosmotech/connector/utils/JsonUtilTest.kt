package com.cosmotech.connector.utils

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.cosmotech.connector.CsmUnitTest
import org.junit.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private val dtJsonWithExtendsModel: JsonObject = Klaxon()
        .parseJsonObject(
            StringReader(
                getTestResource("utils/dt-with-extends.json")
                    ?.readText(Charsets.UTF_8)
            )
        )

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private val dtJsonWithoutExtendsModel: JsonObject = Klaxon()
        .parseJsonObject(
            StringReader(
                getTestResource("utils/dt-with-no-extends.json")
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
                "EnforceProductionPlan",
                "OptimizationObjective"
            ),
            "Check that the properties list is the expected one"
        )
    }

    //TODO add test for composite types
    @Test
    fun test_readPropertiesNameAndType() {
        val readPropertiesNameAndType = JsonUtil.Reader.readPropertiesNameAndType(dtJsonModel)
        assertEquals(
            readPropertiesNameAndType,
            mapOf(
                "TimeStepDuration" to "string",
                "TimeStepPerCycle" to "integer",
                "NumberOfCycle" to "integer",
                "StartingDate" to "dateTime",
                "ManageBacklogQuantities" to "boolean",
                "EnforceProductionPlan" to "boolean",
                "OptimizationObjective" to "CompositeType"
            ),
            "Check that the list of Pair if the expected one"
        )
    }

    @Test
    fun test_isExtension_without_extends() {
        val extensionPair = JsonUtil.Reader.isExtension(dtJsonWithoutExtendsModel)
        assertFalse(extensionPair.first, "Check if the check for the 'extends' properties is correct")
        assertEquals(extensionPair.second,"", "Check if the check for the 'extends' properties is correct")
    }

    @Test
    fun test_isExtension_with_extends() {
        val extensionPair = JsonUtil.Reader.isExtension(dtJsonWithExtendsModel)
        assertTrue(extensionPair.first, "Check if the check for the 'extends' properties is correct")
        assertEquals(extensionPair.second,"dtmi:com:cosmotech:supply:Operation;1", "Check if the check for the 'extends' properties is correct")
    }
}

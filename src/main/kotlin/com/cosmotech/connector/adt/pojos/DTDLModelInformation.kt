// copyright (c) cosmo tech corporation.
// licensed under the mit license.

package com.cosmotech.connector.adt.pojos

/**
 * Represent a DTDL Model
 * @property id represents the modelId
 * @property isExtension tells us if this model extends an existing one
 * @property extensionModelId tells us which DTDL model is extended by the current model
 * @property properties contains properties map (name, type) for this specific model
 * @property dtdlModel store the definition of the model in DTDL format
 */
data class DTDLModelInformation(val id:String,
                                val isExtension:Boolean,
                                val extensionModelId:String?,
                                val properties:MutableMap<String,String>,
                                val dtdlModel:String) {
}
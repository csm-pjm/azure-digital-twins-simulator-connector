package com.cosmotech.connector.extensions

/**
 * @return Return a ADT model name from a ADT model id
 */
fun String.getModelNameFromModelId(): String {
    return this.split(":").last().split(";").first()
}




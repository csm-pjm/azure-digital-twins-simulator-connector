package com.cosmotech.connector

import java.net.URL

open class CsmUnitTest() {

    fun getTestResource(filepath:String) : URL? {
        return this::class
            .java
            .classLoader
            .getResource(filepath)
    }

}
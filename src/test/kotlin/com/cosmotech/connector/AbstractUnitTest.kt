// copyright (c) cosmo tech corporation.
// licensed under the mit license.

package com.cosmotech.connector

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

abstract class AbstractUnitTest {

    /**
     * Get the URL of a resource from a filePath
     * the filePath is relative from resources directory
     * @param filepath the file path
     * @return the URL of the resource (can be null)
     */
    private fun getTestResource(filepath:String) : URL? {
        return this::class
            .java
            .classLoader
            .getResource(filepath)
    }

    /**
     * Get the Path representing a resource file from a filePath
     * the filePath is relative from resources directory
     * @param filePath the file path
     * @return a Path with the desired resource
     * @throws RuntimeException if the resource does not exist or cannot be found
     */
    fun getResourceFile(filePath: String): Path {
        val resourceURL = getTestResource(filePath)
            ?: throw RuntimeException("Resource $filePath cannot be found")
        return Paths.get(resourceURL.file)
    }


}
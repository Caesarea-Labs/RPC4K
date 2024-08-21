package com.caesarealabs.rpc4k.runtime.jvm.user.testing

import com.caesarealabs.logging.PrintLogging
import com.caesarealabs.rpc4k.runtime.jvm.user.components.mongo.MongoDb
import com.caesarealabs.rpc4k.runtime.jvm.user.components.mongo.manualCreateClientMongoDbClient
import com.mongodb.MongoSocketException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.bson.Document
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.io.File

/**
 * Uses a docker TestContainer for MongoDB.
 * If the SHARE_TEST_CONTAINERS environment variable is set to true, multiple processes will share the same test container.
 *
 * **Requires org.testcontainers:mongodb to be manually added to the classpath** (to not bloat apps that don't want testcontainers)
 *
 * **Requires docker to be installed**.
 */
public data object TestContainerMongoDb : MongoDb {
    private val sharedTestContainerFile = File(System.getProperty("user.home"), ".sharedContainers").resolve("MongoDbSharedTestContainer.txt")

    /**
     * Set to true when this process creates a new docker for mongodb
     */
    private var ownsContainer = false


    /**
     * Will be set to not null when [getOrCreateClient] is called
     */
    private var container: MongoDBContainer? = null
    private var closed = false
    private val client = lazy {
        val client = if (sharedTestContainerFile.exists()) {
            val sharedConnectionString = sharedTestContainerFile.readText()
            val (existingDatabase, error) = databaseAt(sharedConnectionString)
            if (existingDatabase == null) fallbackToNewDocker(sharedConnectionString, error)
            else {
                PrintLogging.logInfo { "Shared test container with $sharedConnectionString" }
                existingDatabase
            }
        } else {
            manualCreateClientMongoDbClient(dockerContainer())
        }
        // Make sure to close connection/container when jvm exits
        Runtime.getRuntime().addShutdownHook(Thread {
            close()
        })
        client
    }

    /**
     * Returns the database if it can be connected to at [connectionString], null otherwise
     * If connecting to the database fails because of an exception, will return that exception as well.
     */
    private fun databaseAt(connectionString: String): Pair<MongoClient?, MongoSocketException?> = runBlocking {
        try {
            val client = manualCreateClientMongoDbClient(connectionString)
            val database: MongoDatabase = client.getDatabase("admin")

            val ping = Document("ping", 1)
            val res = withTimeoutOrNull(5_000) {
                database.runCommand(ping)
            }

            // Database exists if ping doesn't fail
            if (res == null) null to null else client to null
        } catch (e: MongoSocketException) {
            null to e
        }
    }

    /**
     * Called when [sharedTestContainerFile] but a database cannot be accessed at the connection string.
     */
    private fun fallbackToNewDocker(missingConnection: String, exception: MongoSocketException?): MongoClient {
        PrintLogging.logWarn(exception) {
            "Could not connect to supposedly existing connection at $missingConnection. " +
                "Make sure to close the TestContainer after using it."
        }
        // Don't make this mistake again
        sharedTestContainerFile.delete()
        return manualCreateClientMongoDbClient(dockerContainer())
    }

    /**
     * Start a new docket container and write down its connection string so other processes can connect to it too
     */
    private fun dockerContainer(): String {
        val container = MongoDBContainer(DockerImageName.parse("mongo:7.0.11"))
        container.start()
        this.container = container
        val connectionString = container.connectionString
        ownsContainer = true
        sharedTestContainerFile.parentFile.mkdirs()
//             write down connection string so other processes can connect to it too
        sharedTestContainerFile.writeText(connectionString)
        return connectionString
    }

    override fun close() {
        if (!closed && client.isInitialized()) {
            client.value.close()
            container?.close()
            // Can't connect to container anymore because this process will close it
            if (ownsContainer) sharedTestContainerFile.delete()
            closed = true
        }
    }

    override fun getOrCreateClient(): MongoClient = client.value
}
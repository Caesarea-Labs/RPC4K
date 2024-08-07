package com.caesarealabs.rpc4k.runtime.jvm.user.components.mongo

import com.caesarealabs.rpc4k.runtime.jvm.user.components.mongo.MongoDb.None.getOrCreateClient
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.datetime.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import org.bson.BsonDateTime
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.kotlinx.BsonDecoder
import org.bson.codecs.kotlinx.BsonEncoder
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import kotlin.io.path.*

/**
 * Abstraction for a MongoDb database.
 * Allows using mongodb in all contexts, even places where we cannot access the real cloud mongodb, with [MongoDb.TestContainer]
 * In the case where a [MongoDb] instances is required but isn't used, you can use a [MongoDb.None].
 */
public interface MongoDb : AutoCloseable {


    /**
     * Reference to a MongoDB handle that can be used to perform MongoDB operations
     * The first call is often very expensive so be cautious
     */
    public fun getOrCreateClient(): MongoClient


    /**
     * Uses a real cloud MongoDB database.
     *
     * **Requires MongoDB access keys**
     */
    public data class Atlas(private val connectionString: () -> String) : MongoDb {
        /**
         * Whether [getOrCreateClient] was called.
         * This is used to check if closing the MongoDb is required.
         */
        private var createdClient: Boolean = false
        private val client by lazy {

            createdClient = true
            // Use a real connection url
            // Try to avoid storing mongoDbConnectionUrl as a variable because it is security sensitive
            manualCreateClientMongoDbClient(connectionString())
        }

        override fun getOrCreateClient(): MongoClient = client
        override fun close() {
            if (createdClient) {
                // Make sure to not reference the client if not needed
                client.close()
            }
        }
    }


    /**
     * Will throw when the [getOrCreateClient] is accessed
     */
    public data object None : MongoDb {

        override fun close() {

        }

        override fun getOrCreateClient(): MongoClient = throw IllegalStateException("The 'None' MongoDB doesn't expect to be used")
    }
}

private object InstantAsBsonDateTime : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantAsBsonDateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Instant) {
        when (encoder) {
            is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.toEpochMilliseconds()))
            else -> throw SerializationException("Instant is not supported by ${encoder::class}")
        }
    }

    override fun deserialize(decoder: Decoder): Instant {
        return when (decoder) {
            is BsonDecoder -> Instant.fromEpochMilliseconds(decoder.decodeBsonValue().asDateTime().value)
            else -> throw SerializationException("Instant is not supported by ${decoder::class}")
        }
    }
}

private val startOfDay = LocalTime(0, 0)

/**
 * Encodes a [LocalDate] as a [BsonDateTime] with epoch millis equal to the start of the day
 */
private object LocalDateAsBsonDateTime : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantAsBsonDateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LocalDate) {
        when (encoder) {
            is BsonEncoder -> encoder.encodeBsonValue(BsonDateTime(value.atTime(startOfDay).toInstant(TimeZone.UTC).toEpochMilliseconds()))
            else -> throw SerializationException("Instant is not supported by ${encoder::class}")
        }
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return when (decoder) {
            is BsonDecoder -> Instant.fromEpochMilliseconds(decoder.decodeBsonValue().asDateTime().value).startOfDayUtc()
            else -> throw SerializationException("Instant is not supported by ${decoder::class}")
        }
    }
}

private fun Instant.startOfDayUtc(): LocalDate = toLocalDateTime(TimeZone.UTC).date



/**
 * Utility for [MongoDb] implementations
 */
public fun manualCreateClientMongoDbClient(connectionString: String): MongoClient {
    val myCustomCodec = KotlinSerializerCodec.create<Instant>(
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantAsBsonDateTime)
            contextual(LocalDate::class, LocalDateAsBsonDateTime)
        }
    )
    val registry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromCodecs(myCustomCodec)
    )

    return MongoClient.create(
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .codecRegistry(registry)
            .build()
    )
}

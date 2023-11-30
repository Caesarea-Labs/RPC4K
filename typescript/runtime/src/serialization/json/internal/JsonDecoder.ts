/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {Json} from "../Json";
import {CompositeDecoder, Decoder} from "../../core/encoding/Decoding";

/**
 * Decoder used by Json during deserialization.
 * This interface can be used to inject desired behaviour into a serialization process of Json.
 */
export interface JsonDecoder extends Decoder, CompositeDecoder {
    /**
     * An instance of the current Json.
     */
    json: Json;
    //
    // /**
    //  * Decodes the next element in the current input as JsonElement.
    //  * The type of the decoded element depends on the current state of the input and, when received
    //  * by serializer in its serialize method, the type of the token directly matches
    //  * the kind.
    //  *
    //  * This method is allowed to invoke only as the part of the whole deserialization process of the class,
    //  * calling this method after invoking beginStructure or any `decode*` method will lead to unspecified behaviour.
    //  */
    // abstract decodeJsonElement(): JsonElement;
}
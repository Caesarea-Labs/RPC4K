/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
/**
 * Configuration of the current Json instance available through Json.configuration
 * and configured with JsonBuilder constructor.
 *
 * Can be used for debug purposes and for custom Json-specific serializers
 * via JsonEncoder and JsonDecoder.
 *
 * Standalone configuration object is meaningless and cannot be used outside the
 * Json, neither new Json instance can be created from it.
 *
 * Detailed description of each property is available in JsonBuilder class.
 */

export class JsonConfiguration {
    public encodeDefaults: boolean;
    public ignoreUnknownKeys: boolean;
    public isLenient: boolean;
    public allowStructuredMapKeys: boolean;
    public prettyPrint: boolean;
    public explicitNulls: boolean;
    public prettyPrintIndent: string;
    public coerceInputValues: boolean;
    public useArrayPolymorphism: boolean;
    public classDiscriminator: string;
    public allowSpecialFloatingPointValues: boolean;
    public useAlternativeNames: boolean;
    public namingStrategy: JsonNamingStrategy | null;
    public decodeEnumsCaseInsensitive: boolean;

    constructor(
        encodeDefaults: boolean = false,
        ignoreUnknownKeys: boolean = false,
        isLenient: boolean = false,
        allowStructuredMapKeys: boolean = false,
        prettyPrint: boolean = false,
        explicitNulls: boolean = true,
        prettyPrintIndent: string = "    ",
        coerceInputValues: boolean = false,
        useArrayPolymorphism: boolean = false,
        classDiscriminator: string = "type",
        allowSpecialFloatingPointValues: boolean = false,
        useAlternativeNames: boolean = true,
        namingStrategy: JsonNamingStrategy | null = null,
        decodeEnumsCaseInsensitive: boolean = false
    ) {
        this.encodeDefaults = encodeDefaults
        this.ignoreUnknownKeys = ignoreUnknownKeys
        this.isLenient = isLenient
        this.allowStructuredMapKeys = allowStructuredMapKeys
        this.prettyPrint = prettyPrint
        this.explicitNulls = explicitNulls
        this.prettyPrintIndent = prettyPrintIndent
        this.coerceInputValues = coerceInputValues
        this.useArrayPolymorphism = useArrayPolymorphism
        this.classDiscriminator = classDiscriminator
        this.allowSpecialFloatingPointValues = allowSpecialFloatingPointValues
        this.useAlternativeNames = useAlternativeNames
        this.namingStrategy = namingStrategy
        this.decodeEnumsCaseInsensitive = decodeEnumsCaseInsensitive
    }
}

export interface JsonNamingStrategy{}
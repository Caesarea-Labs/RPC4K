/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {TsClass} from "../polyfills/TsClass";
import {DeserializationStrategy, SerializationStrategy, TsSerializer} from "../TsSerializer";



export abstract class SerializersModule {

    abstract getContextual<T>(
        kClass: TsClass,
        typeArgumentsSerializers?: TsSerializer<unknown>[]
    ): TsSerializer<T> | null;

    abstract getPolymorphicSerialization<T>(baseClass: TsClass, value: T, valueType: TsClass): SerializationStrategy<T> | null;

    abstract getPolymorphicDeserialization<T>(
        baseClass: TsClass,
        serializedClassName: string | null
    ): DeserializationStrategy<T> | null;

    // // And one more abstract method
    // abstract dumpTo(collector: SerializersModuleCollector): void;
}

export class SerialModuleImpl extends SerializersModule{
    private class2ContextualFactory: Map<TsClass, ContextualProvider>;
    public polyBase2Serializers: Map<TsClass, Map<TsClass, TsSerializer<unknown>>>;
    private polyBase2DefaultSerializerProvider: Map<TsClass, PolymorphicSerializerProvider<unknown>>;
    private polyBase2NamedSerializers: Map<TsClass, Map<string, TsSerializer<unknown>>>;
    private polyBase2DefaultDeserializerProvider: Map<TsClass, PolymorphicDeserializerProvider<unknown>>;

    constructor(
        class2ContextualFactory: Map<TsClass, ContextualProvider>,
        polyBase2Serializers: Map<TsClass, Map<TsClass, TsSerializer<unknown>>>,
        polyBase2DefaultSerializerProvider: Map<TsClass, PolymorphicSerializerProvider<unknown>>,
        polyBase2NamedSerializers: Map<TsClass, Map<string, TsSerializer<unknown>>>,
        polyBase2DefaultDeserializerProvider: Map<TsClass, PolymorphicDeserializerProvider<unknown>>
    ) {
        super();
        this.class2ContextualFactory = class2ContextualFactory;
        this.polyBase2Serializers = polyBase2Serializers;
        this.polyBase2DefaultSerializerProvider = polyBase2DefaultSerializerProvider;
        this.polyBase2NamedSerializers = polyBase2NamedSerializers;
        this.polyBase2DefaultDeserializerProvider = polyBase2DefaultDeserializerProvider;
    }

    getPolymorphicSerialization<T>(baseClass: TsClass, value: T, valueType: TsClass): SerializationStrategy<T> | null {
        // Registered
        const registered = this.polyBase2Serializers.get(baseClass)?.get(valueType) as SerializationStrategy<T>;
        if (registered !== undefined) return registered;
        // Default
        return this.polyBase2DefaultSerializerProvider.get(baseClass)?.(value) ?? null;
    }

    getPolymorphicDeserialization<T>(baseClass: TsClass, serializedClassName: string): DeserializationStrategy<T> | null {
        // Registered
        const registered = this.polyBase2NamedSerializers.get(baseClass)?.get(serializedClassName) as TsSerializer<T>;
        if (registered !== undefined) return registered;
        // Default
        return (this.polyBase2DefaultDeserializerProvider.get(baseClass)?.(serializedClassName) ?? null) as DeserializationStrategy<T> | null;
    }

    getContextual<T>(tsClass: TsClass, typeArgumentsSerializers: Array<TsSerializer<unknown>>): TsSerializer<T> | null {
        return this.class2ContextualFactory.get(tsClass)?.invoke(typeArgumentsSerializers) as TsSerializer<T>;
    }

    // dumpTo(collector: SerializersModuleCollector) {
    //     this.class2ContextualFactory.forEach((serial, tsClass) => {
    //         // ... similar logic for handling different cases
    //     });
    //
    //     this.polyBase2Serializers.forEach((classMap, baseClass) => {
    //         classMap.forEach((serializer, actualClass) => {
    //             collector.polymorphic(baseClass, actualClass, serializer);
    //         });
    //     });
    //
    //     this.polyBase2DefaultSerializerProvider.forEach((provider, baseClass) => {
    //         collector.polymorphicDefaultSerializer(baseClass, provider);
    //     });
    //
    //     this.polyBase2DefaultDeserializerProvider.forEach((provider, baseClass) => {
    //         collector.polymorphicDefaultDeserializer(baseClass, provider);
    //     });
    // }
}
export const EmptySerializersModule = new SerialModuleImpl(new Map(), new Map() , new Map(), new Map(), new Map())

abstract class ContextualProvider {
    abstract invoke(typeArgumentsSerializers: Array<TsSerializer<unknown>>): TsSerializer<unknown>;
}


type PolymorphicDeserializerProvider<Base> = (className: string | null) => DeserializationStrategy<Base> | null;
type PolymorphicSerializerProvider<Base> = (value: Base) => SerializationStrategy<Base> | null;
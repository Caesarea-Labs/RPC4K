/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import {getTsClass, TsClass} from "../polyfills/TsClass";
import {DeserializationStrategy, SerializationStrategy, TsSerializer} from "../TsSerializer";



export abstract class SerializersModule {

    abstract getContextual<T>(
        kClass: TsClass<T>,
        typeArgumentsSerializers?: TsSerializer<any>[]
    ): TsSerializer<T> | null;

    abstract getPolymorphicSerialization<T>(baseClass: TsClass<T>, value: T): SerializationStrategy<T> | null;

    abstract getPolymorphicDeserialization<T>(
        baseClass: TsClass<T>,
        serializedClassName: string | null
    ): DeserializationStrategy<T> | null;

    // // And one more abstract method
    // abstract dumpTo(collector: SerializersModuleCollector): void;
}

export class SerialModuleImpl extends SerializersModule{
    private class2ContextualFactory: Map<TsClass<any>, ContextualProvider>;
    public polyBase2Serializers: Map<TsClass<any>, Map<TsClass<any>, TsSerializer<any>>>;
    private polyBase2DefaultSerializerProvider: Map<TsClass<any>, PolymorphicSerializerProvider<any>>;
    private polyBase2NamedSerializers: Map<TsClass<any>, Map<string, TsSerializer<any>>>;
    private polyBase2DefaultDeserializerProvider: Map<TsClass<any>, PolymorphicDeserializerProvider<any>>;

    constructor(
        class2ContextualFactory: Map<TsClass<any>, ContextualProvider>,
        polyBase2Serializers: Map<TsClass<any>, Map<TsClass<any>, TsSerializer<any>>>,
        polyBase2DefaultSerializerProvider: Map<TsClass<any>, PolymorphicSerializerProvider<any>>,
        polyBase2NamedSerializers: Map<TsClass<any>, Map<string, TsSerializer<any>>>,
        polyBase2DefaultDeserializerProvider: Map<TsClass<any>, PolymorphicDeserializerProvider<any>>
    ) {
        super();
        this.class2ContextualFactory = class2ContextualFactory;
        this.polyBase2Serializers = polyBase2Serializers;
        this.polyBase2DefaultSerializerProvider = polyBase2DefaultSerializerProvider;
        this.polyBase2NamedSerializers = polyBase2NamedSerializers;
        this.polyBase2DefaultDeserializerProvider = polyBase2DefaultDeserializerProvider;
    }

    getPolymorphicSerialization<T>(baseClass: TsClass<T>, value: T): SerializationStrategy<T> | null {
        // Registered
        const registered = this.polyBase2Serializers.get(baseClass)?.get(getTsClass(value)) as SerializationStrategy<T>;
        if (registered) return registered;
        // Default
        return this.polyBase2DefaultSerializerProvider.get(baseClass)?.(value) ?? null;
    }

    getPolymorphicDeserialization<T>(baseClass: TsClass<T>, serializedClassName: string): DeserializationStrategy<T> | null {
        // Registered
        const registered = this.polyBase2NamedSerializers.get(baseClass)?.get(serializedClassName) as TsSerializer<T>;
        if (registered) return registered;
        // Default
        return this.polyBase2DefaultDeserializerProvider.get(baseClass)?.(serializedClassName) ?? null;
    }

    getContextual<T>(tsClass: TsClass<T>, typeArgumentsSerializers: Array<TsSerializer<any>>): TsSerializer<T> | null {
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
    abstract invoke(typeArgumentsSerializers: Array<TsSerializer<any>>): TsSerializer<any>;
}

class Argless extends ContextualProvider {
    serializer: TsSerializer<any>;

    constructor(serializer: TsSerializer<any>) {
        super();
        this.serializer = serializer;
    }

    invoke(typeArgumentsSerializers: Array<TsSerializer<any>>): TsSerializer<any> {
        return this.serializer;
    }
}

class WithTypeArguments extends ContextualProvider {
    provider: (typeArgumentsSerializers: Array<TsSerializer<any>>) => TsSerializer<any>;

    constructor(provider: (typeArgumentsSerializers: Array<TsSerializer<any>>) => TsSerializer<any>) {
        super();
        this.provider = provider;
    }

    invoke(typeArgumentsSerializers: Array<TsSerializer<any>>): TsSerializer<any> {
        return this.provider(typeArgumentsSerializers);
    }
}

type PolymorphicDeserializerProvider<Base> = (className: string | null) => DeserializationStrategy<Base> | null;
type PolymorphicSerializerProvider<Base> = (value: Base) => SerializationStrategy<Base> | null;
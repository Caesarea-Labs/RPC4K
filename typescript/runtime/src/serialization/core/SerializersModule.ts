/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
abstract class SerializersModule {

    // // Method with optional argument and default value
    // abstract getContextual<T>(
    //     kClass: KClass<T>,
    //     typeArgumentsSerializers?: KSerializer<any>[]
    // ): KSerializer<T> | null;
    //
    // // Another abstract method
    // abstract getPolymorphic<T>(baseClass: KClass<T>, value: T): SerializationStrategy<T> | null;
    //
    // // Another abstract method
    // abstract getPolymorphic<T>(
    //     baseClass: KClass<T>,
    //     serializedClassName: string | null
    // ): DeserializationStrategy<T> | null;
    //
    // // And one more abstract method
    // abstract dumpTo(collector: SerializersModuleCollector): void;
}
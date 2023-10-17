export type SerialKind = OtherSerialType | PrimitiveKind | StructureKind | PolymorphicKind

export enum OtherSerialType {
    Enum = "Enum",
    Contextual = "Contextual",
}

export enum PrimitiveKind {
    Boolean = "Boolean",
    Number = "Number",
    String = "String"
}

export enum StructureKind {
    List = "List",
    Object = "Object"
}

export enum PolymorphicKind {
    Open = "Open"
}

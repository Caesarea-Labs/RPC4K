export const TestRpcJson = `{
    "name": "UserProtocol",
    "methods": [
        {
            "name": "createLobby",
            "parameters": [
                {
                    "name": "createdBy",
                    "type": {
                        "name": "PlayerId",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "otherThing",
                    "type": {
                        "name": "string",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "CreateLobbyResponse",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "killSomeone",
            "parameters": [
                {
                    "name": "killer",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "shit",
                    "type": {
                        "name": "PlayerId",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "bar",
                    "type": {
                        "name": "void",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "i32",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "someShit",
            "parameters": [
                {
                    "name": "x",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "y",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "string",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "moreTypes",
            "parameters": [
                {
                    "name": "list",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "PlayerId",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "double",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "array",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                    {
                                        "name": "string",
                                        "isTypeParameter": false,
                                        "isOptional": false,
                                        "typeArguments": [
                                        ],
                                        "inlinedType": null
                                    }
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "pair",
                    "type": {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "triple",
                    "type": {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "void",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "PlayerId",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "entry",
                    "type": {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "record",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "record",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "InlineId",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": {
                                    "name": "i64",
                                    "isTypeParameter": false,
                                    "isOptional": false,
                                    "typeArguments": [
                                    ],
                                    "inlinedType": null
                                }
                            },
                            {
                                "name": "f64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                ],
                "inlinedType": null
            }
        },
        {
            "name": "test",
            "parameters": [
                {
                    "name": "pair",
                    "type": {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "tuple",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "f64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                ],
                "inlinedType": null
            }
        },
        {
            "name": "nullable",
            "parameters": [
                {
                    "name": "mayNull",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": true,
                        "typeArguments": [
                            {
                                "name": "PlayerId",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "mayNull2",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "PlayerId",
                                "isTypeParameter": false,
                                "isOptional": true,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "heavyNullable",
            "parameters": [
                {
                    "name": "mode",
                    "type": {
                        "name": "HeavyNullableTestMode",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "GenericThing",
                "isTypeParameter": false,
                "isOptional": true,
                "typeArguments": [
                    {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": true,
                        "typeArguments": [
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": true,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": true,
                        "typeArguments": [
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": true,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                ],
                "inlinedType": null
            }
        },
        {
            "name": "genericTest",
            "parameters": [
                {
                    "name": "thing",
                    "type": {
                        "name": "string",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "GenericThing",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "string",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                ],
                "inlinedType": null
            }
        },
        {
            "name": "errorTest",
            "parameters": [
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "requirementTest",
            "parameters": [
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "noArgTest",
            "parameters": [
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "requirementFail",
            "parameters": [
                {
                    "name": "value",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "withNullsTest",
            "parameters": [
                {
                    "name": "withNulls",
                    "type": {
                        "name": "WithNulls",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "WithNulls",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                ],
                "inlinedType": null
            }
        },
        {
            "name": "enumArgsTest",
            "parameters": [
                {
                    "name": "enumArgs",
                    "type": {
                        "name": "EnumArgs",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "EnumArgs",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "directObjectTest",
            "parameters": [
                {
                    "name": "obj",
                    "type": {
                        "name": "TheObject",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "TheObject",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "polymorphicTest",
            "parameters": [
                {
                    "name": "obj",
                    "type": {
                        "name": "PolymorphicThing",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicThing",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "directPolymorphicAccess",
            "parameters": [
                {
                    "name": "obj",
                    "type": {
                        "name": "Option1",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "Option1",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "polymorphicClassTest",
            "parameters": [
                {
                    "name": "obj",
                    "type": {
                        "name": "PolymorphicClass",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicClass",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "everyBuiltinType",
            "parameters": [
                {
                    "name": "obj",
                    "type": {
                        "name": "EveryBuiltinType",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "EveryBuiltinType",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ],
                "inlinedType": null
            }
        },
        {
            "name": "everyBuiltinTypeParams",
            "parameters": [
                {
                    "name": "a",
                    "type": {
                        "name": "bool",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "b",
                    "type": {
                        "name": "i8",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "c",
                    "type": {
                        "name": "i16",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "d",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "e",
                    "type": {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "f",
                    "type": {
                        "name": "char",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "g",
                    "type": {
                        "name": "string",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "h",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i8",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "i",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i16",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "j",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "k",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "l",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "char",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "m",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "n",
                    "type": {
                        "name": "record",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "o",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "p",
                    "type": {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "q",
                    "type": {
                        "name": "tuple",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "r",
                    "type": {
                        "name": "void",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "s",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "t",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i8",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "u",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i16",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "v",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "w",
                    "type": {
                        "name": "array",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "i64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ],
                                "inlinedType": null
                            }
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "x",
                    "type": {
                        "name": "i8",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "y",
                    "type": {
                        "name": "i16",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "z",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "a2",
                    "type": {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "b2",
                    "type": {
                        "name": "f32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                },
                {
                    "name": "b3",
                    "type": {
                        "name": "f64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                }
            ],
            "returnType": {
                "name": "tuple",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    },
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ],
                        "inlinedType": null
                    }
                ],
                "inlinedType": null
            }
        }
    ],
    "models": [
        {
            "type": "struct",
            "name": "WithNulls",
            "typeParameters": [
                "T"
            ],
            "properties": {
                "x": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "T",
                            "isTypeParameter": true,
                            "isOptional": true,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "y": {
                    "name": "string",
                    "isTypeParameter": false,
                    "isOptional": true,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "struct",
            "name": "PlayerId",
            "typeParameters": [
            ],
            "properties": {
                "num": {
                    "name": "i64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "union",
            "name": "PolymorphicThing",
            "options": [
                {
                    "name": "Option1",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                {
                    "name": "Option2",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            ],
            "typeParameters": [
            ]
        },
        {
            "type": "struct",
            "name": "Option4",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "struct",
            "name": "CreateLobbyResponse",
            "typeParameters": [
            ],
            "properties": {
                "id": {
                    "name": "i64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "struct",
            "name": "GenericThing",
            "typeParameters": [
                "T1",
                "T2",
                "T3"
            ],
            "properties": {
                "x": {
                    "name": "T1",
                    "isTypeParameter": true,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "y": {
                    "name": "T2",
                    "isTypeParameter": true,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "z": {
                    "name": "T3",
                    "isTypeParameter": true,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "w": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "T3",
                            "isTypeParameter": true,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "struct",
            "name": "Option2",
            "typeParameters": [
            ],
            "properties": {
            }
        },
        {
            "type": "struct",
            "name": "Option3",
            "typeParameters": [
            ],
            "properties": {
            }
        },
        {
            "type": "struct",
            "name": "TheObject",
            "typeParameters": [
            ],
            "properties": {
            }
        },
        {
            "type": "struct",
            "name": "EnumArgs",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "name": {
                    "name": "EnumArgsOptions",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "enum",
            "name": "EnumArgsOptions",
            "options": [
                "Option1",
                "Option5"
            ]
        },
        {
            "type": "union",
            "name": "PolymorphicClass",
            "options": [
                {
                    "name": "Option3",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                {
                    "name": "Option4",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            ],
            "typeParameters": [
            ]
        },
        {
            "type": "struct",
            "name": "EveryBuiltinType",
            "typeParameters": [
            ],
            "properties": {
                "a": {
                    "name": "bool",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "b": {
                    "name": "i8",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "c": {
                    "name": "i16",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "d": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "e": {
                    "name": "i64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "f": {
                    "name": "char",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "g": {
                    "name": "string",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "h": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i8",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "i": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i16",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "j": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "k": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i64",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "l": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "char",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "m": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "n": {
                    "name": "record",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "o": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "p": {
                    "name": "tuple",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "q": {
                    "name": "tuple",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "r": {
                    "name": "void",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "s": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "t": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i8",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "u": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i16",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "v": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "w": {
                    "name": "array",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "i64",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ],
                            "inlinedType": null
                        }
                    ],
                    "inlinedType": null
                },
                "x": {
                    "name": "i8",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "y": {
                    "name": "i16",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "z": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "a2": {
                    "name": "i64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "b2": {
                    "name": "f32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                },
                "b3": {
                    "name": "f64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "struct",
            "name": "Option1",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ],
                    "inlinedType": null
                }
            }
        },
        {
            "type": "enum",
            "name": "HeavyNullableTestMode",
            "options": [
                "EntirelyNull",
                "NullList",
                "NullString"
            ]
        }
    ]
}`
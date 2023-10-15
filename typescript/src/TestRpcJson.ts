export const TestRpcJson = `{
    "name": {
        "name": "UserProtocol",
        "isTypeParameter": false,
        "isOptional": false,
        "typeArguments": [
        ]
    },
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
                        ]
                    }
                },
                {
                    "name": "otherThing",
                    "type": {
                        "name": "string",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "CreateLobbyResponse",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                },
                {
                    "name": "shit",
                    "type": {
                        "name": "PlayerId",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "bar",
                    "type": {
                        "name": "void",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "i32",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                },
                {
                    "name": "y",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "string",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                                ]
                            }
                        ]
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
                                        ]
                                    }
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "i64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "PlayerId",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
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
                        ]
                    },
                    {
                        "name": "record",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                            {
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
                                                "name": "PlayerId",
                                                "isTypeParameter": false,
                                                "isOptional": false,
                                                "typeArguments": [
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "f64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    }
                ]
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
                                ]
                            },
                            {
                                "name": "i64",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "string",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    },
                    {
                        "name": "f64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
            }
        },
        {
            "name": "heavyNullable",
            "parameters": [
                {
                    "name": "mode",
                    "type": {
                        "name": "UserProtocol.HeavyNullableTestMode",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
                    }
                ]
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
                        ]
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
                        ]
                    },
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                ]
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
                ]
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
                ]
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
                ]
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
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "void",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                                ]
                            }
                        ]
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
                        ]
                    }
                ]
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
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "EnumArgs",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "TheObject",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicThing",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
            }
        },
        {
            "name": "directPolymorphicAccess",
            "parameters": [
                {
                    "name": "obj",
                    "type": {
                        "name": "PolymorphicThing.Option1",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicThing.Option1",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicClass",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "EveryBuiltinType",
                "isTypeParameter": false,
                "isOptional": false,
                "typeArguments": [
                ]
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
                        ]
                    }
                },
                {
                    "name": "b",
                    "type": {
                        "name": "i8",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "c",
                    "type": {
                        "name": "i16",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "d",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "e",
                    "type": {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "f",
                    "type": {
                        "name": "char",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "g",
                    "type": {
                        "name": "string",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
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
                                ]
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "i32",
                                "isTypeParameter": false,
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    }
                },
                {
                    "name": "r",
                    "type": {
                        "name": "void",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
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
                                ]
                            }
                        ]
                    }
                },
                {
                    "name": "x",
                    "type": {
                        "name": "i8",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "y",
                    "type": {
                        "name": "i16",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "z",
                    "type": {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "a2",
                    "type": {
                        "name": "i64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "b2",
                    "type": {
                        "name": "f32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "b3",
                    "type": {
                        "name": "f64",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
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
                        ]
                    },
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "i32",
                        "isTypeParameter": false,
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                ]
            }
        }
    ],
    "models": [
        {
            "type": "struct",
            "name": "TheObject",
            "typeParameters": [
            ],
            "properties": {
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
                    ]
                },
                "name": {
                    "name": "EnumArgsOptions",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
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
            "type": "struct",
            "name": "Option2",
            "typeParameters": [
            ],
            "properties": {
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
                    ]
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
                    ]
                }
            }
        },
        {
            "type": "union",
            "name": "PolymorphicThing",
            "options": [
                {
                    "name": "PolymorphicThing.Option1",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                {
                    "name": "PolymorphicThing.Option2",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                }
            ],
            "typeParameters": [
            ]
        },
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
                            ]
                        }
                    ]
                },
                "y": {
                    "name": "string",
                    "isTypeParameter": false,
                    "isOptional": true,
                    "typeArguments": [
                    ]
                }
            }
        },
        {
            "type": "union",
            "name": "PolymorphicClass",
            "options": [
                {
                    "name": "PolymorphicClass.Option3",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                {
                    "name": "PolymorphicClass.Option4",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                }
            ],
            "typeParameters": [
            ]
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
            "name": "Option1",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
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
                    ]
                },
                "y": {
                    "name": "T2",
                    "isTypeParameter": true,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "z": {
                    "name": "T3",
                    "isTypeParameter": true,
                    "isOptional": false,
                    "typeArguments": [
                    ]
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
                            ]
                        }
                    ]
                }
            }
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
                    ]
                }
            }
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
                    ]
                },
                "b": {
                    "name": "i8",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "c": {
                    "name": "i16",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "d": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "e": {
                    "name": "i64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "f": {
                    "name": "char",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "g": {
                    "name": "string",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
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
                            ]
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        },
                        {
                            "name": "i32",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
                },
                "r": {
                    "name": "void",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
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
                            ]
                        }
                    ]
                },
                "x": {
                    "name": "i8",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "y": {
                    "name": "i16",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "z": {
                    "name": "i32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "a2": {
                    "name": "i64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "b2": {
                    "name": "f32",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "b3": {
                    "name": "f64",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                }
            }
        }
    ]
}`
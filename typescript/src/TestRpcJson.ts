export const TestRpcJson = `{
    "name": {
        "name": "UserProtocol",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "otherThing",
                    "type": {
                        "name": "String",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "CreateLobbyResponse",
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
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "shit",
                    "type": {
                        "name": "PlayerId",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "bar",
                    "type": {
                        "name": "Unit",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "UInt",
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
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "y",
                    "type": {
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "String",
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
                        "name": "List",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "PlayerId",
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
                        "name": "List",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Set",
                                "isOptional": false,
                                "typeArguments": [
                                    {
                                        "name": "String",
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
                        "name": "Pair",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Long",
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
                        "name": "Triple",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Unit",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "PlayerId",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "String",
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
                        "name": "Map.Entry",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Map",
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "Long",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "Map",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Set",
                                "isOptional": false,
                                "typeArguments": [
                                    {
                                        "name": "List",
                                        "isOptional": false,
                                        "typeArguments": [
                                            {
                                                "name": "PlayerId",
                                                "isOptional": false,
                                                "typeArguments": [
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "Double",
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
                        "name": "Pair",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Long",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Pair",
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "Triple",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "String",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    },
                    {
                        "name": "Double",
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
                        "name": "List",
                        "isOptional": true,
                        "typeArguments": [
                            {
                                "name": "PlayerId",
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
                        "name": "List",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "PlayerId",
                                "isOptional": true,
                                "typeArguments": [
                                ]
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Unit",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "GenericThing",
                "isOptional": true,
                "typeArguments": [
                    {
                        "name": "List",
                        "isOptional": true,
                        "typeArguments": [
                            {
                                "name": "String",
                                "isOptional": true,
                                "typeArguments": [
                                ]
                            }
                        ]
                    },
                    {
                        "name": "List",
                        "isOptional": true,
                        "typeArguments": [
                            {
                                "name": "String",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    },
                    {
                        "name": "List",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "String",
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
                        "name": "String",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "GenericThing",
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "String",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "Long",
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
                "name": "Unit",
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
                "name": "Unit",
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
                "name": "Unit",
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
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Unit",
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
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "String",
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
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "Int",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "EnumArgs",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "TheObject",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicThing",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicThing.Option1",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "PolymorphicClass",
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
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "EveryBuiltinType",
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
                        "name": "Boolean",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "b",
                    "type": {
                        "name": "Byte",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "c",
                    "type": {
                        "name": "Short",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "d",
                    "type": {
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "e",
                    "type": {
                        "name": "Long",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "f",
                    "type": {
                        "name": "Char",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "g",
                    "type": {
                        "name": "String",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "h",
                    "type": {
                        "name": "ByteArray",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "i",
                    "type": {
                        "name": "ShortArray",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "j",
                    "type": {
                        "name": "IntArray",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "k",
                    "type": {
                        "name": "LongArray",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "l",
                    "type": {
                        "name": "CharArray",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                },
                {
                    "name": "m",
                    "type": {
                        "name": "List",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
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
                        "name": "Map",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
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
                        "name": "Set",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
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
                        "name": "Pair",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
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
                        "name": "Triple",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
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
                        "name": "Unit",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Triple",
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "Int",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    }
                ]
            }
        },
        {
            "name": "genericSealed",
            "parameters": [
                {
                    "name": "sealed",
                    "type": {
                        "name": "GenericSealed",
                        "isOptional": false,
                        "typeArguments": [
                            {
                                "name": "String",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            },
                            {
                                "name": "Int",
                                "isOptional": false,
                                "typeArguments": [
                                ]
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "GenericSealed",
                "isOptional": false,
                "typeArguments": [
                    {
                        "name": "String",
                        "isOptional": false,
                        "typeArguments": [
                        ]
                    },
                    {
                        "name": "Int",
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
            "name": "WithNulls",
            "typeParameters": [
                "T"
            ],
            "properties": {
                "x": {
                    "name": "List",
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
                    "name": "String",
                    "isTypeParameter": false,
                    "isOptional": true,
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
                    "name": "Boolean",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "b": {
                    "name": "Byte",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "c": {
                    "name": "Short",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "d": {
                    "name": "Int",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "e": {
                    "name": "Long",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "f": {
                    "name": "Char",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "g": {
                    "name": "String",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "h": {
                    "name": "ByteArray",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "i": {
                    "name": "ShortArray",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "j": {
                    "name": "IntArray",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "k": {
                    "name": "LongArray",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "l": {
                    "name": "CharArray",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                "m": {
                    "name": "List",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
                },
                "n": {
                    "name": "Map",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        },
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
                },
                "o": {
                    "name": "Set",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
                },
                "p": {
                    "name": "Pair",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        },
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
                },
                "q": {
                    "name": "Triple",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        },
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        },
                        {
                            "name": "Int",
                            "isTypeParameter": false,
                            "isOptional": false,
                            "typeArguments": [
                            ]
                        }
                    ]
                },
                "r": {
                    "name": "Unit",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                }
            }
        },
        {
            "type": "struct",
            "name": "EnumArgs",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "Int",
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
            "type": "union",
            "name": "GenericSealed",
            "options": [
                {
                    "name": "GenericSubclass",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                }
            ],
            "typeParameters": [
                "T1",
                "T2"
            ]
        },
        {
            "type": "struct",
            "name": "CreateLobbyResponse",
            "typeParameters": [
            ],
            "properties": {
                "id": {
                    "name": "Long",
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
                    "name": "Long",
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
                    "name": "Option1",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                {
                    "name": "Option2",
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
            "name": "GenericSubclass",
            "typeParameters": [
                "T"
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
            "type": "union",
            "name": "PolymorphicClass",
            "options": [
                {
                    "name": "Option3",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                },
                {
                    "name": "Option4",
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
                    "name": "List",
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
            "name": "Option1",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "Int",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
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
            "name": "Option4",
            "typeParameters": [
            ],
            "properties": {
                "x": {
                    "name": "Int",
                    "isTypeParameter": false,
                    "isOptional": false,
                    "typeArguments": [
                    ]
                }
            }
        }
    ]
}`
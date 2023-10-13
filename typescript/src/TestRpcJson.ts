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
        }
    ],
    "models": [
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
            "name": "CreateLobbyResponse",
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
            "name": "PlayerId",
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
            "type": "struct",
            "name": "Option1",
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
            "type": "union",
            "name": "PolymorphicThing",
            "options": [
                "Option1",
                "Option2"
            ]
        },
        {
            "type": "struct",
            "name": "Option4",
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
            "name": "Option2",
            "properties": {
            }
        },
        {
            "type": "union",
            "name": "PolymorphicClass",
            "options": [
                "Option3",
                "Option4"
            ]
        },
        {
            "type": "struct",
            "name": "EnumArgs",
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
            "type": "struct",
            "name": "Option3",
            "properties": {
            }
        },
        {
            "type": "struct",
            "name": "TheObject",
            "properties": {
            }
        }
    ]
}`
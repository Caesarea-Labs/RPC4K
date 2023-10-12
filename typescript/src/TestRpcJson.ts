export const TestRpcJson = `{
    "name": {
        "name": "UserProtocol"
    },
    "methods": [
        {
            "name": "createLobby",
            "parameters": [
                {
                    "name": "createdBy",
                    "type": {
                        "name": "PlayerId"
                    }
                },
                {
                    "name": "otherThing",
                    "type": {
                        "name": "String"
                    }
                }
            ],
            "returnType": {
                "name": "CreateLobbyResponse"
            }
        },
        {
            "name": "killSomeone",
            "parameters": [
                {
                    "name": "killer",
                    "type": {
                        "name": "Int"
                    }
                },
                {
                    "name": "shit",
                    "type": {
                        "name": "PlayerId"
                    }
                },
                {
                    "name": "bar",
                    "type": {
                        "name": "Unit"
                    }
                }
            ],
            "returnType": {
                "name": "UInt"
            }
        },
        {
            "name": "someShit",
            "parameters": [
                {
                    "name": "x",
                    "type": {
                        "name": "Int"
                    }
                },
                {
                    "name": "y",
                    "type": {
                        "name": "Int"
                    }
                }
            ],
            "returnType": {
                "name": "String"
            }
        },
        {
            "name": "moreTypes",
            "parameters": [
                {
                    "name": "list",
                    "type": {
                        "name": "List",
                        "typeArguments": [
                            {
                                "name": "PlayerId"
                            }
                        ]
                    }
                },
                {
                    "name": "double",
                    "type": {
                        "name": "List",
                        "typeArguments": [
                            {
                                "name": "Set",
                                "typeArguments": [
                                    {
                                        "name": "String"
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
                        "typeArguments": [
                            {
                                "name": "Int"
                            },
                            {
                                "name": "Long"
                            }
                        ]
                    }
                },
                {
                    "name": "triple",
                    "type": {
                        "name": "Triple",
                        "typeArguments": [
                            {
                                "name": "Unit"
                            },
                            {
                                "name": "PlayerId"
                            },
                            {
                                "name": "String"
                            }
                        ]
                    }
                },
                {
                    "name": "entry",
                    "type": {
                        "name": "Map.Entry",
                        "typeArguments": [
                            {
                                "name": "Int"
                            },
                            {
                                "name": "Int"
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Map",
                "typeArguments": [
                    {
                        "name": "Long"
                    },
                    {
                        "name": "Map",
                        "typeArguments": [
                            {
                                "name": "Set",
                                "typeArguments": [
                                    {
                                        "name": "List",
                                        "typeArguments": [
                                            {
                                                "name": "PlayerId"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "Double"
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
                        "typeArguments": [
                            {
                                "name": "Int"
                            },
                            {
                                "name": "Long"
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Pair",
                "typeArguments": [
                    {
                        "name": "Triple",
                        "typeArguments": [
                            {
                                "name": "Int"
                            },
                            {
                                "name": "Int"
                            },
                            {
                                "name": "String"
                            }
                        ]
                    },
                    {
                        "name": "Double"
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
                        "optional": true,
                        "typeArguments": [
                            {
                                "name": "PlayerId"
                            }
                        ]
                    }
                },
                {
                    "name": "mayNull2",
                    "type": {
                        "name": "List",
                        "typeArguments": [
                            {
                                "name": "PlayerId",
                                "optional": true
                            }
                        ]
                    }
                }
            ],
            "returnType": {
                "name": "Unit"
            }
        },
        {
            "name": "heavyNullable",
            "parameters": [
                {
                    "name": "mode",
                    "type": {
                        "name": "UserProtocol.HeavyNullableTestMode"
                    }
                }
            ],
            "returnType": {
                "name": "GenericThing",
                "optional": true,
                "typeArguments": [
                    {
                        "name": "List",
                        "optional": true,
                        "typeArguments": [
                            {
                                "name": "String",
                                "optional": true
                            }
                        ]
                    },
                    {
                        "name": "List",
                        "optional": true,
                        "typeArguments": [
                            {
                                "name": "String"
                            }
                        ]
                    },
                    {
                        "name": "List",
                        "typeArguments": [
                            {
                                "name": "String",
                                "optional": true
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
                        "name": "String"
                    }
                }
            ],
            "returnType": {
                "name": "GenericThing",
                "typeArguments": [
                    {
                        "name": "String"
                    },
                    {
                        "name": "Int"
                    },
                    {
                        "name": "Long"
                    }
                ]
            }
        },
        {
            "name": "errorTest",
            "parameters": [
            ],
            "returnType": {
                "name": "Unit"
            }
        },
        {
            "name": "requirementTest",
            "parameters": [
            ],
            "returnType": {
                "name": "Unit"
            }
        },
        {
            "name": "noArgTest",
            "parameters": [
            ],
            "returnType": {
                "name": "Unit"
            }
        },
        {
            "name": "requirementFail",
            "parameters": [
                {
                    "name": "value",
                    "type": {
                        "name": "Int"
                    }
                }
            ],
            "returnType": {
                "name": "Unit"
            }
        }
    ],
    "models": [
        {
            "name": "PlayerId",
            "properties": {
                "num": {
                    "name": "Long"
                }
            }
        },
        {
            "name": "GenericThing",
            "typeParameters": [
                "T1",
                "T2",
                "T3"
            ],
            "properties": {
                "x": {
                    "name": "T1",
                    "isTypeParameter": true
                },
                "y": {
                    "name": "T2",
                    "isTypeParameter": true
                },
                "z": {
                    "name": "T3",
                    "isTypeParameter": true
                },
                "w": {
                    "name": "List",
                    "typeArguments": [
                        {
                            "name": "T3",
                            "isTypeParameter": true
                        }
                    ]
                }
            }
        },
        {
            "name": "HeavyNullableTestMode",
            "properties": {
                "name": {
                    "name": "String"
                },
                "ordinal": {
                    "name": "Int"
                }
            }
        },
        {
            "name": "CreateLobbyResponse",
            "properties": {
                "id": {
                    "name": "Long"
                }
            }
        }
    ]
}`
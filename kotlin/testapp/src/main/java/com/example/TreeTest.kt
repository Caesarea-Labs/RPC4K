package com.example

import kotlinx.serialization.Serializable

@Serializable
data class TreeTest<T>(val item: T, val children: List<TreeTest<T>>)
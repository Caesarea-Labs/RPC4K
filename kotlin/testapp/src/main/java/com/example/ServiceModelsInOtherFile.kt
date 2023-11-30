package com.example

import kotlinx.serialization.Serializable

@Serializable
data class ServiceInOtherFile1(val x: Int): ServiceSealedInterfaceInOtherFile
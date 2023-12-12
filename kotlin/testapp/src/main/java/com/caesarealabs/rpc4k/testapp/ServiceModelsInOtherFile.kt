package com.caesarealabs.rpc4k.testapp

import kotlinx.serialization.Serializable

@Serializable
data class ServiceInOtherFile1(val x: Int): ServiceSealedInterfaceInOtherFile
@Serializable
data class ServiceInOtherFile2(val x: Int, val y: ServiceInOtherFile1): ServiceSealedInterfaceInOtherFile


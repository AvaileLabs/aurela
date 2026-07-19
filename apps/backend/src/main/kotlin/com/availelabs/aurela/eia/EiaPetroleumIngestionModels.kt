package com.availelabs.aurela.eia

import tools.jackson.databind.node.ObjectNode

sealed interface EiaPetroleumRecord {
    val jsonObject: ObjectNode
    data class Series(override val jsonObject: ObjectNode) : EiaPetroleumRecord
    data class Category(override val jsonObject: ObjectNode) : EiaPetroleumRecord
    data class Unrecognized(override val jsonObject: ObjectNode) : EiaPetroleumRecord
}
package com.availelabs.aurela.eia

import tools.jackson.databind.node.ObjectNode

/**
 * A record from an EIA petroleum bulk-data file.
 *
 * @property jsonObject unmodified JSON object supplied by EIA
 */
sealed interface EiaPetroleumRecord {
    /** Unmodified JSON object supplied by EIA. */
    val jsonObject: ObjectNode

    /** A recognized series record containing a `series_id` field. */
    data class Series(override val jsonObject: ObjectNode) : EiaPetroleumRecord

    /** A recognized category record containing a `category_id` field. */
    data class Category(override val jsonObject: ObjectNode) : EiaPetroleumRecord

    /** A JSON object that has neither a `series_id` nor a `category_id` field. */
    data class Unrecognized(override val jsonObject: ObjectNode) : EiaPetroleumRecord
}
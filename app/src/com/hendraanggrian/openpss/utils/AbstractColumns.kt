@file:Suppress("NOTHING_TO_INLINE")

package com.hendraanggrian.openpss.utils

import kotlinx.nosql.AbstractColumn
import kotlinx.nosql.AbstractSchema
import kotlinx.nosql.Query

inline fun <T : AbstractSchema, C> AbstractColumn<out C?, T, *>.matches(
    regex: String,
    flags: Int = 0
): Query = matches(regex.toPattern(flags))
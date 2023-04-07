package net.horizonsend.ion.common.database

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.lowerCase

@Suppress("UNCHECKED_CAST")
infix fun <T: String?> Expression<T>.leq(value: String): Op<Boolean> = lowerCase() eq value.lowercase() as T

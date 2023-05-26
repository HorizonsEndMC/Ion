package net.horizonsend.ion.common.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.vendors.currentDialect

data class DBLocation(val world: String, val coords: DoubleLocation)
typealias IntLocation = Triple<Int, Int, Int>
typealias DoubleLocation = Triple<Double, Double, Double>

@Suppress("UNCHECKED_CAST")
infix fun <T : String?> Expression<T>.leq(value: String): Op<Boolean> = lowerCase() eq value.lowercase() as T

fun Table.location(name: String = "location"): Column<DBLocation> = registerColumn(name, LocationColumnType())

class LocationColumnType : ColumnType() {
	override fun sqlType(): String = currentDialect.dataTypeProvider.varcharType(60)

	override fun valueFromDB(value: Any): DBLocation = when (value) {
		is DBLocation -> value
		is String ->
			value.split(";").run {
				DBLocation(
					this[0], // World
					DoubleLocation(
						this[1].toDouble(), // X
						this[2].toDouble(), // Y
						this[3].toDouble() // Z
					)
				)
			}

		else -> error("Unexpected value of type DBLocation: $value of ${value::class.qualifiedName}")
	}

	override fun valueToDB(value: Any?): String = when (value) {
		is DBLocation -> "${value.world};${value.coords.first};${value.coords.second};${value.coords.third};"

		null -> error("Cannot save a null location!")
		else -> error("Unexpected value of type DBLocation: $value of ${value::class.qualifiedName}")
	}
}

package net.horizonsend.ion.server.features.data.migrator

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.data.migrator.types.CustomItemStackMigrator
import net.horizonsend.ion.server.features.data.migrator.types.DataMigrator
import net.horizonsend.ion.server.features.data.migrator.types.WorldDataMigrator

object DataMigrators : IonServerComponent() {
	override fun onEnable() {
		registerMigrators()
	}

	private fun registerMigrators() {


		worldMigrators.sort()
		customItemMigrators.forEach { it.value.sort() }
	}

	private val worldMigrators = mutableListOf<WorldDataMigrator>()
	private val customItemMigrators = mutableMapOf<String, MutableList<CustomItemStackMigrator>>()

	private fun registerMigrator(migrator: DataMigrator<*, *>) {
		when (migrator) {
			is CustomItemStackMigrator -> customItemMigrators.getOrPut(migrator.customItemIdentifier) { mutableListOf() }.add(migrator)
			is WorldDataMigrator -> worldMigrators.add(migrator)
		}
	}
}

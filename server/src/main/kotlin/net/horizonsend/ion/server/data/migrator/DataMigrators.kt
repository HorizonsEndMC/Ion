package net.horizonsend.ion.server.data.migrator

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.data.migrator.types.item.migrator.AspectMigrator
import net.horizonsend.ion.server.data.migrator.types.item.predicate.CustomItemPredicate
import net.horizonsend.ion.server.features.custom.CustomItemRegistry
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataType

object DataMigrators : IonServerComponent() {
	override fun onEnable() {
		registerDataVersions()
	}

	private val dataVersions = mutableListOf<DataVersion>()
	private val lastDataVersion get() = dataVersions.lastIndex

	private fun registerDataVersions() {
		registerDataVersion(DataVersion.builder(0).build()) // To align to index

		registerDataVersion(
			DataVersion
				.builder(1)
				.addMigrator(AspectMigrator
					.builder(CustomItemPredicate(CustomItemRegistry.CANNON.identifier), CustomItemRegistry.CANNON)
					.setItemMaterial(Material.SNOW)
					.setCustomName(Component.text("Test"))
					.build())
				.build()
		)
	}

	private fun registerDataVersion(dataVersion: DataVersion) {
		dataVersions.add(dataVersion)
	}

	fun migrate(chunk: Chunk) {
		val chunkVersion = chunk.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (chunkVersion == lastDataVersion) return

		val nextVersion = minOf(chunkVersion + 1, lastDataVersion)
		val toApply = dataVersions.subList(nextVersion, lastDataVersion)

		val snapshot = chunk.chunkSnapshot

		for (x in 0..15) for (y in chunk.world.minHeight..chunk.world.maxHeight) for (z in 0..15) {
			val type = snapshot.getBlockType(x, y, z)
			if (Pipes.isPipedInventory(type)) {
				val state = chunk.getBlock(x, y, z).state as InventoryHolder
				migrateInventory(state.inventory, toApply)
			}
		}
	}

	fun migrate(player: Player) {
		val playerVersion = player.persistentDataContainer.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)
		if (playerVersion == lastDataVersion) return

		val nextVersion = minOf(playerVersion + 1, lastDataVersion)
		val toApply = dataVersions.subList(nextVersion, lastDataVersion)

		migrateInventory(player.inventory, toApply)
	}

	private fun migrateInventory(inventory: Inventory, versions: List<DataVersion>) {
		for (dataVersion in versions) {
			dataVersion.migrateInventory(inventory, dataVersion.versionNumber)
		}
	}
}

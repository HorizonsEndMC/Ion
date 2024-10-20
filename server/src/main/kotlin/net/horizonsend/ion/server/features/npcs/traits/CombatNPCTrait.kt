package net.horizonsend.ion.server.features.npcs.traits

import net.citizensnpcs.api.persistence.DelegatePersistence
import net.citizensnpcs.api.persistence.ItemStackPersister
import net.citizensnpcs.api.persistence.Persist
import net.citizensnpcs.api.persistence.Persister
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import net.citizensnpcs.api.util.DataKey
import net.horizonsend.ion.server.IonServer
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.UUID

@TraitName("ioncombatnpc")
class CombatNPCTrait : Trait("ioncombatnpc") {
	@Persist
	var created: Long = System.currentTimeMillis()

	@Persist
	lateinit var owner: UUID

	@DelegatePersistence(InventoryPersister::class)
	lateinit var inventoryContents: Array<ItemStack?>

	@Persist
	var despawnTime: Long = 0

	@Persist
	var wasInCombat: Boolean = false

	override fun run() {
		if (!::owner.isInitialized) return

		if (Bukkit.getPlayer(owner) != null) return destroy()
		if (isExpired()) return destroy()
	}

	fun isExpired() = System.currentTimeMillis() > despawnTime

	private fun destroy() {
		if (npc.isSpawned) {
			npc.entity.remove()
			npc.entity.location.chunk.removePluginChunkTicket(IonServer)
		}

		npc.destroy()
	}

	class InventoryPersister : Persister<Array<ItemStack?>> {
		companion object {
			private val itemStackPersister = ItemStackPersister()
		}

		override fun create(root: DataKey): Array<ItemStack?>? {
			val size = root.getInt("size", 0)

			if (size == 0) return null

			var index = 0
			val items = root.getString("items").split(",").map {
				val item = itemStackPersister.create(root.getRelative("item$index"))
				index++

				item
			}

			return Array(size) {
				items[it]
			}
		}

		override fun save(instance: Array<ItemStack?>, root: DataKey) {
			root.setInt("size", instance.size)

			for ((index, item) in instance.withIndex()) {
				itemStackPersister.save(item, root.getRelative("item$index"))
			}
		}
	}
}

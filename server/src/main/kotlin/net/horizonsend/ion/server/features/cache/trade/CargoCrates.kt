package net.horizonsend.ion.server.features.cache.trade

import com.google.gson.Gson
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.CargoCrate
import net.starlegacy.feature.economy.cargotrade.CrateItems
import net.starlegacy.feature.economy.cargotrade.ShipmentManager
import net.horizonsend.ion.common.utils.redisaction.RedisAction
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.`in`
import org.litote.kmongo.nin
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import java.io.File
import java.io.FileReader
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object CargoCrates : ManualCache() {
	private lateinit var refreshGlobal: RedisAction<Long>

	override fun load() {
		refreshGlobal = RedisActions.register("trade-reload-all-crate-data", runSync = false) { time: Long ->
			log.info("Received Reload Request Initiated At ${Date(time).toInstant().atZone(ZoneId.systemDefault())}")
			refreshLocal()
		}

		refreshLocal()
	}

	private fun refreshLocal() {
		crates = CargoCrate.all()

		CrateItems.invalidateAll()

		ITEM_MAP = crates.associateBy {
			(Material.valueOf(it.color.shulkerMaterial) to CrateItems[it].itemMeta!!.displayName)
		}

		NAME_MAP = crates.associateBy {
			it.name.uppercase(Locale.getDefault())
		}

		ID_MAP = crates.associateBy {
			it._id
		}

		// state of crates has changed, so regenerate shipments
		ShipmentManager.regenerateShipmentsAsync()
	}

	private const val fileName = "crateData.json"

	private class CrateListWrapper(val crates: List<CargoCrate>)

	var crates = listOf<CargoCrate>()

	/**
	 * Loads cargo crate data from file if it exists.
	 * If there is data to load, it parses crates, then clears old crates, then imports supplied crates.
	 * If there is any data attached to crates that are removed, e.g. shipments, it also disposes of that.
	 *
	 * @return True if there was a file to load, else false
	 */
	internal fun reloadData() {
		import()
		CargoCrates.refreshGlobal(System.currentTimeMillis())
	}

	private fun import() {
		val file = File(IonServer.dataFolder, fileName).takeIf(File::exists) ?: return

		val data = FileReader(file).use { Gson().fromJson(it, CrateListWrapper::class.java) }

		val oldCrates = CargoCrate.all()
		val newNames = data.crates.map { it.name }
		val newCrates = data.crates.associate { it.name to it }

		// remove crates that are not in the imported list
		for (removed in CargoCrate.find(CargoCrate::name nin newNames).toList()) {
			CargoCrate.delete(removed._id)
		}

		// used for storing ids from both updated and adding
		val idMap = mutableMapOf<String, Oid<CargoCrate>>()

		// update crates that already exist
		for (crate in CargoCrate.find(CargoCrate::name `in` newNames)) {
			val name = crate.name
			val id = crate._id
			idMap[name] = id

			val newCrate = checkNotNull(newCrates[name]) { "Missing map entry for crate $name" }
			val newColor = newCrate.color
			val newValues = newCrate.values
			CargoCrate.updateById(id, set(CargoCrate::color setTo newColor, CargoCrate::values setTo newValues))
		}

		// add crates that didn't exist before
		for (name in newNames.filter { name -> oldCrates.none { it.name == name } }) {
			val newCrate = checkNotNull(newCrates[name]) { "Missing map entry for crate $name" }

			idMap[name] = CargoCrate.create(name, newCrate.color, newCrate.values)
		}

		file.delete()
	}

	//region CACHES
	private lateinit var ITEM_MAP: Map<Pair<Material, String>, CargoCrate>
	private lateinit var NAME_MAP: Map<String, CargoCrate>
	private lateinit var ID_MAP: Map<Oid<CargoCrate>, CargoCrate>

	operator fun get(itemStack: ItemStack?): CargoCrate? {
		val type = itemStack?.type ?: return null
		val displayName = itemStack.itemMeta?.displayName ?: return null

		return ITEM_MAP[type to displayName]
	}

	operator fun get(box: ShulkerBox): CargoCrate? = ITEM_MAP[box.type to box.customName]

	operator fun get(name: String?): CargoCrate? = NAME_MAP[name?.uppercase(Locale.getDefault())]

	operator fun get(id: Oid<CargoCrate>): CargoCrate = ID_MAP[id] ?: error("Crate $id not cached!")
	//endregion CACHES
}

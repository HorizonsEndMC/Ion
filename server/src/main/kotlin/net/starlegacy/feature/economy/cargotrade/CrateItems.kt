package net.starlegacy.feature.economy.cargotrade

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.CargoCrate
import net.horizonsend.ion.server.miscellaneous.registrations.updateMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CrateItems {
	private val crateItemTemplates: Cache<Oid<CargoCrate>, ItemStack> = CacheBuilder.newBuilder().build()

	fun invalidateAll(): Unit = crateItemTemplates.invalidateAll()

	// this is terribly formatted but i just wanted to use brackets that way *so bad* :3
	operator fun get(crate: CargoCrate): ItemStack = crateItemTemplates[
		crate._id, {
			ItemStack(Material.valueOf(crate.color.shulkerMaterial), 1).updateMeta { meta ->
				meta.setDisplayName("${crate.color.legacyChatColor}${crate.name}${ChatColor.RESET} Cargo Crate")
			}.ensureServerConversions()
		}
	]
}

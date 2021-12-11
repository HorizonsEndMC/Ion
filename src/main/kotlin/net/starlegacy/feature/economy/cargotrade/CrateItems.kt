package net.starlegacy.feature.economy.cargotrade

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.economy.CargoCrate
import net.starlegacy.util.updateMeta
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack

object CrateItems {
    private val crateItemTemplates: Cache<Oid<CargoCrate>, ItemStack> = CacheBuilder.newBuilder().build()

    fun invalidateAll(): Unit = crateItemTemplates.invalidateAll()

    // this is terribly formatted but i just wanted to use brackets that way *so bad* :3
    operator fun get(crate: CargoCrate): ItemStack = crateItemTemplates[crate._id, {
        ItemStack(crate.color.shulkerMaterial, 1).updateMeta { meta ->
            meta.setDisplayName("${crate.color.chatColor}${crate.name}${ChatColor.RESET} Cargo Crate")
        }.ensureServerConversions()
    }]
}

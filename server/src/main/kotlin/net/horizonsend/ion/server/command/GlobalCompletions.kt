package net.horizonsend.ion.server.command

import co.aikar.commands.PaperCommandManager
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.Optional

object GlobalCompletions {
	fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(AnyItem::class.java) {
			fromItemString(it.popFirstArg())
		}
		manager.commandCompletions.registerAsyncCompletion( "anyItem") { Bazaars.strings }
		manager.commandCompletions.setDefaultCompletion("anyItem", AnyItem::class.java)

		manager.commandContexts.registerContext(SLPlayer::class.java) {
			SLPlayer[it.popFirstArg()]
		}
		manager.commandCompletions.registerAsyncCompletion("allPlayers") { SLPlayer.findProp(property =  SLPlayer::lastKnownName).toList() }
		manager.commandCompletions.setDefaultCompletion("allPlayers", SLPlayer::class.java)

		manager.commandCompletions.registerAsyncCompletion("anyBlock") { Material.entries.filter { it.isBlock && !it.isLegacy }.map { it.name } }
		manager.commandCompletions.registerAsyncCompletion("chatChannel") { ChatChannel.entries.map { it.name.lowercase() } }
	}

	fun toItemString(item: ItemStack): String {
		return item.customItem?.getBazaarString(item) ?: item.type.toString()
	}

	val stringItemCache: LoadingCache<String, Optional<ItemStack>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { string -> Optional.ofNullable(stringToItem(string)) }
	)

	fun fromItemString(string: String): ItemStack = stringItemCache[string].get().clone()

	fun stringToItem(string: String): ItemStack? {
		// if a custom item is found, use that
		CustomItemKeys[string.substringBefore('[')]?.let { return it.getValue().fromBazaarString(string) }

		val material: Material = try { Material.valueOf(string) } catch (e: Throwable) { return null }

		if (!material.isItem) return null
		return ItemStack(material, 1)
	}
}

typealias AnyItem = ItemStack

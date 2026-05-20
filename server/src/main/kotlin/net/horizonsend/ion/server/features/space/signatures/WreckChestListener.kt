package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

object WreckChestListener : IonServerComponent() {

	override fun onEnable() {

		listen<BlockBreakEvent> { event ->
			val chest = event.block.state as? Chest ?: return@listen
			if (!chest.persistentDataContainer.has(NamespacedKeys.WRECK_CHEST, PersistentDataType.BOOLEAN)) return@listen
			event.isCancelled = true
			event.player.sendMessage(net.kyori.adventure.text.Component.text("This wreck chest cannot be broken!", org.bukkit.ChatColor.RED.asBungee().let { net.kyori.adventure.text.format.NamedTextColor.RED }))
		}

		listen<PlayerInteractEvent> { event ->
			if (event.action != Action.RIGHT_CLICK_BLOCK) return@listen
			val block = event.clickedBlock ?: return@listen
			if (block.type != Material.CHEST) return@listen
			val chest = block.state as? Chest ?: return@listen
			val isWreckChest = chest.persistentDataContainer.get(NamespacedKeys.WRECK_CHEST, PersistentDataType.BOOLEAN) ?: return@listen

			event.isCancelled = true

			// check if locked
			if (!isWreckChest) {
				event.player.sendMessage(text("This system has been permanently locked out.", NamedTextColor.RED))
				return@listen
			}

			val locked = chest.persistentDataContainer.get(NamespacedKeys.WRECK_CHEST_LOCKED, PersistentDataType.BOOLEAN) ?: true

			if (locked) {
				WreckHackingGui(event.player, chest).openGui()
			} else {
				// unlocked so open
				event.isCancelled = false
			}
		}
	}

	fun unlockChest(chest: Chest, player: Player) {
		chest.persistentDataContainer.set(NamespacedKeys.WRECK_CHEST_LOCKED, PersistentDataType.BOOLEAN, false)

		// fill with rewards
		val inventory = chest.inventory
		inventory.clear()

		inventory.addItem(CustomItemKeys.DATA_CHIP.getValue().constructItemStack().apply {
			amount = Random.nextInt(1, 21)
		})
		inventory.addItem(CustomItemKeys.GUIDANCE_SYSTEM.getValue().constructItemStack().apply {
			amount = Random.nextInt(1, 21)
		})
		inventory.addItem(CustomItemKeys.SUPERCONDUCTOR.getValue().constructItemStack().apply {
			amount = Random.nextInt(1, 21)
		})

		chest.update()

		player.sendMessage(text("Hacking successful! The chest has been unlocked.", NamedTextColor.GREEN))
	}
}

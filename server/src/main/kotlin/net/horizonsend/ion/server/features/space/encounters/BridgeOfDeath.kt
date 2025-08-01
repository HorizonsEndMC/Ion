package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.InputValidator
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.castSpawnEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.CompoundTag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.entity.ZombieVillager
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import java.util.Random

object BridgeOfDeath : Encounter(identifier = "bridge_of_death") {
	private fun checkAir(block: Block): Boolean {
		val up1 = block.getRelative(BlockFace.UP)
		val up2 = up1.getRelative(BlockFace.UP)

		return up1.isEmpty && up2.isEmpty
	}

	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		val chest = (targetedBlock.state as? Chest) ?: return

		if (Encounters.getChestFlag(chest, INACTIVE) == "true") {
			event.isCancelled = true
			return
		}

		val blocks = Encounters.getBlocks(chest.world, Vec3i(chest.location), 3.0) {
			checkAir(it)
		}

		val block = blocks.shuffled().first().location
		val bridgeKeeper =
			block.world.castSpawnEntity<ZombieVillager>(block.toCenterLocation(), EntityType.ZOMBIE_VILLAGER).apply {
				this.customName(text("Old Man From Scene 24"))
				this.lookAt(event.player)
				this.setAI(false)
			}

		event.player.sendRichMessage("<bold><dark_red>Stop!")
		event.player.sendRichMessage("<bold><red>Who would cross the Bridge of Death must answer me these questions three, <italic>ere the other side he see!")

		Encounters.setChestFlag(chest, LOCKED, "true")
		event.isCancelled = true

		var failed = false
		fun fail() {
			Notify.chatAndGlobal(text("${event.player.name}: \"Aaaaaaaaaaaahhhhhh!\""))

			event.player.world.castSpawnEntity<Fireball>(
				event.player.location.clone().add(0.0, 30.0, 0.0),
				EntityType.FIREBALL
			).apply {
				this.direction = Vector(0.0, -2.0, 0.0)
				this.yield = 25f
				this.shooter = bridgeKeeper
				bridgeKeeper.setAI(true)
			}

			failed = true
		}

		event.player.openInputMenu(
			prompt = "What is your name?".toComponent(),
			inputValidator = InputValidator { ValidatorResult.ValidatorSuccessSingleEntry(it) }
		) { _, response ->
			if (response.result != event.player.name) return@openInputMenu fail()
		}

		if (failed) return

		event.player.openInputMenu(
			prompt = "What is your quest?".toComponent(),
			inputValidator = InputValidator { ValidatorResult.ValidatorSuccessSingleEntry(it) }
		) { _, _ -> }

		var promptNation: NationCache.NationData? = null
		val (id, prompt) = when (Random(System.currentTimeMillis()).nextInt(0, 2)) {
			0 -> 0 to "What is your favorite color?"
			1 -> {
				promptNation = NationCache.all().randomOrNull()
				1 to "What is the capital of ${promptNation?.name}"
			}

			2 -> 2 to "When was the Star Legacy reset released?"

			else -> 0 to "What is your favorite color?"
		}

		event.player.openInputMenu(prompt.toComponent(), inputValidator = InputValidator { ValidatorResult.ValidatorSuccessSingleEntry(it) }) { _, validator ->
			val answer = validator.result
			when (id) {
				2 ->
					if (answer.contains("august", true) || answer.contains("2021", true)) return@openInputMenu
				1 -> {
					val data = promptNation ?: return@openInputMenu
					val settlementName = SettlementCache[data.capital].name

					if (answer.contains(settlementName, true)) return@openInputMenu
				}

				0 -> {
					val filteredSpace = answer.substringBefore(" ")

					val names = NamedTextColor.NAMES.keys().map { it.lowercase() }
					if (names.any { it.contains(filteredSpace, true) }) return@openInputMenu
				}
			}

			fail()
			null
		}

		if (failed) return

		Encounters.setChestFlag(chest, LOCKED, "false")
		Encounters.setChestFlag(chest, INACTIVE, "true")
		bridgeKeeper.remove()
		event.player.sendRichMessage("Alright, off you go.")
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/power_armor_mods")
	}
}

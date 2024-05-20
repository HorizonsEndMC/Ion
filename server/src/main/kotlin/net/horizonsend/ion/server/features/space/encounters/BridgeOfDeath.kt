package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.nations.gui.anvilInput
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.castSpawnEntity
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

		event.player.anvilInput("What is your name?".toComponent()) { player, response ->
			if (response == player.name) return@anvilInput null
			fail()

			null
		}

		if (failed) return

		event.player.anvilInput("What is your quest?".toComponent()) { _, _ ->
			null
		}

		var promptNation: NationCache.NationData? = null
		val (id, prompt) =
			when (Random(System.currentTimeMillis()).nextInt(0, 2)) {
				0 -> 0 to "What is your favorite color?"
				1 -> {
					promptNation = NationCache.all().randomOrNull()
					1 to "What is the capital of ${promptNation?.name}"
				}

				2 -> 2 to "When was the Star Legacy reset released?"

				else -> 0 to "What is your favorite color?"
			}

		event.player.anvilInput(prompt.toComponent()) { _, answer ->
			when (id) {
				2 ->
					if (answer.contains("august", true) || answer.contains("2022", true))
						return@anvilInput null

				1 -> {
					val data = promptNation ?: return@anvilInput null
					val settlementName = SettlementCache[data.capital].name

					if (answer.contains(settlementName, true))
						return@anvilInput null
				}

				0 -> {
					val filteredSpace = answer.substringBefore(" ")

					val names = NamedTextColor.NAMES.keys().map { it.lowercase() }
					if (names.any { it.contains(filteredSpace, true) })
						return@anvilInput null
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

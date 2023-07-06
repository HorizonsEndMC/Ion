package net.horizonsend.ion.server.features.space.encounters

import java.util.Random
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.castSpawnEntity
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.CompoundTag
import net.starlegacy.cache.nations.SettlementCache
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.starlegacy.util.Notify
import net.starlegacy.util.toBlockPos
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.MessagePrompt
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.entity.ZombieVillager
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import net.starlegacy.cache.nations.NationCache

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

		val blocks = Encounters.getBlocks(chest.world, chest.location.toBlockPos(), 3.0) {
			checkAir(it)
		}

		val block = blocks.shuffled().first().location
		val bridgeKeeper = block.world.castSpawnEntity<ZombieVillager>(block.toCenterLocation(), EntityType.ZOMBIE_VILLAGER).apply {
			this.customName(text("Old Man From Scene 24"))
			this.lookAt(event.player)
			this.setAI(false)
		}

		event.player.sendRichMessage("<bold><dark_red>Stop!")
		event.player.sendRichMessage("<bold><red>Who would cross the Bridge of Death must answer me these questions three, <italic>ere the other side he see!")

		Encounters.setChestFlag(chest, LOCKED, "true")
		event.isCancelled = true

		fun fail() {
			Notify.online(text("${event.player.name}: \"Aaaaaaaaaaaahhhhhh!\""))

			event.player.world.castSpawnEntity<Fireball>(
				event.player.location.clone().add(0.0, 30.0, 0.0),
				EntityType.FIREBALL
			).apply {
				this.direction = Vector(0.0, -2.0, 0.0)
				this.yield = 25f
				this.shooter = bridgeKeeper
				bridgeKeeper.setAI(true)
			}
		}

		val finalStatement = object: MessagePrompt() {
			override fun getPromptText(context: ConversationContext): String {
				Encounters.setChestFlag(chest, LOCKED, "false")
				Encounters.setChestFlag(chest, INACTIVE, "true")
				bridgeKeeper.remove()
				return "Alright, off you go."
			}

			override fun getNextPrompt(context: ConversationContext): Prompt? {
				return null
			}
		}

		val thirdQuestion = object: StringPrompt() {
			override fun getPromptText(context: ConversationContext): String {

				when (Random(System.currentTimeMillis()).nextInt(0, 2)) {
					0 -> {
						context.allSessionData["third"] = "color"
						return "What is your favorite color?"
					}
					1 -> {
						val nation = NationCache.all().randomOrNull()

						context.allSessionData["third"] = "nation"
						context.allSessionData["nation"] = nation

						return "What is the capital of ${nation?.name}"
					}
					2 -> {
						context.allSessionData["third"] = "reset"
						return "When was the Star Legacy reset released?"
					}
				}

				context.allSessionData["third"] = "color"
				return "What is your favorite color?"
			}

			override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
				if (input != null) {
					when (context.allSessionData["third"] as? String) {
						"reset" -> {
							if (input.contains("august", true) || input.contains("2022", true)) {
								return finalStatement
							} else {
								fail()
							}
						}
						"nation" -> {
							val data = context.allSessionData["nation"] as? Nation ?: return finalStatement
							val settlementName = SettlementCache[data.capital as Oid<Settlement>].name

							if (input.contains(settlementName, true)) {
								return finalStatement
							} else {
								fail()
							}
						}
						"color" -> {
							val filteredSpace = input.substringBefore(" ")

							val names = NamedTextColor.NAMES.keys().map { it.lowercase() }
							if (names.any { it.contains(filteredSpace, true) }) {
								return finalStatement
							} else {
								fail()
							}
						}
					}
				}
				return null
			}
		}

		val secondQuestion = object: StringPrompt() {
			override fun getPromptText(context: ConversationContext): String {
				return "What is your quest?"
			}

			@Suppress()
			override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
				return thirdQuestion
			}
		}

		val firstQuestion = object: StringPrompt() {
			override fun getPromptText(context: ConversationContext): String {
				return "What is your name?"
			}

			override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
				if (input == event.player.name) return secondQuestion

				fail()

				return null
			}
		}

		event.player.beginConversation(
			Conversation(
				IonServer,
				event.player,
				firstQuestion
			)
		)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/power_armor_mods")
	}
}

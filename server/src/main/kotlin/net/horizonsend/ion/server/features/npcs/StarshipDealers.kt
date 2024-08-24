package net.horizonsend.ion.server.features.npcs

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry
import net.citizensnpcs.trait.LookClose
import net.citizensnpcs.trait.SkinTrait
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.configuration.UUIDSerializer
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.starship.BlueprintCommand
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.nations.gui.item
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.ItemStack
import java.lang.System.currentTimeMillis
import java.util.UUID

object StarshipDealers : NPCFeature() {
	private val lastBuyTimes = mutableMapOf<ServerConfiguration.Ship, MutableMap<UUID, Long>>()
	val schematicMap = IonServer.configuration.soldShips.associateWith { it.schematic() }

	val store = JsonNPCStore<ShipDealer>(this)

	override fun onEnable() {
		setupRegistry()

		store.loadNPCs()
	}

	override fun onDisable() {
		disableRegistry()
	}

	@Serializable
	data class ShipDealer(
		override val position: ServerConfiguration.Pos,
		@Serializable(with = UUIDSerializer::class) override val uuid: UUID,
		val skinName: String,
		val skinSignature: String,
		val skinValue: String
	) : JsonNPCStore.NPC {
		override fun createNPC(registry: NPCRegistry, index: Int): NPC {
			val npc = registry.createNPC(
				EntityType.PLAYER,
				uuid,
				2000 + index,
				legacyAmpersand().serialize(text("Starship Dealer", GOLD))
			)

			val location = position.toLocation()

			spawnNPCAsync(
				npc = npc,
				world = location.world,
				location = location,
				spawn = {
					npc.getOrAddTrait(SkinTrait::class.java).apply {
						setSkinPersistent(this@ShipDealer.skinName, skinSignature, skinValue)
					}

					npc.getOrAddTrait(LookClose::class.java).apply {
						lookClose(true)
						setRealisticLooking(true)
					}

					npc.isProtected = true

					npc.spawn(location)
				}
			)

			return npc
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onClickNPC(event: NPCRightClickEvent) {
		val npc = event.npc
		val player = event.clicker

		if (!npcRegistry.contains(npc)) {
			return
		}

		MenuHelper.apply {
			val ships: List<GuiItem> = schematicMap.map { (ship, schematic) ->
				val item: ItemStack = item(ship.guiMaterial)

				item.updateMeta {
					it.displayName(miniMessage().deserialize(ship.displayName))

					it.lore(
						ship.lore.map {  loreLine ->
							miniMessage().deserialize(loreLine)
						}
					)
				}

				val button = guiButton(item) {
					loadShip(player, ship, schematic)
				}

				button.setName(miniMessage().deserialize(ship.displayName))

				return@map button
			}

			player.openPaginatedMenu("Ship Dealer", ships)
		}
	}

	private fun loadShip(player: Player, ship: ServerConfiguration.Ship, schematic: Clipboard) {
		val shipLastBuy = lastBuyTimes.getOrDefault(ship, mutableMapOf())

		if (shipLastBuy.getOrDefault(player.uniqueId, 0) + (ship.cooldown) > currentTimeMillis()) {
			if (player.hasProtection() && ship.protectionCanBypass) {
				player.information("You seem new around these parts. I usually don't do this, but I'll let you take another")
			} else {
				player.userError("Didn't I sell you a ship not too long ago? These things are expensive, " +
							"and I am already selling them at a discount, leave some for other people.")
				return
			}
		}

		if (Levels[player] < ship.shipType.minLevel) {
			player.userError("You are not a high enough level to pilot that ship!")
			return
		}

		if (!player.hasEnoughMoney(ship.price)) {
			player.userError("This ship is too expensive for you\n It costs ${ship.price}, you currently have ${player.getMoneyBalance()}")
			return
		}

		var target = player.location
		target.y = 216.0
		target = resolveTarget(schematic, target)

		val world = player.world
		val targetVec3i = Vec3i(target)

		placeSchematicEfficiently(schematic, world, targetVec3i, true) {
			val vec3i = Vec3i(target.blockX, target.blockY, target.blockZ)
			player.teleport(target.add(0.0, 1.0, 0.0).toCenterLocation())

			player.withdrawMoney(ship.price)
			shipLastBuy[player.uniqueId] = currentTimeMillis()
			lastBuyTimes[ship] = shipLastBuy

			BlueprintCommand.tryPilot(player, vec3i, ship.shipType, ship.displayName) {
				(it.data as PlayerStarshipData).shipDealerInformation = PlayerStarshipData.ShipDealerInformation(
					soldType = ship.schematicName,
					soldTime = currentTimeMillis(),
					creationBlockKey = it.data.blockKey
				)
			}

			player.success("Successfully bought a ${ship.schematicName} (Cost: ${ship.price}\n Remaining Balance: ${player.getMoneyBalance()})")

			player.rewardAchievement(Achievement.BUY_SPAWN_SHUTTLE)
		}
	}

	fun resolveTarget(schematic: Clipboard, destination: Location): Location {
		val target = destination.clone()

		var xOffset = listOf(-25, 25).random()
		var zOffset = listOf(-25, 25).random()
		var reversed = false
		val world = target.world

		for (i in 0..5000) {
			val targetVec = Vec3i(target)
			val dx = targetVec.x - schematic.origin.x
			val dy = targetVec.y - schematic.origin.y
			val dz = targetVec.z - schematic.origin.z

			var obstructed = false
			for (blockVector3 in schematic.region) {
				if (schematic.getBlock(blockVector3).blockType.material.isAir) {
					continue
				}

				val x = blockVector3.x + dx
				val y = blockVector3.y + dy
				val z = blockVector3.z + dz

				if (!world.worldBorder.isInside(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
					obstructed = true
					break
				}

				if (!world.getBlockAt(x, y, z).type.isAir) {
					obstructed = true
					break
				}
			}

			if (!obstructed) {
				return target
			}

			target.add(xOffset.toDouble(), 0.0, zOffset.toDouble())
			if (reversed || target.world.worldBorder.isInside(target)) {
				continue
			}
			reversed = true
			xOffset *= -1
			zOffset *= -1
		}

		return target
	}
}

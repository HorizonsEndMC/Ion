package net.starlegacy.feature.starship

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.NewPlayerProtection.hasProtection
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.starlegacy.SLComponent
import net.starlegacy.feature.nations.gui.item
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getMoneyBalance
import net.starlegacy.util.hasEnoughMoney
import net.starlegacy.util.placeSchematicEfficiently
import net.starlegacy.util.readSchematic
import net.starlegacy.util.withdrawMoney
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.ItemStack
import java.io.File
import java.lang.System.currentTimeMillis
import java.nio.file.Paths
import java.util.*

object StarshipDealers : SLComponent() {
	private val lastBuyTimes = mutableMapOf<UUID, Long>()

	@EventHandler(priority = EventPriority.LOWEST)
	fun onClickNPC(event: NPCRightClickEvent) {
		val npc = event.npc
		val player = event.clicker

		if (!npc.name.endsWith("Ship Dealer")) {
			return
		}
		val menu = MenuHelper.apply {
			val ships: List<GuiItem> = IonServer.shipList.map { ship ->
				val item: ItemStack = item(ship.material)
				item.editMeta {
					it.displayName(miniMessage().deserialize(ship.displayName))
					it.lore(
						listOf(
							miniMessage().deserialize(ship.loreLine1),
							miniMessage().deserialize(ship.loreLine2),
							miniMessage().deserialize(ship.loreLine3),
							miniMessage().deserialize(ship.loreLine4)
						)
					)
				}

				return@map guiButton(item) {
					val schematicFile: File? = getSchematicFile(ship.pathToSchem!!)
					if (!player.hasProtection()) {
						if (lastBuyTimes.getOrDefault(player.uniqueId, 0) + ship.cost > currentTimeMillis()) {
							player.sendMessage(miniMessage().deserialize("<yellow>Didn't I sell you a ship not too long ago? These things are expensive, and I am already selling them at a discount, leave some for other people."))
							return@guiButton
						}
					}

					if (!player.hasEnoughMoney(ship.cost)) {
						player.userError("This ship is too expensive for you\n It costs ${ship.cost}, you currently have ${player.getMoneyBalance()}")
						return@guiButton
					}

					if (schematicFile == null) {
						player.serverError("Schematic for ${ship.name} not found, please alert an Admin!")
						return@guiButton
					}

					val schematic = readSchematic(schematicFile)

					if (schematic == null) {
						player.serverError("Failed to read schematic file. Contact an admin!")
						return@guiButton
					}

					var target = player.location
					target.y = 196.0
					target = resolveTarget(schematic, target)

					val world = player.world
					val targetVec3i = Vec3i(target)
					placeSchematicEfficiently(schematic, world, targetVec3i, true) {
						player.teleport(target.add(ship.teleportOffsetX, ship.teleportOffsetY, ship.teleportOffsetZ))

						player.withdrawMoney(ship.cost)
						lastBuyTimes[player.uniqueId] = currentTimeMillis()

						player.success("Successfully bought a ${ship.name} (Cost: ${ship.cost}\n Remaining Balance: ${player.getMoneyBalance()})")
					}
				}.setName(miniMessage().deserialize(ship.displayName))
			}
			player.openPaginatedMenu("DealerShip", ships)
		}
	}

	private fun resolveTarget(schematic: Clipboard, destination: Location): Location {
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

	private fun getSchematicFile(filePath: String): File? {
		val file = Paths.get(IonServer.dataFolder.path.plus("/$filePath")).toFile()

		if (file.exists()) {
			return file
		}

		return null
	}
}

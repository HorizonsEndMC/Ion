package net.starlegacy.feature.starship

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.extent.clipboard.Clipboard
import java.io.File
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.starlegacy.SLComponent
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getMoneyBalance
import net.starlegacy.util.hasEnoughMoney
import net.starlegacy.util.msg
import net.starlegacy.util.placeSchematicEfficiently
import net.starlegacy.util.readSchematic
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.withdrawMoney
import org.bukkit.Location
import org.bukkit.event.EventHandler

object StarshipDealers : SLComponent() {
	private const val PRICE = 200.0
	private const val SCHEMATIC_NAME = "noob_ship"

	@EventHandler
	fun onClickNPC(event: NPCRightClickEvent) {
		val npc = event.npc
		val player = event.clicker

		if (!npc.name.endsWith("Ship Dealer")) {
			return
		}

		if (!player.hasEnoughMoney(PRICE)) {
			player msg "&cYou can't afford that: " +
				"You only have ${player.getMoneyBalance().toCreditsString()}, " +
				"but it costs ${PRICE.toCreditsString()}"
			return
		}

		val schematicFile = getSchematicFile()

		if (schematicFile == null) {
			player msg "&cSchematic $SCHEMATIC_NAME not found. Contact an admin!"
			return
		}

		val schematic = readSchematic(schematicFile)

		if (schematic == null) {
			player msg "&cFailed to read schematic file. Contact an admin!"
			return
		}

		var target = player.location
		target.y = 196.0
		target = resolveTarget(schematic, target)

		val world = player.world
		val targetVec3i = Vec3i(target)
		placeSchematicEfficiently(schematic, world, targetVec3i, true) {
			player.teleport(target.add(0.0, -3.0, 0.0))

			player.withdrawMoney(PRICE)

			player msg "&aPasted! (Cost: ${PRICE.toCreditsString()}; " +
				"Remaining Balance: ${player.getMoneyBalance().toCreditsString()})"
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

	private fun getSchematicFile(): File? {
		val file = WorldEdit.getInstance()
			.getWorkingDirectoryPath("schematics/$SCHEMATIC_NAME.schem")
			.toFile()

		if (file.exists()) {
			return file
		}

		val secondFile = WorldEdit.getInstance()
			.getWorkingDirectoryPath("schematics/$SCHEMATIC_NAME.schematic")
			.toFile()

		if (secondFile.exists()) {
			return secondFile
		}

		return null
	}
}

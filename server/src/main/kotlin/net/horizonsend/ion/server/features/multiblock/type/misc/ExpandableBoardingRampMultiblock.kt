package net.horizonsend.ion.server.features.multiblock.type.misc

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.manager.ShipMultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.BOARDING_RAMP_STATE
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object ExpandableBoardingRampMultiblock : Multiblock(), InteractableMultiblock, EntityMultiblock<ExpandableBoardingRampMultiblock.BoardingRampEntity> {
	override val name: String = "boardingramp"

	override val signText: Array<Component?> = createSignText(
		text("Boarding Ramp", RED),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-2) {
				x(0).ironBlock()
			}
			y(-1) {
				x(0).anyDoubleSlab()
			}
			y(0) {
				x(0).ironBlock()
			}
		}
		z(-1) {
			y(-2) {
				x(0).ironBlock()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): BoardingRampEntity {
		return BoardingRampEntity(data, manager, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.handleInteract(player)
	}

	class BoardingRampEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, ExpandableBoardingRampMultiblock, world, x, y, z, structureDirection) {
		override val inputsData: InputsData = none()
		private var extended: Boolean = data.getAdditionalDataOrDefault(BOARDING_RAMP_STATE, PersistentDataType.BOOLEAN, false)

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			store.addAdditionalData(BOARDING_RAMP_STATE, PersistentDataType.BOOLEAN, extended)
		}

		private fun incrementState() {
			extended = !extended
			val alias = STATE_ALIASES[extended]!!

			Tasks.sync {
				val sign = getSign() ?: return@sync
				sign.front().line(2, alias)
				sign.update()
				saveToSign()
			}
		}

		fun handleInteract(player: Player) {
			if (player.isSneaking) {
				player.information("Switched boarding ramp state")
				return incrementState()
			}

			if (toggleState(player)) incrementState()
			else {
				player.information("If the state is mismatched, you may shift click to swap it.")
			}
		}

		/**
		 * Mirrors the boarding ramp blocks over the centerline, using a defined rail as the initial guideline
		 * to detect the location.
		 *
		 * Returns if the toggle was successful
		 **/
		private fun toggleState(player: Player): Boolean {
			val width = getWidth()

			if (width > MAX_WIDTH + 1) {
				player.userError("Boarding ramp is too wide!")
				return false
			}

			if (width <= MIN_WIDTH) {
				player.userError("Boarding ramp is too narrow!")
				return false
			}

			val interiorLocations = detectRail(width, player) ?: return false

			// Get a map of new to old blocks
			val locationMap = getNewLocationMap(interiorLocations)

			return Tasks.getSyncBlocking {
				// Ensure that all blocks are available
				if (!checkObstructions(locationMap)) {
					player.userError("The boarding ramp is obstructed!")
					return@getSyncBlocking false
				}

				// Handle the movement
				switchState(locationMap)
				true
			}
		}

		/** Gets the width of the boarding ramp by continuing in a straight line while it is still iron blocks */
		private fun getWidth(): Int {
			if (!isIntact(true)) return 0

			val signDirection = structureDirection.oppositeFace

			var width = 0
			while (width < MAX_WIDTH + 2) {
				val block = getOrigin()
					.getRelative(FIRST_RAIL_OFFSET.x, FIRST_RAIL_OFFSET.y, FIRST_RAIL_OFFSET.z)
					.getRelative(signDirection, width)

				if (block.getTypeSafe() != Material.IRON_BLOCK) break

				width++
			}

			return width - 1
		}

		/**
		 *
		 * Returns the interior locations
		 **/
		private fun detectRail(width: Int, player: Player): List<Vec3i>? {
			val origin = globalVec3i.plus(FIRST_RAIL_OFFSET)
			val direction = structureDirection.rightFace

			val startBlock = origin.getRelative(direction)
			val startType = getBlockTypeSafe(world, startBlock.x, startBlock.y, startBlock.z) ?: return null

			val visited = mutableListOf<Vec3i>()
			val toVisit = arrayListOf(startBlock)

			val visitDirections = setOf(BlockFace.UP, direction)

			// Perform flood fill to find rail blocks up and to the right
			while (toVisit.isNotEmpty()) {
				val current = toVisit.removeFirst()
				visited.add(current)

				visitDirections
					.firstNotNullOfOrNull {
						val relativePos = current.getRelative(it)
						relativePos.takeIf { getBlockTypeSafe(world, relativePos.x, relativePos.y, relativePos.z) == startType }
					}
					?.let(toVisit::add)
			}

			// Ensure the total length of the ramp is <= 20
			if (!verifyLength(visited)) {
				player.userError("The rails are too long!")
				return null
			}

			// Ensure the slope of the rails are consistent
			if (!verifySlope(visited, player)) {
				// The error message is handled in the function
				return null
			}

			val oppositeRailLocations = visited.map { it.getRelative(structureDirection.oppositeFace, width) }

			// check other rail
			if (oppositeRailLocations.any { getBlockTypeSafe(world, it.x, it.y, it.z) != startType }) {
				player.userError("Opposite rail is not intact!")
				return null
			}

			val interiorLocations = getInteriorLocations(visited, width)

			if (interiorLocations.any {
				val material = getBlockTypeSafe(world, it.x, it.y, it.z)
				(material == null || !isAcceptableMaterial(material))
			}) {
				player.userError("Interior is not intact! It must be double slabs or stairs!")
				return null
			}

			return interiorLocations
		}

		/**
		 * Collects the interior locations of the rails
		 **/
		private fun getInteriorLocations(railLocations: List<Vec3i>, width: Int): List<Vec3i> {
			var interiorLocations =  railLocations.flatMap { pos ->
				(1..< width).map { pos.getRelative(structureDirection.oppositeFace, it) }
			}

			// Flip downwards if extended
			if (extended) {
				val baseY = globalVec3i.y + FIRST_RAIL_OFFSET.y

				interiorLocations = interiorLocations.map { interiorBlock ->
					val difference = interiorBlock.y - baseY
					Vec3i(interiorBlock.x, baseY - difference, interiorBlock.z)
				}
			}

			return interiorLocations
		}

		/**
		 * Checks if the material is acceptable to be moved.
		 **/
		private fun isAcceptableMaterial(material: Material): Boolean {
			return material.isSlab || material.isStairs || material.isGlass
		}

		/**
		 * Returns the run, assuming the rise is 1
		 **/
		private fun verifySlope(railLocations: List<Vec3i>, player: Player): Boolean {
			if (railLocations.size < 3) {
				player.userError("The rail is too short!")
				return false
			}

			// Get one to compare against
			var previousLoc = railLocations.first()

			var horizontalChangeSteps = 0
			var verticalChangeSteps = 0

			var lastSlope = 1

			// Move through the list of blocks in the rail
			for ((index, loc) in railLocations.withIndex()) {
				if (loc.x != previousLoc.x || loc.z != previousLoc.z) horizontalChangeSteps = 1
				else {
					horizontalChangeSteps++

					// If it has taken more than 2 steps to move horizontally, it is too steep.
					if (horizontalChangeSteps > 2) {
						player.userError("The rail is too steep!")
						return false
					}
				}

				if (loc.y != previousLoc.y) {
					// Check if the last slope matches the current slope.
					// Supress the check under 4 blocks, as measurements are inaccurate
					if (lastSlope != verticalChangeSteps && index > 4) {
						player.userError("The rail slope is not consistent!")
						return false
					}

					// Record the slope as the number of steps between vertical changes
					lastSlope = verticalChangeSteps
					verticalChangeSteps = 0
				}
				else verticalChangeSteps++

				// Mark location as the previous location and move to the next one
				previousLoc = loc
			}

			return true
		}

		private fun getNewLocationMap(interiorBlocks: List<Vec3i>): Map<Vec3i, Vec3i> {
			val baseY = globalVec3i.y + FIRST_RAIL_OFFSET.y

			return interiorBlocks.associateWith { interiorBlock ->
				val difference = interiorBlock.y - baseY
				Vec3i(interiorBlock.x, baseY - difference, interiorBlock.z)
			}
		}

		private fun checkObstructions(blockMap: Map<Vec3i, Vec3i>): Boolean {
			return blockMap.all { (original, new) -> original == new || getBlockTypeSafe(world, new.x, new.y, new.z)?.isAir == true }
		}

		private fun switchState(blockMap: Map<Vec3i, Vec3i>) {
			val removeKeys = LongOpenHashSet()
			val addKeys = LongOpenHashSet()

			for ((oldLocation, newLocation) in blockMap.entries) {
				val newBlockData = getFlippedData(world.getBlockData(oldLocation.x, oldLocation.y, oldLocation.z))

				world.setBlockData(oldLocation.x, oldLocation.y, oldLocation.z, Material.AIR.createBlockData())
				world.setBlockData(newLocation.x, newLocation.y, newLocation.z, newBlockData)
				removeKeys.add(oldLocation.toBlockKey())
				addKeys.add(newLocation.toBlockKey())
			}

			val starship = (manager as? ShipMultiblockManager)?.starship ?: return

			starship.blocks.removeAll(removeKeys)
			starship.blocks.addAll(addKeys)
			starship.calculateHitbox()
		}

		private fun getFlippedData(blockData: BlockData) = when (blockData) {
			is Slab -> (blockData.clone() as Slab).apply {
				when (type) {
					Slab.Type.BOTTOM -> type = Slab.Type.TOP
					Slab.Type.TOP -> type = Slab.Type.BOTTOM
					else -> {}
				}
			}

			is Stairs -> (blockData.clone() as Stairs).apply {
				when (half) {
					Bisected.Half.BOTTOM -> half = Bisected.Half.TOP
					Bisected.Half.TOP -> half = Bisected.Half.BOTTOM
					else -> {}
				}
			}

			else -> blockData
		}

		private fun verifyLength(railLocations: List<Vec3i>): Boolean {
			return maxOf(
				railLocations.maxOf { it.x } - railLocations.minOf { it.x },
				railLocations.maxOf { it.z } - railLocations.minOf { it.z }
			) < MAX_LENGTH
		}

		companion object {
			val FIRST_RAIL_OFFSET = Vec3i(0, -2, 0)

			const val MIN_WIDTH = 1
			const val MAX_WIDTH = 16
			const val MAX_LENGTH = 20

			val STATE_ALIASES = mapOf(
				false to text("Retracted"),
				true to text("Extended"),
			)
		}
	}
}

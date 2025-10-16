package net.horizonsend.ion.server.features.multiblock.type.misc

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.ProceduralMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.manager.ShipMultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.miscellaneous.playSoundInRadius
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.BOARDING_RAMP_STATE
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
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
import kotlin.math.ceil
import kotlin.math.floor

object ExpandableBoardingRampBaseMultiblock : ProceduralMultiblock(), InteractableMultiblock, EntityMultiblock<ExpandableBoardingRampBaseMultiblock.BoardingRampEntity> {
	override val name: String = "boardingramp"

	override val signText: Array<Component?> = createSignText(
		text("Boarding Ramp", RED),
		null,
		text("Wood Leek", NamedTextColor.AQUA),
		text("Industries, Inc", NamedTextColor.AQUA)
	)

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(-2) {
				x(0).ironBlock()
			}
		}
	}

	override fun MultiblockShape.buildExampleShape() {
		z(-1) {
			y(-2) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
			}
			y(-1) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(-2) {
			y(-2) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
			}
			y(-1) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(-3) {
			y(-2) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
			}
			y(-1) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(0) {
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): BoardingRampEntity {
		return BoardingRampEntity(data, manager, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.handleInteract(sign, player)
	}

	class BoardingRampEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, ExpandableBoardingRampBaseMultiblock, world, x, y, z, structureDirection) {
		override val ioData: IOData = none()
		private var extended: Boolean = data.getAdditionalDataOrDefault(BOARDING_RAMP_STATE, PersistentDataType.BOOLEAN, false)

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			store.addAdditionalData(BOARDING_RAMP_STATE, PersistentDataType.BOOLEAN, extended)
		}

		private fun incrementState() {
			extended = !extended
			val alias = STATE_ALIASES[extended]!!

			Tasks.sync {
				val sign = getSign() ?: return@sync
				sign.front().line(1, alias)
				sign.update()
				saveToSign()

				val soundKey = if (extended) "minecraft:block.piston.extend" else "minecraft:block.piston.contract"
				val sound = Sound.sound(Key.key(soundKey), Sound.Source.PLAYER, 1.0f, 1.0f)

				playSoundInRadius(location, 30.0, sound)
			}
		}

		fun handleInteract(sign: Sign, player: Player) {
			if (player.isSneaking) {
				player.information("Switched boarding ramp state")
				return incrementState()
			}

			if (toggleState(sign, player)) incrementState()
			else {
				player.information("If the state is mismatched, you may shift click to swap it.")
			}
		}

		/**
		 * Switches the state of the ramp, returns whether the state can be switched sucessfully
		 **/
		fun toggleState(sign: Sign, player: Player): Boolean {
			val detectionOrigin = getRelative(Vec3i(sign.x, sign.y, sign.z), structureDirection, FIRST_RAIL_OFFSET.x, FIRST_RAIL_OFFSET.y, FIRST_RAIL_OFFSET.z)
			val widthDirection = structureDirection.oppositeFace
			val rampDirection = widthDirection.leftFace

			var plateWidth = 0

			while (plateWidth < MAX_WIDTH) {
				val (x, y, z) = detectionOrigin.getRelative(widthDirection, plateWidth)
				if (world.getBlockData(x, y, z).material != Material.IRON_BLOCK) break

				plateWidth++
			}

			if (plateWidth < 1) {
				player.userError("Boarding ramp is too narrow!")
				return false
			}

			var length = 1
			var height = 0.0

			val locations = mutableListOf<Vec3i>()

			while (length < MAX_LENGTH) {
				var foundSegment: BoardingRampSegment? = null

				val firstSegment = detectionOrigin
					.getRelative(BlockFace.UP, (if (extended) floor(height) else ceil(height)).toInt())
					.getRelative(rampDirection, length)

				for (forward in 0..<plateWidth) {
					val (x, y, z) = firstSegment.getRelative(widthDirection, forward)

					val block = world.getBlockAt(x, y, z)

					if (foundSegment != null) {
						if (!foundSegment.blockMatches(block)) {
							// If broken part way through the horizontal axis, it's not intact
							player.userError("Boarding ramp not intact!")
							return false
						}
					} else {
						val matching = BoardingRampSegment[block] ?: break
						foundSegment = matching
					}

					locations.add(Vec3i(x, y, z))
				}

				if (foundSegment == null) break

				val computedOffset = foundSegment.getNextHeight(world, firstSegment, rampDirection)

 				if (extended) {
					height -= computedOffset
				} else {
					height += computedOffset
				}

				length++
			}

			val locationMap = getNewLocationMap(locations)

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

		/**
		 * Returns a map of old locations to new locations
		 **/
		private fun getNewLocationMap(interiorBlocks: List<Vec3i>): Map<Vec3i, Vec3i> {
			val baseY = globalVec3i.y + FIRST_RAIL_OFFSET.y

			return interiorBlocks.associateWith { interiorBlock ->
				val difference = interiorBlock.y - baseY
				Vec3i(interiorBlock.x, baseY - difference, interiorBlock.z)
			}
		}

		/**
		 * Returns true if there are no obstructions found in the new location set
		 **/
		private fun checkObstructions(blockMap: Map<Vec3i, Vec3i>): Boolean = blockMap.all { (original, new) ->
			original == new || getBlockTypeSafe(world, new.x, new.y, new.z)?.isAir == true
		}

		/**
		 * Translates the old block locations to the new ones, and flips the block data
		 **/
		private fun switchState(blockMap: Map<Vec3i, Vec3i>) {
			val removeKeys = LongOpenHashSet()
			val addKeys = LongOpenHashSet()

			for ((oldLocation, newLocation) in blockMap.entries) {
				val newBlockData = getFlippedData(world.getBlockData(oldLocation.x, oldLocation.y, oldLocation.z))

				world.setBlockData(oldLocation.x, oldLocation.y, oldLocation.z, Material.AIR.createBlockData())
				world.setBlockData(newLocation.x, newLocation.y, newLocation.z, newBlockData)

				@Suppress("DEPRECATION") removeKeys.add(oldLocation.toBlockKey())
				@Suppress("DEPRECATION") addKeys.add(newLocation.toBlockKey())
			}

			// Change the blocks if this is part of a starship
			val starship = (manager as? ShipMultiblockManager)?.starship ?: return

			starship.blocks.removeAll(removeKeys)
			starship.blocks.addAll(addKeys)
			starship.calculateHitbox()
		}

		/**
		 * Flips the block data of blocks part of the ramp
		 **/
		private fun getFlippedData(blockData: BlockData) = when (blockData) {
			is Slab -> (blockData.clone() as Slab).apply {
				when (type) {
					Slab.Type.BOTTOM -> type = Slab.Type.TOP
					Slab.Type.TOP -> type = Slab.Type.BOTTOM
					Slab.Type.DOUBLE -> {}
				}
			}

			is Stairs -> (blockData.clone() as Stairs).apply {
				half = when (half) {
					Bisected.Half.BOTTOM -> Bisected.Half.TOP
					Bisected.Half.TOP -> Bisected.Half.BOTTOM
				}
			}

			else -> blockData
		}

		companion object {
			val FIRST_RAIL_OFFSET = Vec3i(0, -2, 0)
			private val MAX_WIDTH get() = 9
			private val MAX_LENGTH get() = 30

			val STATE_ALIASES = mapOf(
				false to text("Retracted"),
				true to text("Extended"),
			)
		}
	}

	sealed class BoardingRampSegment(val height: Double) {
		// Lock to north since they're 1x1 structures and
		fun blockMatches(block: Block) = structure.checkRequirementsSpecific(block, BlockFace.NORTH, false, false)

		private val structure = MultiblockShape().apply { buildStructure() }

		protected abstract fun MultiblockShape.buildStructure()

		companion object {
			private val SEGEMENTS get() = arrayOf(SLAB, STAIR)

			operator fun get(block: Block) = SEGEMENTS.firstOrNull { segment -> segment.blockMatches(block) }
		}

		abstract fun getNextHeight(world: World, currentSegmentLocation: Vec3i, rampDirection: BlockFace): Double

		object SLAB: BoardingRampSegment(0.5) {
			override fun MultiblockShape.buildStructure() {
				at(0, 0, 0).anySlab()
			}

			override fun getNextHeight(world: World, currentSegmentLocation: Vec3i, rampDirection: BlockFace): Double {
				val next = currentSegmentLocation.getRelative(rampDirection)
				debugAudience.highlightBlock(next, 120L)
				if (get(world.getBlockAt(next.x, next.y, next.z)) == SLAB) return 0.0
				return 1.0
			}
		}

		object STAIR: BoardingRampSegment(1.0) {
			override fun MultiblockShape.buildStructure() {
				at(0, 0, 0).anyStairs()
			}

			override fun getNextHeight(world: World, currentSegmentLocation: Vec3i, rampDirection: BlockFace): Double {
				return 1.0
			}
		}
	}
}

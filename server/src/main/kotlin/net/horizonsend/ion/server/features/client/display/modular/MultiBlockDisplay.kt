package net.horizonsend.ion.server.features.client.display.modular

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Observer
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Vector2d

class MultiBlockDisplay(
	val world: World,
	anchorPoint: Vector,
	initHeading: Vector2d
) {
	var anchorPoint = anchorPoint
		set(value) {
			field = value
			blocks.forEach { _, display -> display.position = value }
		}

	val referenceVector = initHeading

	val blocks = mutableMapOf<Vec3i, BlockDisplayWrapper>()
	var special: Pair<Vec3i, BlockDisplayWrapper>? = null

	var heading: Vector2d = initHeading
		set(value) {
			field = value
			refresh()
		}

	fun reCalculateBlockOffsetHeading(offset: Vec3i, block: BlockDisplayWrapper) {
		val radians = heading.angle(referenceVector)

		block.offset = offset.toVector()
			.subtract(Vector(0.5, 0.0, 0.5)) // Remove the offset from the rotation axis being a center loc
			.rotateAroundY(radians) // Rotate around the axis
			.add(Vector(0.5, 0.0, 0.5)) // Add back the center location offset

		block.heading = Vector(referenceVector.x, 0.0, referenceVector.y).rotateAroundY(radians)

		block.update()
	}

	fun updateCamera() {
		val (offset: Vec3i, block: BlockDisplayWrapper) = special ?: return

		val radians = heading.angle(referenceVector)
		block.position = offset.toVector()
			.subtract(Vector(0.5, 0.0, 0.5)) // Remove the offset from the rotation axis being a center loc
			.rotateAroundY(radians) // Rotate around the axis
			.add(Vector(0.5, 0.0, 0.5)) // Add back the center location offset

		block.heading = Vector(referenceVector.x, 0.0, referenceVector.y).rotateAroundY(radians)
		block.update()
	}

	fun refresh() {
		blocks.forEach { t -> reCalculateBlockOffsetHeading(t.key, t.value) }
		updateCamera()
	}

	fun addBlock(offset: Vec3i, blockData: BlockData) {
		if (blockData is Observer) {
			addCamera(offset, blockData)
			return
		}

		blocks[offset] = BlockDisplayWrapper(
			world = world,
			initPosition = anchorPoint,
			initHeading = Vector(referenceVector.x, 0.0, referenceVector.y),
			initTransformation = offset.toVector(),
			blockData = blockData
		)
	}

	fun addCamera(offset: Vec3i, blockData: BlockData) {
		special = offset to BlockDisplayWrapper(
			world = world,
			initPosition = anchorPoint.clone().add(offset.toVector()),
			initHeading = Vector(referenceVector.x, 0.0, referenceVector.y),
			initTransformation = Vector(),
			blockData = blockData
		)
	} //TODO do this separately later

	fun remove() {
		displays.remove(this)
		blocks.forEach { _, display -> display.remove() }
	}

	fun displace(movement: StarshipMovement) {
		anchorPoint = movement.displaceLocation(anchorPoint.toLocation(world)).toVector()
	}

	companion object : IonServerComponent() {
		fun createFromClipboard(player: Player, clipboard: Clipboard): MultiBlockDisplay {
			val center = clipboard.origin

			val parent = MultiBlockDisplay(
				player.world,
				Vec3i(player.location).toVector().add(Vector(0.5, 0.0, 0.5)),
				Vector2d(player.location.direction.x, player.location.direction.z)
			)

			clipboard.forEach { t ->
				val block = BukkitAdapter.adapt(clipboard.getBlock(t))
				if (block.material.isAir) return@forEach

				val offset = Vec3i(t.x() - center.x(), t.y() - center.y(), t.z() - center.z())
				parent.addBlock(offset, block)
			}

			displays.add(parent)
			return parent
		}

		fun createFromBlocks(world: World, anchorBlock: Vec3i, initHeading: Vector2d, blocks: Map<Vec3i, BlockData>): MultiBlockDisplay {
			val parent = MultiBlockDisplay(
				world,
				anchorBlock.toVector().add(Vector(0.5, 0.0, 0.5)),
				initHeading
			)

			blocks.forEach { (key, value) ->
				if (value.material.isAir) return@forEach
				parent.addBlock(key, value)
			}

			displays.add(parent)
			return parent
		}

		val displays: MutableList<MultiBlockDisplay> = mutableListOf()
	}
}

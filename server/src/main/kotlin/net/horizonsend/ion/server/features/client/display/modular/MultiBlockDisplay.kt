package net.horizonsend.ion.server.features.client.display.modular

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.atan2

class MultiBlockDisplay(
	val owner: Player,
	var anchorPoint: Vector,
	initHeading: Double
) {
	val blocks = mutableMapOf<Vec3i, BlockDisplayWrapper>()

	var heading: Double = initHeading
		set(value) {
			field = value
			refresh()
		}

	fun reCalculateBlockOffsetHeading(offset: Vec3i, block: BlockDisplayWrapper) {
		block.offset = offset.toVector().rotateAroundY(Math.toRadians(heading))
		block.heading = referenceAngle.clone().rotateAroundY(Math.toRadians(heading))

		block.update()
	}

	fun refresh() {
		blocks.forEach { t -> reCalculateBlockOffsetHeading(t.key, t.value) }
	}

	fun addBlock(offset: Vec3i, blockData: BlockData) {
		blocks[offset] = BlockDisplayWrapper(
			owner.world,
			anchorPoint,
			referenceAngle,
			offset.toCenterVector(),
			blockData
		)
	}

	companion object : IonServerComponent() {
		// North as reference
		val referenceAngle = BlockFace.SOUTH.direction

		fun vectorToDegrees(vector: Vector): Double {
			val twoPi = 2 * Math.PI
			val theta = atan2(-vector.x, vector.z)
			return Math.toDegrees((theta + twoPi) % twoPi)
		}

		fun createFromClipboard(player: Player, clipboard: Clipboard) {
			val center = clipboard.origin

			val parent = MultiBlockDisplay(
				player,
				Vec3i(player.location).toVector().add(Vector(0.5, 0.0, 0.5)),
				360.0 - vectorToDegrees(player.location.direction)
			)

			clipboard.forEach { t ->
				val block = BukkitAdapter.adapt(clipboard.getBlock(t))
				if (block.material.isAir) return@forEach

				val offset = Vec3i(t.x() - center.x(), t.y() - center.y(), t.z() - center.z())
				parent.addBlock(offset, block)
			}

			displays.add(parent)
		}

		val displays: MutableList<MultiBlockDisplay> = mutableListOf()

		override fun onEnable() {
			Tasks.asyncRepeat(1L, 1L) {
				displays.forEach { t ->
					t.heading = 360.0 - vectorToDegrees(t.owner.location.direction)
				}
			}
		}
	}
}

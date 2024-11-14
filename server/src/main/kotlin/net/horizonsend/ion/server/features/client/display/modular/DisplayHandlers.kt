package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.modular.display.Display
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import java.util.concurrent.ConcurrentHashMap

object DisplayHandlers : IonServerComponent() {

	fun newSignOverlay(sign: Sign) {

	}

	fun newMultiblockSignOverlay(entity: MultiblockEntity, vararg display: Display): TextDisplayHandler {
		val signDirection = entity.structureDirection.oppositeFace

		return TextDisplayHandler(
			entity,
			entity.world,
			entity.x.toDouble() + 0.5,
			entity.y.toDouble() + 0.4,
			entity.z.toDouble() + 0.5,
			offsetRight = 0.0,
			offsetUp = 0.0,
			offsetForward = 0.61,
			signDirection,
			*display
		)
	}

	fun newBlockOverlay(holder: DisplayHandlerHolder, world: World, block: Vec3i, direction: BlockFace, vararg display: Display): TextDisplayHandler {
		val facingBlock = getRelative(toBlockKey(block), direction)

		val x = getX(facingBlock).toDouble() + 0.5
		val y = getY(facingBlock).toDouble() + 0.35
		val z = getZ(facingBlock).toDouble() + 0.5

		return TextDisplayHandler(
			holder,
			world,
			x,
			y,
			z,
			offsetRight = 0.0,
			offsetUp = 0.0,
			offsetForward = -0.45,
			direction,
			*display
		)
	}

	fun newMultiText() {

	}

	fun newSingleText() {

	}

	private val displayHandlers = ConcurrentHashMap.newKeySet<TextDisplayHandler>()

	fun registerHandler(handler: TextDisplayHandler) {
		displayHandlers.add(handler)
	}

	fun deRegisterHandler(handler: TextDisplayHandler) {
		displayHandlers.remove(handler)
	}

	override fun onEnable() {
		Tasks.asyncRepeat(100L, 100L, ::runUpdates)
	}

	private fun runUpdates() {
		for (displayHolder in displayHandlers) {
			displayHolder.update()
		}
	}
}

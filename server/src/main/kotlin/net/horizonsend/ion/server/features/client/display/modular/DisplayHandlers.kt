package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import java.util.concurrent.ConcurrentHashMap

object DisplayHandlers : IonServerComponent() {

	fun newSignOverlay(sign: Sign) {

	}

	fun newMultiblockSignOverlay(entity: MultiblockEntity, vararg displayModule: (TextDisplayHandler) -> DisplayModule): TextDisplayHandler {
		val signDirection = entity.structureDirection.oppositeFace
		val signLocation = entity.getSignLocation()

		val builder = TextDisplayHandler.builder(entity, signLocation.blockX, signLocation.blockY, signLocation.blockZ)
			.setOffset(offsetRight = 0.0, offsetUp = -0.5, offsetForward = -0.39)
			.setDirection(signDirection)

		displayModule.forEach(builder::addDisplay)

		return builder.build()
	}

	fun newBlockOverlay(holder: DisplayHandlerHolder, block: Vec3i, direction: BlockFace, vararg displayModule: (TextDisplayHandler) -> DisplayModule): TextDisplayHandler {
		val builder = TextDisplayHandler.builder(holder, block.x, block.y, block.z)
			.setOffset(offsetRight = 0.0, offsetUp = -0.1, offsetForward = -0.39)
			.setDirection(direction)

		displayModule.forEach(builder::addDisplay)

		return builder.build()
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

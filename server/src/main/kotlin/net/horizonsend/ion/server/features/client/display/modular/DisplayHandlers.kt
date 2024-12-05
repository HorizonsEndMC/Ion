package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.modular.display.Display
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import java.util.concurrent.ConcurrentHashMap

object DisplayHandlers : IonServerComponent() {

	fun newSignOverlay(sign: Sign) {

	}

	fun newMultiblockSignOverlay(entity: MultiblockEntity, vararg display: Display): TextDisplayHandler {
		val signDirection = entity.structureDirection.oppositeFace
		val signLocation = entity.getSignLocation()

		return TextDisplayHandler(
			entity,
			entity.world,
			signLocation.blockX,
			signLocation.blockY,
			signLocation.blockZ,
			0.0,
			-0.1,
			-0.39, // Back up towards the sign
			signDirection,
			*display
		)
	}

	fun newBlockOverlay(holder: DisplayHandlerHolder, world: World, block: Vec3i, direction: BlockFace, vararg display: Display): TextDisplayHandler {
		return TextDisplayHandler(
			holder,
			world,
			block.x,
			block.y,
			block.z,
			0.0,
			-0.15,
			0.55,
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

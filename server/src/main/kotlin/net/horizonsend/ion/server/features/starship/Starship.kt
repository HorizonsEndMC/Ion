package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.mainThreadCheck
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

open class Starship(serverLevel: ServerLevel, centerOfMass: BlockPos) {
	open var serverLevel = serverLevel
		set(value) {
			mainThreadCheck()
			field = value
		}

	open var centerOfMass = centerOfMass
		set(value) {
			mainThreadCheck()
			field = value
		}

	var controller: Controller? = null
		set(value) {
			(value ?: field)?.hint("Updated control mode to ${value?.name ?: "None"}.")
			field?.destroy()
			field = value
		}

	/** Called when a starship is removed. Any cleanup logic should be done here. */
	fun destroy() {
		controller?.destroy()
	}
}

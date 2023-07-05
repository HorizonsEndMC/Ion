package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.controllers.PlayerController
import net.horizonsend.ion.server.miscellaneous.mainThreadCheck
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import org.bukkit.entity.Player

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

	val playerPilot: Player? get() =
		(controller as? PlayerController)?.player

	/** Called on each server tick. */
	fun tick() {
		controller?.tick()
	}

	/** Called when a starship is removed. Any cleanup logic should be done here. */
	fun destroy() {
		IonWorld[serverLevel].starships.remove(this)
		controller?.destroy()
	}

	init {
		@Suppress("LeakingThis") // This is done right at the end of the class's initialization, it *should* be fine
		IonWorld[serverLevel].starships.add(this)
	}
}

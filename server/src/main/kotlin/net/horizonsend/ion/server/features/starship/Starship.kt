package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.server.features.starship.controllers.ActivePlayerController
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.controllers.PlayerController
import net.horizonsend.ion.server.miscellaneous.IonWorld
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.mainThreadCheck
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.World
import org.bukkit.entity.Player

open class Starship(world: World, centerOfMass: Vec3i) {
	open var world: World = world
		set(value) {
			mainThreadCheck()
			field = value
		}

	open var centerOfMass: Vec3i = centerOfMass
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

	/**
	 * If the controller is an active player controller, get the player.
	 * It just makes a lot of things less verbose.
	 *
	 * Try not to use, most starship code should not rely on players.
	 **/
	val playerPilot: Player? get() = (controller as? ActivePlayerController)?.player

	/** Similar to playerPilot, gets the last player if unpiloted or active **/
	val lastPilot: Player? get() = (controller as? PlayerController)?.player

	/** Called on each server tick. */
	fun tick() {
		controller?.tick()
	}

	/** Called when a starship is removed. Any cleanup logic should be done here. */
	fun destroy() {
		IonWorld[world.minecraft].starships.remove(this)
		controller?.destroy()
	}

	init {
		@Suppress("LeakingThis") // This is done right at the end of the class's initialization, it *should* be fine
		IonWorld[world.minecraft].starships.add(this)
	}
}

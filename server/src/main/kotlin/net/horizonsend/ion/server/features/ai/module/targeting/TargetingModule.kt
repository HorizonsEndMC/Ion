package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag

abstract class TargetingModule(controller: AIController) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	/** Determines if the module should stick to a target once finding it */
	var sticky: Boolean = false
	var lastTarget: AITarget? = null

	open fun findTarget(): AITarget? {
		if (sticky && lastTarget != null) return lastTarget
		return searchForTarget()
	}

	open fun findTargets(): List<AITarget> {
		return searchForTargetList()
	}

	protected abstract fun searchForTarget(): AITarget?

	protected abstract fun searchForTargetList(): List<AITarget>

	override fun toString(): String {
		return "${javaClass.simpleName}[sticky: $sticky, lastTarget: $lastTarget]"
	}

	protected fun targetFilter(aiTarget: AITarget, targetAI : Boolean) : Boolean {
		when  {
			aiTarget is StarshipTarget && aiTarget.ship.controller is PlayerController-> {
				if (targetAI) return false
				val player = (aiTarget.ship.controller as PlayerController).player
				if (!player.hasProtection()) return true // check for prot
				if (starship.world.ion.hasFlag(WorldFlag.NOT_SECURE)) return true //ignore prot in unsafe areas
				if (starship.damagers.keys.any{(it as PlayerDamager).player == player}) return true //fire first
			}
			aiTarget is StarshipTarget && aiTarget.ship.controller is AIController -> {
				return targetAI && aiTarget.ship.controller != controller
			}
			aiTarget is PlayerTarget && !targetAI -> {
				if (starship.world.ion.hasFlag(WorldFlag.NOT_SECURE)) return true
				return !aiTarget.player.hasProtection()
			}
		}
		return false
	}
}

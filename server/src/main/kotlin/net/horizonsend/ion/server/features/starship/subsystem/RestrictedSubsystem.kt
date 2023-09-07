package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.starship.control.controllers.Controller

interface RestrictedSubsystem {
	fun canUse(controller: Controller): Boolean
}

package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class FactionManagerModule(controller: AIController, val faction: AIFaction) : AIModule(controller)

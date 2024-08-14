package net.horizonsend.ion.server.features.npcs.traits

import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.persistence.Persist
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import org.bukkit.event.EventHandler

@TraitName("ioncombatnpc")
class CombatNPCTrait : Trait("ioncombatnpc") {
	@EventHandler
	fun onNPCInteract(event: NPCRightClickEvent) {
		println("b")
	}

	@Persist
	var created: Long = System.currentTimeMillis()

	override fun run() {

	}
}

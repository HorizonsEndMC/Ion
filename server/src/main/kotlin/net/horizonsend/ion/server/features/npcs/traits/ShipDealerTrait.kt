package net.horizonsend.ion.server.features.npcs.traits

import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import org.bukkit.event.EventHandler

@TraitName("ionshipdealer")
class ShipDealerTrait : Trait("ionshipdealer") {
	@EventHandler
	fun onNPCInteract(event: NPCRightClickEvent) {
		println("b")
	}
}

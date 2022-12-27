package net.horizonsend.ion.server.items.objects

import net.kyori.adventure.text.Component.text
import org.bukkit.Material.WARPED_FUNGUS_ON_A_STICK

object Magazine : AmmunitionHoldingItem("MAGAZINE", WARPED_FUNGUS_ON_A_STICK, 1, text("Magazine"), true) {
	override fun getMaximumAmmunition(): Int = 30
}
package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayContainer
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class GlowModule(controller: AIController) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	val container = ItemDisplayContainer(
		world,
		1.5f,
		starship.centerOfMass.toCenterVector(),
		BlockFace.UP.direction,
		ItemStack(Material.JUKEBOX)
	).apply {
		getEntity().setGlowingTag(true)
	}

	override fun onMove(movement: StarshipMovement) {
		container.position = starship.centerOfMass.toCenterVector()

		container.update()
	}

	private var ticks = 0

	override fun tick() {
		ticks++

		if (ticks % 20 != 0) return

		container.update()
	}
}

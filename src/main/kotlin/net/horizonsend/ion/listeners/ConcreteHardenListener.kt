package net.horizonsend.ion.listeners

import java.util.EnumSet
import org.bukkit.Material
import org.bukkit.Material.BLACK_CONCRETE_POWDER
import org.bukkit.Material.BLUE_CONCRETE_POWDER
import org.bukkit.Material.BROWN_CONCRETE_POWDER
import org.bukkit.Material.CYAN_CONCRETE_POWDER
import org.bukkit.Material.GRAY_CONCRETE_POWDER
import org.bukkit.Material.GREEN_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_BLUE_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_GRAY_CONCRETE_POWDER
import org.bukkit.Material.LIME_CONCRETE_POWDER
import org.bukkit.Material.MAGENTA_CONCRETE_POWDER
import org.bukkit.Material.ORANGE_CONCRETE_POWDER
import org.bukkit.Material.PINK_CONCRETE_POWDER
import org.bukkit.Material.PURPLE_CONCRETE_POWDER
import org.bukkit.Material.RED_CONCRETE_POWDER
import org.bukkit.Material.WHITE_CONCRETE_POWDER
import org.bukkit.Material.YELLOW_CONCRETE_POWDER
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent

internal class ConcreteHardenListener: Listener {
	private val concretePowder: EnumSet<Material> = setOf(
		WHITE_CONCRETE_POWDER,
		ORANGE_CONCRETE_POWDER,
		MAGENTA_CONCRETE_POWDER,
		LIGHT_BLUE_CONCRETE_POWDER,
		YELLOW_CONCRETE_POWDER,
		LIME_CONCRETE_POWDER,
		PINK_CONCRETE_POWDER,
		GRAY_CONCRETE_POWDER,
		LIGHT_GRAY_CONCRETE_POWDER,
		CYAN_CONCRETE_POWDER,
		PURPLE_CONCRETE_POWDER,
		BLUE_CONCRETE_POWDER,
		BROWN_CONCRETE_POWDER,
		GREEN_CONCRETE_POWDER,
		RED_CONCRETE_POWDER,
		BLACK_CONCRETE_POWDER
	).toCollection(EnumSet.noneOf(Material::class.java))

	@EventHandler
	fun onConcreteHarden(event: BlockFormEvent) {
		if (concretePowder.contains(event.block.type)) event.isCancelled = true
	}
}
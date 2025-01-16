package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import net.kyori.adventure.sound.Sound
import org.bukkit.inventory.ItemStack

interface RecipeEnviornment {
	fun getItemSize(): Int
	fun getItem(index: Int): ItemStack?

	fun isEmpty(): Boolean

	fun playSound(sound: Sound)
}

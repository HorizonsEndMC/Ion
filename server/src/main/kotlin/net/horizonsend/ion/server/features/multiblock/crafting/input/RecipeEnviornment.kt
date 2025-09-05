package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.kyori.adventure.sound.Sound
import org.bukkit.inventory.ItemStack

interface RecipeEnviornment {
	val multiblock: MultiblockEntity

	fun getInputItems(): List<ItemStack?>

	fun getInputItemSize(): Int
	fun getInputItem(index: Int): ItemStack?

	fun playSound(sound: Sound)
}

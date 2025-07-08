package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.kyori.adventure.sound.Sound
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ChemicalProcessorEnviornment(
	override val multiblock: MultiblockEntity,
	val inputInventory: Inventory,
	val outputInventory: Inventory,

	val fluidInputOne: FluidStorageContainer,
	val fluidInputTwo: FluidStorageContainer,

	val fluidOutputOne: FluidStorageContainer,
	val fluidOutputTwo: FluidStorageContainer,

	val pollutionContainer: FluidStorageContainer
) : RecipeEnviornment {
	override fun getItemSize(): Int {
		return 0
	}

	override fun getItem(index: Int): ItemStack? {
		return inputInventory.contents.getOrNull(index)
	}

	override fun getInputItems(): List<ItemStack?> {
		return inputInventory.contents.toList()
	}

	override fun playSound(sound: Sound) {
		val originLocation = multiblock.location.toCenterLocation()
		originLocation.world.playSound(sound, originLocation.x, originLocation.y, originLocation.z)
	}
}

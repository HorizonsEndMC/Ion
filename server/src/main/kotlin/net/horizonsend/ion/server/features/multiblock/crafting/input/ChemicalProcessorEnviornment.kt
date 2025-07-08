package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.ChemicalProcessorMultiblock
import net.kyori.adventure.sound.Sound
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ChemicalProcessorEnviornment(
	override val multiblock: ChemicalProcessorMultiblock.ChemicalProcessorEntity,
	val inputInventory: Inventory,
	val outputInventory: Inventory,

	val fluidInputOne: FluidStorageContainer,
	val fluidInputTwo: FluidStorageContainer,

	val fluidOutputOne: FluidStorageContainer,
	val fluidOutputTwo: FluidStorageContainer,

	val pollutionContainer: FluidStorageContainer
) : RecipeEnviornment, InventoryResultEnviornment, FluidMultiblockEnviornment {
	override fun getInputItemSize(): Int {
		return 0
	}

	override fun getInputItem(index: Int): ItemStack? {
		return inputInventory.contents.getOrNull(index)
	}

	override fun getInputItems(): List<ItemStack?> {
		return inputInventory.contents.toList()
	}

	override fun playSound(sound: Sound) {
		val originLocation = multiblock.location.toCenterLocation()
		originLocation.world.playSound(sound, originLocation.x, originLocation.y, originLocation.z)
	}

	override fun getResultInventory(): Inventory {
		return outputInventory
	}
}

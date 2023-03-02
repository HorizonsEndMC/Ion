package net.starlegacy.feature.multiblock.generator

import net.starlegacy.feature.machine.GeneratorFuel
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class GeneratorMultiblock(tierText: String, private val tierMaterial: Material) :
	Multiblock(),
	PowerStoringMultiblock,
	FurnaceMultiblock {
	abstract val speed: Double
	override val inputComputerOffset = Vec3i(0, -1, 0)

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0
		val inventory = furnace.inventory

		val smelting = inventory.smelting

		if (smelting != null && smelting.type != Material.PRISMARINE_CRYSTALS) {
			return
		}

		if (PowerMachines.getPower(sign) < this.maxPower) {
			val fuelItem = inventory.fuel ?: return
			val fuel = GeneratorFuel.getFuel(fuelItem) ?: return

			event.isBurning = true
			event.burnTime = (fuel.cooldown / speed).toInt()
			furnace.cookTime = (-1000).toShort()
			val customItem = CustomItems[fuelItem]
			if (customItem is CustomItems.GasItem) {
				val emptyCanister = CustomItems.GAS_CANISTER_EMPTY.itemStack(1)

				val location = sign.block
					.getRelative(sign.getFacing().rightFace)
					.location.toCenterLocation()

				furnace.world.dropItem(location, emptyCanister)
			}
			PowerMachines.addPower(sign, fuel.power)
			return
		} else {
			furnace.world.playEffect(furnace.location.add(0.5, 0.5, 0.5), Effect.SMOKE, 4)
		}
		event.isCancelled = true
	}

	override val name = "generator"

	override val signText = createSignText(
		line1 = "&2Power",
		line2 = "&8Generator",
		line3 = null,
		line4 = tierText
	)

	override fun LegacyMultiblockShape.buildStructure() {
		at(x = -1, y = -1, z = +0).extractor()
		at(x = +0, y = -1, z = +0).wireInputComputer()
		at(x = +1, y = -1, z = +0).extractor()
		at(x = -1, y = -1, z = +1).type(tierMaterial)
		at(x = +0, y = -1, z = +1).redstoneBlock()
		at(x = +1, y = -1, z = +1).type(tierMaterial)

		at(x = -1, y = +0, z = +0).anyGlassPane()
		at(x = +0, y = +0, z = +0).machineFurnace()
		at(x = +1, y = +0, z = +0).anyGlassPane()
		at(x = -1, y = +0, z = +1).anyGlassPane()
		at(x = +0, y = +0, z = +1).redstoneBlock()
		at(x = +1, y = +0, z = +1).anyGlassPane()
	}
}

package net.horizonsend.ion.server.features.multiblock.farming.planter

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.farming.Crop
import net.horizonsend.ion.server.features.multiblock.farming.CropMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class PlanterMultiblock(val tierMaterial: Material, tierNumber: Int) : CropMultiblock() {
	override val name: String = "planter"
	override val signText: Array<Component?> = arrayOf(
		text().append(text("Auto ", NamedTextColor.GRAY), text("Planter", NamedTextColor.GREEN)).build(),
		text().append(text("Tier ", NamedTextColor.DARK_AQUA), text(tierNumber, NamedTextColor.AQUA)).build(),
		null,
		null
	)

	override val regionRadius: Int = 1
	override val regionHeight: Int = 0

	override fun getOriginOffset(): Vec3i = Vec3i(0, -1, -5)

	private val powerPerCrop: Int = 10

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		if (furnace.inventory.smelting?.type != Material.PRISMARINE_CRYSTALS) return

		val seedItem = furnace.inventory.fuel ?: return
		val crop = Crop.findBySeed(seedItem.type) ?: return

		event.isCancelled = false
		event.isBurning = false
		event.burnTime = 20

		var planted = 0
		val initialPower = PowerMachines.getPower(sign)

		if (initialPower == 0) {
			event.burnTime = 500
			return
		}

		for (block in regionIterable(sign)) {
			if (block.type != Material.AIR) continue
			if (seedItem.amount <= 0) break
			if (!crop.canBePlanted(block)) continue

			if ((planted + 1) * powerPerCrop > initialPower) {
				event.burnTime = 500
				break
			}

			planted++
			seedItem.amount--

			crop.plant(block)
		}

		if (planted == 0) {
			event.burnTime = 500
			return
		}

		PowerMachines.removePower(sign, planted * powerPerCrop)
	}

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).anyStairs()
				x(0).noteBlock()
				x(+1).anyStairs()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).sponge()
				x(0).type(tierMaterial)
				x(+1).sponge()
			}
			y(0) {
				x(-1).anyGlass()
				x(0).type(tierMaterial)
				x(+1).anyGlass()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).sponge()
				x(0).type(tierMaterial)
				x(+1).sponge()
			}
			y(0) {
				x(-1).anyGlass()
				x(0).type(tierMaterial)
				x(+1).anyGlass()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).dispenser()
				x(0).dispenser()
				x(+1).dispenser()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).anyStairs()
				x(+1).anyStairs()
			}
		}
	}
}

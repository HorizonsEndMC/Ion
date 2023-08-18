package net.horizonsend.ion.server.features.multiblock.generator

import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import kotlin.math.roundToInt

object GasPowerPlantMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val maxPower: Int = 500000

	override val name: String = "gaspowerplant"

	override val signText: Array<Component?> = arrayOf(
		text("Gas Power Plant", NamedTextColor.RED),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
				x(+2).anyGlassPane()
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).anyWall()
				x(+0).sponge()
				x(+1).anyWall()
				x(+2).copperBlock()
			}
		}
		z(+1) {
			y(+0) {
				x(-2).copperBlock()
				x(-1).type(Material.LIGHTNING_ROD)
				x(+0).sponge()
				x(+1).type(Material.LIGHTNING_ROD)
				x(+2).copperBlock()
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).copperBlock()
			}
		}
		z(+2) {
			y(+0) {
				x(-2).anyGlass()
				x(-1).type(Material.LIGHTNING_ROD)
				x(+0).sponge()
				x(+1).type(Material.LIGHTNING_ROD)
				x(+2).anyGlass()
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).copperBlock()
			}
		}
		z(+3) {
			y(+0) {
				x(-2).anyGlass()
				x(-1).type(Material.LIGHTNING_ROD)
				x(+0).sponge()
				x(+1).type(Material.LIGHTNING_ROD)
				x(+2).anyGlass()
			}
			y(-1) {
				x(-2).anyGlass()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).anyGlass()
			}
		}
		z(+4) {
			y(+0) {
				x(-2).anyGlass()
				x(-1).type(Material.LIGHTNING_ROD)
				x(+0).sponge()
				x(+1).type(Material.LIGHTNING_ROD)
				x(+2).anyGlass()
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).copperBlock()
			}
		}
		z(+5) {
			y(+0) {
				x(-2).copperBlock()
				x(-1).type(Material.LIGHTNING_ROD)
				x(+0).sponge()
				x(+1).type(Material.LIGHTNING_ROD)
				x(+2).copperBlock()
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).copperBlock()
			}
		}
		z(+6) {
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlassPane()
				x(+0).anyPipedInventory()
				x(+1).anyGlassPane()
				x(+2).anyGlassPane()
			}
			y(-1) {
				x(-2).copperBlock()
				x(-1).ironBlock()
				x(+0).extractor()
				x(+1).ironBlock()
				x(+2).copperBlock()
			}
		}
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0
		val inventory = furnace.inventory

		println(0)

		val smelting = inventory.smelting ?: return
		val fuelItem = inventory.fuel ?: return

		val fuel = (inventory.smelting?.customItem as? GasCanister) ?: return
		val oxidizer = (inventory.fuel?.customItem as? GasCanister) ?: return

		val fuelType = fuel.gas
		val oxidizerType = oxidizer.gas

			println(1)

		if (fuelType !is GasFuel || oxidizerType !is GasOxidizer) return

		println(4)

		if (PowerMachines.getPower(sign) < this.maxPower) {
			event.isBurning = true
			event.burnTime = fuelType.cooldown
			furnace.cookTime = (-1000).toShort()

			val power = fuelType.powerPerUnit * oxidizerType.powerMultipler

			PowerMachines.addPower(sign, power.roundToInt())

			return
		} else {
			furnace.world.playEffect(furnace.location.add(0.5, 0.5, 0.5), Effect.SMOKE, 4)
		}
		event.isCancelled = true
	}
}

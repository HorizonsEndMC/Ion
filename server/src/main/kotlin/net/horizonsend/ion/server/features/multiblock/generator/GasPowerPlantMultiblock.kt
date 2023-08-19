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
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getStateIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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

	const val GAS_CONSUMED: Int = 30

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
				x(-1).extractor()
				x(+0).wireInputComputer()
				x(+1).extractor()
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
		event.isCancelled = true

		val inventory = furnace.inventory

		val fuelItem = inventory.smelting ?: return println(1)
		val oxidizerItem = inventory.fuel ?: return println(2)

		val fuel = (fuelItem.customItem as? GasCanister) ?: return println(3)
		val oxidizer = (oxidizerItem.customItem as? GasCanister) ?: return println(4)

		val fuelType = fuel.gas
		val oxidizerType = oxidizer.gas

		if (fuelType !is GasFuel || oxidizerType !is GasOxidizer) return println(5)

		val consumed = checkCanisters(sign, furnace, fuelItem, fuel, oxidizerItem, oxidizer) ?: return println(6)

		if (PowerMachines.getPower(sign) <= this.maxPower) {
			event.isBurning = true
			furnace.burnTime = fuelType.cooldown.toShort()
			furnace.cookTime = (-1000).toShort()

			val power = (fuelType.powerPerUnit * oxidizerType.powerMultipler) * consumed
			PowerMachines.addPower(sign, power.roundToInt())
		} else {
			furnace.world.playEffect(furnace.location.add(0.5, 0.5, 0.5), Effect.SMOKE, 4)
		}
	}

	private fun checkCanisters(
		sign: Sign,
		furnace: Furnace,
		fuelItem: ItemStack,
		fuelType: GasCanister,
		oxidizerItem: ItemStack,
		oxidizerType: GasCanister
	): Int? {
		val fuelFill = fuelType.getFill(fuelItem)
		val oxidizerFill = oxidizerType.getFill(oxidizerItem)

		// God forbid it goes negative
		if (fuelFill <= 0) {
			println("empty fuel")
			clearEmpty(sign, furnace.inventory, fuelItem)
			return null
		}

		if (oxidizerFill <= 0) {
			println("empty oxidizer")
			clearEmpty(sign, furnace.inventory, oxidizerItem)
			return null
		}

		// Burn fuel and oxidizer at 1:1
		// Cap consumption at 30 units
		val consumed = minOf(GAS_CONSUMED, fuelFill, oxidizerFill)

		fuelType.setFill(fuelItem, furnace.inventory, fuelFill - consumed)
		oxidizerType.setFill(oxidizerItem, furnace.inventory, oxidizerFill - consumed)

		return consumed
	}

	private fun clearEmpty(sign: Sign, furnaceInventory: Inventory, itemStack: ItemStack) {
		println("""
			clearing empty
			$furnaceInventory
			$itemStack
		""".trimIndent())

		val discardChest = getStorage(sign, outputInventory) ?: return println("DISCARD CHEST NOT FOUND")

		val noFit = discardChest.inventory.addItem(itemStack).values.isNotEmpty()

		println(noFit)

		if (noFit) return

		furnaceInventory.remove(itemStack)
	}

	private fun getStorage(sign: Sign, offset: Vec3i): Container? {
		val (x, y, z) = offset
		val facing = sign.getFacing()
		val right = facing.rightFace

		val absoluteOffset = Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)

		val absolute = absoluteOffset + Vec3i(sign.location)

		val (absoluteX, absoluteY, absoluteZ) = absolute

		return getStateIfLoaded(sign.world, absoluteX, absoluteY, absoluteZ) as? Container
	}

	private val outputInventory: Vec3i = Vec3i(0, 0, -8)
}

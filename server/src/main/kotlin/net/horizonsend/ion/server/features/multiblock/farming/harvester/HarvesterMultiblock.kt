package net.horizonsend.ion.server.features.multiblock.farming.harvester

import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.farming.Crop
import net.horizonsend.ion.server.features.multiblock.farming.CropMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.block.data.Ageable
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class HarvesterMultiblock(val tierMaterial: Material, tierNumber: Int) : CropMultiblock() {
	override val requiredPermission: String? = "ion.multiblock.harvester"
	override val name: String = "harvester"
	override val signText: Array<Component?> = arrayOf(
		Component.text().append(Component.text("Auto ", NamedTextColor.GRAY), Component.text("Harvester", NamedTextColor.GREEN)).build(),
		Component.text().append(Component.text("Tier ", NamedTextColor.DARK_AQUA), Component.text(tierNumber, NamedTextColor.AQUA)).build(),
		null,
		null
	)

	override val regionRadius: Int = 1
	override val regionHeight: Int = 0

	val powerPerCrop: Int = 10

	override fun getOriginOffset(): Vec3i = Vec3i(0, -1, -5)

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		if (furnace.inventory.smelting?.type != Material.PRISMARINE_CRYSTALS) return
		if (furnace.inventory.fuel?.type != Material.PRISMARINE_CRYSTALS) return

		val inventory = getInventory(sign) ?: return
		event.isCancelled = false
		event.isBurning = false
		event.burnTime = 20

		var broken = 0
		val initialPower = PowerMachines.getPower(sign)

		if (initialPower == 0) {
			event.burnTime = 500
			return
		}

		for (block in regionIterable(sign)) {
			val data = block.blockData

			if (data !is Ageable) continue
			if (data.age != data.maximumAge) continue
			val crop = Crop[block.type] ?: continue

			val drops = crop.getDrops(block).toTypedArray()

			for (item in drops) {
				if (!LegacyItemUtils.canFit(inventory, item)) {
					event.burnTime = 500
					break
				}
			}

			if ((broken + 1) * powerPerCrop > initialPower) {
				event.burnTime = 500
				break
			}

			crop.harvest(block)
			broken++

			val didNotFit = inventory.addItem(*drops)

			if (didNotFit.isNotEmpty()) {
				event.burnTime = 500
				break
			}
		}

		PowerMachines.removePower(sign, broken * powerPerCrop)
	}

	private fun getInventoryOffset(): Vec3i = Vec3i(0, 0, -3)
	private fun getInventory(sign: Sign): Inventory? {
		val (x, y, z) = getInventoryOffset()
		val facing = sign.getFacing()
		val right = facing.rightFace

		val offset =  Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)

		val (posX, posY, posZ) = offset + Vec3i(sign.location)

		val block = getBlockIfLoaded(sign.world, posX, posY, posZ) ?: return null
		return (block.state as? InventoryHolder)?.inventory
	}

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).ironBlock()
				x(0).noteBlock()
				x(+1).ironBlock()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).anyGlassPane()
				x(0).type(tierMaterial)
				x(+1).anyGlassPane()
			}
			y(0) {
				x(-1).anyGlassPane()
				x(0).type(tierMaterial)
				x(+1).anyGlassPane()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).dispenser()
				x(0).dispenser()
				x(+1).dispenser()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).anyPipedInventory()
				x(+1).ironBlock()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).type(Material.STONECUTTER)
				x(0).type(Material.STONECUTTER)
				x(+1).type(Material.STONECUTTER)
			}
			y(0) {
				x(-1).anyStairs()
				x(0).anyPipedInventory()
				x(+1).anyStairs()
			}
		}
	}
}

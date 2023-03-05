package net.horizonsend.ion.server.legacy.multiblocks

import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.getFacing
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

object DisposalMultiblock : PowerStoringMultiblock(), FurnaceMultiblock {
	var amountCleared = 0

	override val maxPower: Int = 150_000

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).type(Material.FURNACE)
				x(+0).type(Material.CHEST)
				x(+1).noteBlock()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).anyGlass()
				x(+1).anyStairs()
			}
			y(+1) {
				x(0).anySlab()
			}
			z(+1) {
				y(-1) {
					x(-1).stainedTerracotta()
					x(+0).sponge()
					x(+1).stainedTerracotta()
				}
				y(+0) {
					x(-1).anyGlassPane()
					x(+0).type(Material.MAGMA_BLOCK)
					x(+1).anyGlassPane()
				}
				y(+1) {
					x(-1).anySlab()
					x(0).anySlab()
					x(+1).anySlab()
				}
			}
			z(+2) {
				y(-1) {
					x(-1).anyStairs()
					x(+0).redstoneBlock()
					x(+1).anyStairs()
				}
				y(+0) {
					x(-1).goldBlock()
					x(+0).anyGlassPane()
					x(+1).goldBlock()
				}
				y(+1) {
					x(0).anySlab()
				}
			}
		}
	}

	override val name = "charger"

	override var signText = createSignText(
		line1 = "&14Disposal",
		line2 = null,
		line3 = null,
		line4 = amountCleared.toString()
	)

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		event.isBurning = false
		event.burnTime = 0
		val chest = sign.block.getRelative(sign.getFacing().oppositeFace).getRelative(0, -1, 0)
		val inventory = (chest as? Chest)?.inventory ?: return
		val furnaceInventory = furnace.inventory
		val smelting = furnaceInventory.smelting
		val power = PowerMachines.getPower(sign)
		if (smelting == null || smelting.type != Material.PRISMARINE_CRYSTALS) {
			return
		}
		if (power == 0) {
			return
		}
		var amountToClear = 0
		inventory.storageContents.forEach {
			if (it != null){
				amountToClear+=it.amount
			}
		}
		inventory.clear()
		PowerMachines.setPower(sign, power - 3*amountToClear)
		amountCleared += amountToClear
		furnace.cookTime = 20.toShort()
		event.isCancelled = false
		event.isBurning = false
		event.burnTime = 20
		signText[4] = MiniMessage.miniMessage().deserialize("<gray>$amountCleared")
	}
}

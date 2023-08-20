package net.horizonsend.ion.server.features.multiblock.gas

import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent

object VentMultiblock : Multiblock(), FurnaceMultiblock {
	override val name: String = "vent"

	override val signText: Array<Component?> = arrayOf(
		Component.text()
			.append(Component.text("Gas", NamedTextColor.RED))
			.append(Component.text(" Vent", NamedTextColor.GOLD))
			.build(),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).lightningRod()
				x(+0).ironBlock()
				x(+1).lightningRod()
			}
			y(+0) {
				x(-1).lightningRod()
				x(+0).machineFurnace()
				x(+1).lightningRod()
			}
		}
		z(+1) {
			y(-1) {
				x(0).lightningRod()
			}
			y(+0) {
				x(0).lightningRod()
			}
		}
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		if (furnace.inventory.smelting?.type != Material.PRISMARINE_CRYSTALS) return

		val fuel = furnace.inventory.fuel ?: return
		val customItem = fuel.customItem ?: return
		if (customItem !is GasCanister) return

		furnace.inventory.fuel = null
		furnace.inventory.result = Gasses.EMPTY_CANISTER
	}
}

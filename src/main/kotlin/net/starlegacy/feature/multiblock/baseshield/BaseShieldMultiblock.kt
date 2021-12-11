package net.starlegacy.feature.multiblock.baseshield

import net.starlegacy.feature.machine.BaseShields
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent

abstract class BaseShieldMultiblock : PowerStoringMultiblock(), FurnaceMultiblock {
    override val name = "baseshield"

    abstract val radius: Int

    protected fun MultiblockShape.sideRings(vararg xAxes: Int) {
        for (x in xAxes) {
            z(+0) {
                y(-1) { x(x).anyStairs() }
                y(+0) { x(x).stoneBrick() }
                y(+1) { x(x).anyStairs() }
            }

            z(+1) {
                y(-1) { x(x).stoneBrick() }
                y(+0) { x(x).sponge() }
                y(+1) { x(x).stoneBrick() }
            }

            z(+2) {
                y(-1) { x(x).anyStairs() }
                y(+0) { x(x).stoneBrick() }
                y(+1) { x(x).anyStairs() }
            }
        }
    }

    override fun onTransformSign(player: Player, sign: Sign) {
        BaseShields.setBaseShieldEnabled(sign, false)
        super.onTransformSign(player, sign)
    }

    override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
        if (BaseShields.isBaseShieldDisabled(sign)) {
            return
        }

        event.isBurning = false
        event.burnTime = 0
        event.isCancelled = true

        val smelting = furnace.inventory.smelting ?: return
        val fuel = furnace.inventory.fuel ?: return

        if (smelting.type != Material.PRISMARINE_CRYSTALS || fuel.type != Material.PRISMARINE_CRYSTALS) {
            return
        }

        val power = PowerMachines.getPower(sign, true)

        if (power <= 0) {
            BaseShields.setBaseShieldEnabled(sign, false)
            return
        }

        if (power < 1000) {
            return
        }

        event.isBurning = false
        event.burnTime = BaseShields.REGEN_TICK_INTERVAL
        furnace.cookTime = (-1000).toShort()
        event.isCancelled = false

        BaseShields.regenerateBaseShield(sign, radius)
    }
}

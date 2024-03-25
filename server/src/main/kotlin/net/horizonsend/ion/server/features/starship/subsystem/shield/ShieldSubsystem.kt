package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.multiblock.particleshield.ShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.stripColor
import org.bukkit.block.Block
import org.bukkit.block.Sign
import kotlin.math.pow
import kotlin.math.roundToInt

abstract class ShieldSubsystem(
	starship: ActiveStarship,
	sign: Sign,
	multiblock: ShieldMultiblock
) : AbstractMultiblockSubsystem<ShieldMultiblock>(starship, sign, multiblock) {
	val name: String = sign.getLine(2).stripColor()
	var destroyed = false

	val maxShields: Double = (0.00671215 * starship.initialBlockCount.toDouble().pow(0.836512) - 0.188437)
		get() = if (starship.initialBlockCount < 500) (1 - field + (1)) else field

	val maxPower: Int = (starship.initialBlockCount.coerceAtLeast(500).d().pow(3.0 / 5.0) * 10000.0).roundToInt()
		get() = if (starship.shields.size > maxShields) {
			(field * ((maxShields / starship.shields.size) * starship.balancing.shieldPowerMultiplier)).toInt()
		}
		else {
			(field * starship.balancing.shieldPowerMultiplier).toInt()
		}

	var power: Int = maxPower
		set(value) {
			checkDestroyed()
			field = value.coerceIn(0, maxPower)
		}
	var isReinforcementEnabled = multiblock.isReinforced

	fun isReinforcementActive(): Boolean {
		return isReinforcementEnabled && powerRatio > 0.8
	}

	val powerRatio: Double get() = power.toDouble() / maxPower.toDouble()

	fun getPowerUsage(power: Double): Int {
		return (power * 3000.0).toInt()
	}

	private fun checkDestroyed() {
		if (destroyed) return
		if (!isIntact()) destroyed =  true
	}

	abstract fun containsBlock(block: Block): Boolean
}

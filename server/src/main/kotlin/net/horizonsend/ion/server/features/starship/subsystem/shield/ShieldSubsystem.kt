package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.multiblock.type.particleshield.ShieldMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.stripColor
import org.bukkit.World
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

	open val maxPower: Int = (starship.initialBlockCount.d().pow(3.0 / 5.0) * 10000.0).roundToInt()
		get() = if (starship.shields.size > starship.maxShields) {
			(field * ((starship.maxShields / starship.shields.size) * starship.balancing.shieldPowerMultiplier)).toInt()
		}
		else {
			(field * starship.balancing.shieldPowerMultiplier).toInt()
		}

	// Abstract so max power can be safely overriden
	abstract var power: Int

	var pastPower : Int = maxPower
		set(value) {field = value.coerceIn(0, maxPower)}


	var recentDamage : Double = 0.0
		set(value) {
			field = value.coerceIn(0.0, maxPower.toDouble())
		}

	var isReinforcementEnabled = multiblock.isReinforced

	fun isReinforcementActive(): Boolean {
		return isReinforcementEnabled && powerRatio > 0.8
	}

	val powerRatio: Double get() = power.toDouble() / maxPower.toDouble()

	fun getPowerUsage(power: Double): Int {
		return (power * 3000.0).toInt()
	}

	fun containsBlock(block: Block): Boolean = containsPosition(block.world, Vec3i(block.x, block.y, block.z))
	abstract fun containsPosition(world: World, blockPos: Vec3i): Boolean
}

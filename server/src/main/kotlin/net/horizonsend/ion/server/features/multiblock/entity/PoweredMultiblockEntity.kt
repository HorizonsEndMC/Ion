package net.horizonsend.ion.server.features.multiblock.entity

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

abstract class PoweredMultiblockEntity(
	x: Int,
	y: Int,
	z: Int,
	world: World,
	type: Multiblock,
	signOffset: BlockFace,
	private var power: Int
) : MultiblockEntity(x, y, z, world, type, signOffset) {
	init {
	    require(type is PowerStoringMultiblock)
	}

	val maxPower = (type as PowerStoringMultiblock).maxPower

	private val prefixComponent = Component.text("E: ", NamedTextColor.YELLOW)

	fun setPower(amount: Int) {
		val correctedPower = amount.coerceIn(0, maxPower)

		power = correctedPower
	}

	fun getPower(): Int {
		return power
	}

	/**
	 * Returns the amount of power that could not be added
	 **/
	fun addPower(sign: Sign, amount: Int): Int {
		val newAmount = getPower() + amount

		setPower(newAmount)

		return if (newAmount > maxPower) maxPower - newAmount else 0
	}

	/**
	 * Returns the amount of power that could not be removed
	 **/
	fun removePower(amount: Int): Int {
		val newAmount = getPower() - amount

		setPower(newAmount)

		return if (newAmount < 0) newAmount else 0
	}

	/**
	 * Returns whether this multiblock has the capacity to fit the specified amount of power
	 **/
	fun canFitPower(amount: Int): Boolean {
		return getPower() + amount < maxPower
	}

	/**
	 * Returns true if this amount of power can be removed without reaching zero.
	 **/
	fun canRemovePower(amount: Int): Boolean {
		return getPower() - amount > 0
	}
}

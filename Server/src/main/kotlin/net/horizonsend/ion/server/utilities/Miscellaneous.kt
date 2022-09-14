package net.horizonsend.ion.server.utilities

import net.milkbowl.vault.economy.Economy
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import java.util.EnumSet

inline fun vaultEconomy(execute: (Economy) -> Unit) {
	execute(Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider ?: return)
}

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }

fun blockKeyFrom(x: Int, y: Int, z: Int): Long {
	return x.toLong() and 0x3FFFFFF shl 38 or (z.toLong() and 0x3FFFFFF shl 12) or (y.toLong() and 0xFFF)
}

fun Position<Int>.toBlockKey(): Long = blockKeyFrom(x, y, z)

fun Position<Int>.toBlockPos(): BlockPos = BlockPos(x, y, z)

fun Long.toIntPosition(): Position<Int> {
	var x = this ushr 38
	var y = this shl 52 ushr 52
	var z = this shl 26 ushr 38

	if (x >= 1 shl 25) x -= 1 shl 26
	if (y >= 1 shl 11) y -= 1 shl 12
	if (z >= 1 shl 25) z -= 1 shl 26

	return Position(x.toInt(), y.toInt(), z.toInt())
}
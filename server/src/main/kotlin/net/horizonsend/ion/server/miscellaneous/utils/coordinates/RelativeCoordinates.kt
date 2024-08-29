package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

fun toAbsolute(forwardFace: BlockFace, backFourth: Int, leftRight: Int, upDown: Int): Vec3i {
	val rightFace = forwardFace.rightFace

	return Vec3i(
		x = rightFace.modX * leftRight + forwardFace.modX * backFourth,
		y = upDown,
		z = rightFace.modZ * leftRight + forwardFace.modZ * backFourth
	)
}

fun getRelative(origin: Vec3i, forwardFace: BlockFace, forward: Int, right: Int, up: Int): Vec3i {
	val relative = toAbsolute(forwardFace, forward, right, up)

	return origin + relative
}

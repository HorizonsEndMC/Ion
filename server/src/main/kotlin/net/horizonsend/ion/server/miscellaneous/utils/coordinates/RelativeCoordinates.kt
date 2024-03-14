package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

fun toAbsolute(inwardFace: BlockFace, backFourth: Int, leftRight: Int, upDown: Int): Vec3i {
	val rightFace = inwardFace.rightFace

	return Vec3i(
		x = rightFace.modX * leftRight + inwardFace.modX * backFourth,
		y = upDown,
		z = rightFace.modZ * leftRight + inwardFace.modZ * backFourth
	)
}

fun getRelative(origin: Vec3i, face: BlockFace, backFourth: Int, leftRight: Int, upDown: Int): Vec3i {
	val relative = toAbsolute(face, backFourth, leftRight, upDown)

	return origin + relative
}

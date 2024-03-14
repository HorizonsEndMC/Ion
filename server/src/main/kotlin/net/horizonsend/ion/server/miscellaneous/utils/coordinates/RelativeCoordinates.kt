package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

fun toAbsolute(face: BlockFace, backFourth: Int, leftRight: Int, upDown: Int): Vec3i {
	val right = face.rightFace

	return Vec3i(
		x = (right.modX * leftRight) + (face.modX * backFourth),
		y = upDown,
		z = (right.modZ * leftRight) + (face.modZ * backFourth)
	)
}

fun getRelative(origin: Vec3i, face: BlockFace, backFourth: Int, leftRight: Int, upDown: Int): Vec3i {
	val relative = toAbsolute(face, backFourth, leftRight, upDown)

	return origin + relative
}

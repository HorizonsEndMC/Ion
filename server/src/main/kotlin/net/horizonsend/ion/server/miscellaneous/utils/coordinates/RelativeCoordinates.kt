package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

fun toAbsolute(forwardFace: BlockFace, right: Int, up: Int, forward: Int): Vec3i {
	val rightFace = forwardFace.rightFace

	return Vec3i(
		x = rightFace.modX * right + forwardFace.modX * forward,
		y = up,
		z = rightFace.modZ * right + forwardFace.modZ * forward
	)
}

fun getRelative(origin: Vec3i, forwardFace: BlockFace, right: Int, up: Int, forward: Int): Vec3i {
	val relative = toAbsolute(forwardFace, right, up, forward)

	return origin + relative
}

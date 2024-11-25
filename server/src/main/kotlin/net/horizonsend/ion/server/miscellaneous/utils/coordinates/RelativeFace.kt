package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

enum class RelativeFace {
    FORWARD {
        override fun get(face: BlockFace): BlockFace {
            return face
        }
    },
    BACKWARD {
        override fun get(face: BlockFace): BlockFace {
            return face.oppositeFace
        }
    },
    RIGHT {
        override fun get(face: BlockFace): BlockFace {
            return face.rightFace
        }
    },
    LEFT {
        override fun get(face: BlockFace): BlockFace {
            return face.leftFace
        }
    };

    abstract operator fun get(face: BlockFace): BlockFace

	companion object {
		operator fun get(forward: BlockFace, relative: BlockFace): RelativeFace {
			return when {
				forward == relative -> FORWARD
				forward.oppositeFace == relative -> BACKWARD
				forward.rightFace == relative -> RIGHT
				forward.leftFace == relative -> LEFT
				else -> throw IllegalArgumentException("Unsupported relationship! Forward: $forward, relative: $relative.")
			}
		}
	}
}

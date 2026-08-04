package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

enum class RelativeFace {
    FORWARD {
		override val opposite: RelativeFace get() = BACKWARD

		override fun get(face: BlockFace): BlockFace {
            return face
        }
    },
    BACKWARD {
		override val opposite: RelativeFace get() = FORWARD

		override fun get(face: BlockFace): BlockFace {
            return face.oppositeFace
        }
    },
    RIGHT {
		override val opposite: RelativeFace get() = LEFT

		override fun get(face: BlockFace): BlockFace {
            return face.rightFace
        }
    },
    LEFT {
		override val opposite: RelativeFace get() = RIGHT

        override fun get(face: BlockFace): BlockFace {
            return face.leftFace
        }
    },
    UP {
		override val opposite: RelativeFace get() = DOWN

		override fun get(face: BlockFace): BlockFace {
            return BlockFace.UP
        }
    },
    DOWN {
		override val opposite: RelativeFace get() = UP

        override fun get(face: BlockFace): BlockFace {
            return BlockFace.DOWN
        }
    };

	abstract val opposite: RelativeFace

    abstract operator fun get(face: BlockFace): BlockFace

	companion object {
		operator fun get(forward: BlockFace, relative: BlockFace): RelativeFace {
			return when {
				forward == BlockFace.SELF -> FORWARD
				forward == relative -> FORWARD
				forward.oppositeFace == relative -> BACKWARD
				forward.rightFace == relative -> RIGHT
				forward.leftFace == relative -> LEFT
				relative == BlockFace.UP -> UP
				relative == BlockFace.DOWN -> DOWN
				else -> throw IllegalArgumentException("Unsupported relationship! Forward: $forward, relative: $relative.")
			}
		}
	}
}

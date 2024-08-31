package net.horizonsend.ion.server.miscellaneous.utils.coordinates

import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace

enum class RelativeFace {
    SELF {
        override fun get(face: BlockFace): BlockFace {
            return face
        }
    },
    OPPOSITE {
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
}

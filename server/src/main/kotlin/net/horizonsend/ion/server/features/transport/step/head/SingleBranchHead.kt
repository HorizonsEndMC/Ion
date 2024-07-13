package net.horizonsend.ion.server.features.transport.step.head

import net.horizonsend.ion.server.features.transport.network.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import org.bukkit.block.BlockFace

/**
 * Can either branch off of a MultiBranch or exist alone
 **/
interface SingleBranchHead<T: ChunkTransportNetwork> : BranchHead<T> {
	/** The last offset this branch moved */
	var lastDirection: BlockFace

	/** The current location of the tip of the branch */
	var currentNode: TransportNode
}

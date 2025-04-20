package net.horizonsend.ion.server.features.transport.nodes

import net.horizonsend.ion.server.features.transport.nodes.util.Path
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class PathfindResult(val destinationPosition: BlockKey, val trackedPath: Path)

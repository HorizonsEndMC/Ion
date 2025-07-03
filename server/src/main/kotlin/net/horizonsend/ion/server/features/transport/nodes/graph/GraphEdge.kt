package net.horizonsend.ion.server.features.transport.nodes.graph

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getPointsBetween
import org.bukkit.util.Vector
import kotlin.math.roundToInt

interface GraphEdge {
	val nodeOne: TransportNode
	val nodeTwo: TransportNode

	fun getDisplayPoints(): List<Vector> {
		val distance = maxOf(1, distance(nodeOne.getCenter(), nodeTwo.getCenter()).roundToInt()) * 3
		return getPointsBetween(nodeOne.getCenter(), nodeTwo.getCenter(), distance)
	}
}

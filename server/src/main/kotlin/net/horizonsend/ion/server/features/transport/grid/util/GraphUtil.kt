package net.horizonsend.ion.server.features.transport.grid.util

import com.google.common.graph.Graph
import java.util.LinkedList

@Suppress("UnstableApiUsage")
fun <T> separateGraphs(graph: Graph<T>): List<Set<T>> {
	val seen: MutableSet<T> = HashSet()
	val stack = LinkedList<T>()
	val separated: MutableList<Set<T>> = LinkedList()

	while (true) {
		var first: T? = null

		// Find next node in graph we haven't seen.
		for (node in graph.nodes()) {
			if (!seen.contains(node)) {
				first = node
				break
			}
		}

		// We have discovered all nodes, exit.
		if (first == null) break

		// Start recursively building out all nodes in this sub-graph
		val subGraph: MutableSet<T> = HashSet()

		stack.push(first)

		while (!stack.isEmpty()) {
			val entry = stack.pop()

			if (seen.contains(entry)) continue

			stack.addAll(graph.adjacentNodes(entry))
			seen.add(entry)
			subGraph.add(entry)
		}

		separated.add(subGraph)
	}

	return separated
}

package net.horizonsend.ion.server.features.transport.old

import dev.cubxity.plugins.metrics.api.metric.collector.CollectorCollection
import dev.cubxity.plugins.metrics.api.metric.collector.Histogram

object IonMetricsCollection: CollectorCollection {
	val timeSpent = Histogram("pipes_sync_processing_time")

	override val collectors = listOf(timeSpent)
}

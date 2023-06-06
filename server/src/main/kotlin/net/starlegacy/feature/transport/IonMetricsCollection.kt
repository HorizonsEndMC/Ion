package net.starlegacy.feature.transport

import dev.cubxity.plugins.metrics.api.metric.collector.CollectorCollection
import dev.cubxity.plugins.metrics.api.metric.collector.Histogram

object IonMetricsCollection: CollectorCollection {
	val timeSpent = Histogram("pipes_sync_processing_time")

	override val collectors = listOf(timeSpent)
}

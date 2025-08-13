package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.apache.commons.lang.math.DoubleRange

@Serializable
data class WeaponSet(val name: String, private val engagementRangeMin: Double, private val engagementRangeMax: Double) {
	@Transient
	val engagementRange = DoubleRange(engagementRangeMin, engagementRangeMax)
}

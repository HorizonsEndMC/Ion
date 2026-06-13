package net.horizonsend.ion.server.features.sequences.phases

import net.horizonsend.ion.common.utils.text.formatObjectiveLocation
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component

class PhaseDescription(
    val description: Component,
    val position: Vec3i? = null
) {
    fun formattedDescription(context: SequenceContext): Component {
        if (position == null) return description

        val absolutePosition = position.plus(context.getOrigin())
        return template(
            description,
            formatObjectiveLocation(absolutePosition.x, absolutePosition.y, absolutePosition.z)
        )
    }
}
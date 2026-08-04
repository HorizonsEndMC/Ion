package net.horizonsend.ion.server.features.transport.fluids

import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.FluidDisplayModule.Companion.format
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object FluidUtils {
	fun formatFluidInfo(fluidStack: FluidStack): Component {
		val text = text()
		text.append(fluidStack.type.getValue().displayName)
		text.append(Component.space(), bracketed(ofChildren(text(format.format(fluidStack.amount), NamedTextColor.GRAY), text("L", NamedTextColor.GRAY))))

		var lines = 0

		for ((key, property) in fluidStack.getDataMap()) {
			lines++

			text.append(Component.newline())
			text.append(text(" • ", HE_MEDIUM_GRAY))
			text.append(key.getDisplayName())
			text.append(text(": ", HE_DARK_GRAY))
			text.append(key.formatValueUnsafe(property))
		}

		return text.build()
	}
}

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
	const val GAS_CONSTANT = 0.08206

	fun formatFluidInfo(fluidStack: FluidStack): Component {
		val text = text()
		text.append(fluidStack.getDisplayName())
		text.append(Component.space(), bracketed(ofChildren(text(format.format(fluidStack.amount), NamedTextColor.GRAY), text("L", NamedTextColor.GRAY))))

		text.append(formatFluidProperties(fluidStack))

		return text.build()
	}

	fun formatFluidProperties(fluidStack: FluidStack): List<Component> {
		val built = mutableListOf<Component>()
		for ((key, property) in fluidStack.getCustomDataMap()) {
			val text = text()
			text.append(Component.newline())
			text.append(text(" • ", HE_MEDIUM_GRAY))
			text.append(key.getDisplayName())
			text.append(text(": ", HE_DARK_GRAY))
			text.append(key.formatValueUnsafe(property))
			built.add(text.build())
		}

		return built
	}
}

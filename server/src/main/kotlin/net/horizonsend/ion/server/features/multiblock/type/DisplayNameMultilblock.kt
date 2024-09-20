package net.horizonsend.ion.server.features.multiblock.type

import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.kyori.adventure.text.Component

interface DisplayNameMultilblock {
	val displayName: Component

	companion object {
		fun Multiblock.getDisplayName() = if (this is DisplayNameMultilblock) displayName else javaClass.simpleName.toComponent()
	}
}

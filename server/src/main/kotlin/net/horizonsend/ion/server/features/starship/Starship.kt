package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.miscellaneous.mainThreadCheck
import net.minecraft.server.level.ServerLevel

open class Starship(serverLevel: ServerLevel) {
	open var serverLevel = serverLevel
		set(value) {
			mainThreadCheck()
			field = value
		}
}

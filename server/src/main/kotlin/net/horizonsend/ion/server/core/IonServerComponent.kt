package net.horizonsend.ion.server.core

import net.horizonsend.ion.common.IonComponent
import org.bukkit.event.Listener

abstract class IonServerComponent(
	val runAfterTick: Boolean = false
) : Listener, IonComponent()

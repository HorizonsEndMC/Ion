package net.horizonsend.ion.core

interface Bridge {
	val isIonPresent: Boolean
}

class DummyBridge: Bridge {
	override val isIonPresent: Boolean = false
}

var bridge: Bridge = DummyBridge()
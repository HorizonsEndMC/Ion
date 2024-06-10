package net.horizonsend.ion.server.features.gui.custom

/**
 * Represents an element of the GUI that should be notified of a change in somewhere else in the GUI
 **/
interface ChangeListener {
	fun handleChange()
}

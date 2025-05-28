package net.horizonsend.ion.server.gui.invui

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.changeTitle
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import java.util.UUID

abstract class InvUIWindowWrapper(val viewer: Player, val async: Boolean = false) : CommonGuiWrapper {
	private var isInitalized = false

	open fun firstTimeSetup() {}

	/**
	 * If this window was opened from another window, that can be tracked.
	 **/
	protected open var parentWindow: CommonGuiWrapper? = null

	/**
	 * Marks the provided gui wrapper as the parent of this window.
	 **/
	fun setParent(gui: CommonGuiWrapper) {
		parentWindow = gui
	}

	fun openGui(parent: CommonGuiWrapper?) {
		parent?.let(::setParent)
		openGui()
	}

	protected fun getParent(): CommonGuiWrapper? = parentWindow

	/**
	 * Builds the window that this class wraps.
	 **/
	abstract fun buildWindow(): Window?

	/**
	 * Builds the window title Component.
	 **/
	abstract fun buildTitle(): Component

	/**
	 * Re-builds and applies the window title.
	 **/
	fun refreshTitle() {
		currentWindow?.changeTitle(buildTitle())
	}

	private val trackedButtons = mutableMapOf<UUID, Item>()

	/**
	 * Tracks this button, making it able to be updated by this GUI
	 **/
	fun <T: Item> T.tracked(): T {
		val id = UUID.randomUUID()
		trackedButtons[id] = this
		return this
	}

	/**
	 * Tracks this button, making it able to be updated by this GUI
	 **/
	fun <T: Item> tracked(buttonBuilder: (UUID) -> T): T {
		val id = UUID.randomUUID()
		val button = buttonBuilder.invoke(UUID.randomUUID())
		trackedButtons[id] = button
		return button
	}

	/**
	 * Refreshes all the tracked buttons.
	 * This is useful if, for example, one button updates the states of many.
	 **/
	fun refreshButtons() {
		trackedButtons.values.forEach(Item::notifyWindows)
	}

	/**
	 * Refreshes all the tracked buttons, excluding the calling UUID.
	 * This is useful if, for example, one button updates the states of many.
	 **/
	fun refreshButtons(calling: UUID) {
		trackedButtons.keys.minus(calling).forEach {
			val button = trackedButtons[it] ?: return@forEach
			button.notifyWindows()
		}
	}

	/**
	 * Refreshes the title, and all buttons.
	 **/
	fun refreshAll() {
		refreshTitle()
		refreshButtons()
	}

	protected var currentWindow: Window? = null

	fun getOpenWindow() = currentWindow

	fun performSetup() {
		if (!isInitalized) {
			isInitalized = true
			firstTimeSetup()
		}
	}

	override fun openGui() {
		if (async) {
			Tasks.async {
				performSetup()

				currentWindow = buildWindow()
				Tasks.sync { currentWindow?.open() }
			}
		} else {
			Tasks.sync {
				performSetup()

				currentWindow = buildWindow()
				currentWindow?.open()
			}
		}
	}

	/**
	 * Builds a simple single window containing this GUI
	 **/
	fun normalWindow(gui: Gui): Window = Window.single()
		.setViewer(viewer)
		.setGui(gui)
		.setTitle(buildTitle())
		.build()

	fun parentOrBackButton(icon: GuiItem = GuiItem.CANCEL) =
		if (parentWindow == null) GuiItems.closeMenuItem(viewer, icon)
		else icon.makeItem(text("Go Back to Previous Menu")).makeGuiButton { _, _ -> getParent()?.openGui() }
}

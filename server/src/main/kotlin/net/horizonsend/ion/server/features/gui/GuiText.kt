package net.horizonsend.ion.server.features.gui

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.common.utils.set
import net.horizonsend.ion.common.utils.text.DEFAULT_BACKGROUND_CHARACTER
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.GUI_HEADER_MARGIN
import net.horizonsend.ion.common.utils.text.GUI_MARGIN
import net.horizonsend.ion.common.utils.text.SHIFT_DOWN_MIN
import net.horizonsend.ion.common.utils.text.SLOT_OVERLAY_WIDTH
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.leftShift
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.shift
import net.horizonsend.ion.common.utils.text.shiftToLine
import net.horizonsend.ion.common.utils.text.shiftToStartOfComponent
import net.horizonsend.ion.common.utils.text.slotOverlay
import net.horizonsend.ion.common.utils.text.yFontKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.WHITE

class GuiText(
    private val name: String,
    private val guiWidth: Int = DEFAULT_GUI_WIDTH,
    private val initialShiftDown: Int = GUI_HEADER_MARGIN
) {

    /**
     * The list of GuiComponents added to this GuiText
     */
    private val guiComponents = mutableListOf<GuiComponent>()

    /**
     * The backgrounds added to this GuiText
     */
    private val guiBackgrounds = mutableListOf<GuiBackground>()

    /**
     * List of chars that indicate if a slot should have an overlay. Similar to setStructure in InvUI.
     */
    private val slotOverlayStructure = mutableListOf<String>()

    /**
     * List of chars that indicate if a slot should have an overlay. Similar to setStructure in InvUI.
     */
    private val iconStructure = mutableListOf<List<Char>>()
    private val iconMap = mutableMapOf<Char, GuiIcon>('.' to GuiIcon.EMPTY)
    private val iconindexes = HashBasedTable.create<Int, Int, GuiIcon>()

	private val guiBorders = mutableListOf<GuiBorder>()

    /**
     * Adds a GuiComponent to the GuiText
     * @param component the GuiComponent to add
     */
    fun add(component: GuiComponent): GuiText {
        val index = guiComponents.indexOfLast { it.isOccupied(component) }

        if (index >= 0) {
            // an element with the same line and alignment was found; replace it
            guiComponents[index] = component
        } else {
            // append element to the end of the list
            guiComponents.add(component)
        }

		return this
    }

    /**
     * Adds a GuiComponent to the GuiText
     * @param component the text representation of the GuiComponent
     * @param line the line that the new GuiComponent should be set at
     * @param alignment the alignment that the new GuiComponent should be set at
     * @param horizontalShift the amount of horizontal shift to be applied
     * @param verticalShift the amount of vertical shift to be applied
     */
    fun add(
        component: Component,
        line: Int = 0,
        alignment: TextAlignment = TextAlignment.LEFT,
        horizontalShift: Int = 0,
        verticalShift: Int = 0
    ): GuiText {
        add(GuiComponent(component, line, alignment, horizontalShift, verticalShift))
		return this
    }

    /**
     * Adds a default GuiBackground to the GuiText
     */
    fun addBackground(): GuiText {
        addBackground(GuiBackground())
		return this
    }

    /**
     * Adds a GuiBackground to the GuiText
     * @param background the GuiBackground to add
     */
    fun addBackground(background: GuiBackground): GuiText {
        guiBackgrounds.add(background)
		return this
    }

    /**
     * Removes a GuiComponent at the specified line and alignment
     * @param line the line to remove the GuiComponent from
     * @param alignment the column to remove the GuiComponent from
     * @return `true` if any elements were removed
     */
    fun remove(line: Int, alignment: TextAlignment) = guiComponents.removeIf { it.isOccupied(line, alignment) }

    /**
     * Removes all GuiComponents on a line
     * @param line the line to remove the GuiComponents from
     * @return `true` if any elements were removed
     */
    fun removeLine(line: Int) = guiComponents.removeIf { it.line == line }

    /**
     * Removes all GuiComponents on a column
     * @param alignment the alignment to remove the GuiComponents from
     * @return `true` if any elements were removed
     */
    fun removeColumn(alignment: TextAlignment) = guiComponents.removeIf { it.alignment == alignment }

    /**
     * Sets the slot overlays in the GUI
     *
     * '.' - empty
     *
     * '#' - fully covered slot
     * @param structureData list of strings indicating what each slot should be covered with
     */
    fun setSlotOverlay(vararg structureData: String): GuiText {
        slotOverlayStructure.clear()
        for (row in structureData) {
            val sanitizedRow = row.replace(" ", "").replace("\n", "")
            slotOverlayStructure.add(sanitizedRow)

        }

		return this
    }

    /**
     * Sets the slot overlays in the GUI. The provided characters will be referenced from ones added to the icon mapping
     * @param structureData list of strings indicating what each slot should be covered with
     */
    fun setGuiIconOverlay(vararg structureData: String): GuiText {
        iconStructure.clear()
        for (row in structureData) {
            val sanitizedRow = row.replace(" ", "").replace("\n", "").toList()
			iconStructure.add(sanitizedRow)
        }

		return this
    }

	/**
	 * Marks the character to be displayed as the provided icon
	 */
	fun addIcon(key: Char, icon: GuiIcon): GuiText {
		iconMap[key] = icon
		return this
	}

	/**
	 * Marks the character to be displayed as the provided icon
	 */
	fun setIcon(rowIndex: Int, columnIndex: Int, icon: GuiIcon): GuiText {
		iconindexes[rowIndex, columnIndex] = icon
		return this
	}

	fun addBorder(border: GuiBorder): GuiText {
		guiBorders.add(border)
		return this
	}

    /**
     * Builds the GuiText, returning a Component that can be placed in an Inventory's title
     * @return an Adventure Component for use in an Inventory
     */
    fun build(): Component {
        val renderedComponents = mutableListOf<Component>()

		for (border in guiBorders) {
			renderedComponents.add(border.build())
		}

        // add GUI background. this operation must be performed first as subsequent text components will be placed on
        // top of previous components
        for (background in guiBackgrounds) {
            renderedComponents.add(buildGuiBackground(background))
        }

        // add GUI header
        renderedComponents.add(buildGuiHeader(name))

        // parse slot overlay structure and add overlay components
        for ((index, slotOverlayRow) in slotOverlayStructure.withIndex()) {
            val slotOverlayComponents = mutableListOf<Component>()
            val line = index * 2 // slots only on even line

            for (char in slotOverlayRow) {
                slotOverlayComponents.add(when (char) {
                    '.' -> shift(SLOT_OVERLAY_WIDTH)
                    '#' -> slotOverlay(line)
                    else -> Component.empty()
                })
            }

            renderedComponents.add(Component.textOfChildren(*slotOverlayComponents.toTypedArray()).shiftToStartOfComponent())
        }

        // parse slot overlay structure and add overlay components
        for ((index, inputOverlayRow) in iconStructure.withIndex()) {
            val inputBoxComponents = mutableListOf<Component>()
            val line = index * 2 // slots only on even line

            for (key in inputOverlayRow) {
				val icon = iconMap[key] ?: GuiIcon.EMPTY

				inputBoxComponents.add(icon.getComponent(line))
            }

            renderedComponents.add(Component.textOfChildren(*inputBoxComponents.toTypedArray()).shiftToStartOfComponent())
        }

		for (row in iconindexes.rowKeySet()) {
			val line = row * 2 // slots only on even line
			val inputBoxComponents = mutableListOf<Component>()

			for (columnIndex in 0..9) {
				val icon = iconindexes[row, columnIndex] ?: GuiIcon.EMPTY

				inputBoxComponents.add(icon.getComponent(line))
			}
			renderedComponents.add(Component.textOfChildren(*inputBoxComponents.toTypedArray()).shiftToStartOfComponent())
		}

        // get sorted list of all lines in the builder
        for (line in guiComponents.map { it.line }.toSet().sorted()) {

            // get the maximum of three GuiComponents on this line
            val leftGuiComponent = guiComponents.find { it.line == line && it.alignment == TextAlignment.LEFT }
            val centerGuiComponent = guiComponents.find { it.line == line && it.alignment == TextAlignment.CENTER }
            val rightGuiComponent = guiComponents.find { it.line == line && it.alignment == TextAlignment.RIGHT }

            val verticalShift = listOf(
                leftGuiComponent?.verticalShift ?: SHIFT_DOWN_MIN,
                centerGuiComponent?.verticalShift ?: SHIFT_DOWN_MIN,
                rightGuiComponent?.verticalShift ?: SHIFT_DOWN_MIN
            ).max()

            // get the TextComponents of the GuiComponents with the proper shift, or an empty component if not present
            val leftTextComponent = leftGuiComponent?.component ?: Component.empty()
            val centerTextComponent = centerGuiComponent?.component ?: Component.empty()
            val rightTextComponent = rightGuiComponent?.component ?: Component.empty()

            // calculate the shift needed to move the text cursor from the left edge of the GUI to the beginning of
            // the left component
            val leftTextShiftComponent = if (leftTextComponent != Component.empty()) {
                shift(leftGuiComponent!!.horizontalShift)
            } else Component.empty()

            // calculate the shift needed to move the text cursor from the end of the left component (or the left edge)
            // to the beginning of the center component
            val centerTextShiftComponent = if (centerTextComponent != Component.empty()) {
                // use the original component's length and not include the offset
                shift(centerGuiComponent!!.horizontalShift +
                        ((guiWidth - GUI_MARGIN) / 2) - (centerGuiComponent.component.minecraftLength / 2) -
                        leftTextComponent.minecraftLength - leftTextShiftComponent.minecraftLength)
            } else Component.empty()

            // calculate the shift needed to move the text cursor from the end of the center component, the left
            // component, or the left edge, to the beginning of the right component
            val rightTextShiftComponent = if (rightTextComponent != Component.empty())
            {
                shift(rightGuiComponent!!.horizontalShift +
                        guiWidth - GUI_MARGIN - rightGuiComponent.component.minecraftLength -
                        centerTextComponent.minecraftLength - centerTextShiftComponent.minecraftLength -
                        leftTextComponent.minecraftLength - leftTextShiftComponent.minecraftLength)
            } else Component.empty()

            // assemble the components into one TextComponent
            val currentComponent = ofChildren(
                leftTextShiftComponent,
                leftTextComponent,
                centerTextShiftComponent,
                centerTextComponent,
                rightTextShiftComponent,
                rightTextComponent
            ).shiftToLine(line, initialShiftDown + verticalShift).shiftToStartOfComponent()

            renderedComponents.add(currentComponent)
        }

		val left = mutableListOf<Component>()
		val right = mutableListOf<Component>()

		guiBorders.forEach { background ->
			background.headerIcon?.build()?.let(renderedComponents::add)

			val headerWidth = background.headerIcon?.width ?: 0
			val textSize = (DEFAULT_GUI_WIDTH - headerWidth) / 2

			background.leftText?.let {
				left += GuiText("", guiWidth = textSize, initialShiftDown = -8)
					.add(it, alignment = TextAlignment.CENTER)
					.build()
			}

			background.rightText?.let {
				right += GuiText("", guiWidth = textSize - 1, initialShiftDown = -8)
					.add(it, alignment = TextAlignment.CENTER, horizontalShift = textSize + (headerWidth) + 2)
					.build()
			}
		}

        return Component.textOfChildren(*renderedComponents.toTypedArray(), *left.toTypedArray(), *right.toTypedArray())
    }

    /**
     * Stores line, Component, and alignment information for use in a GuiText
     * @param component the Component to be displayed
     * @param line the line that the Component should appear on
     * @param alignment the text justification of the Component
     * @param horizontalShift the amount to horizontally shift the Component by. Negative values shift the Component
     * left while positive values shift the Component right
     * @param verticalShift the amount to vertically shift the Component by. Added to the line modifier. This will shift
     * the entire line down; use sparingly
     */
    data class GuiComponent(
        val component: Component,
        val line: Int = 0,
        val alignment: TextAlignment = TextAlignment.LEFT,
        val horizontalShift: Int = 0,
        val verticalShift: Int = 0
    )

    /**
     * Stores background information for use in a GuiText
     * @param backgroundChar the character associated with the background with the font horizonsend:special
     * @param backgroundWidth the width that text and other usable elements within the GUI occupies. This is different
     * from the total pixel width of the background element. By default, this should be the total pixel width minus
     * 16, where a margin of 8 pixels is subtracted from the left and right border of the background
     */
    data class GuiBackground(
        val backgroundChar: Char = DEFAULT_BACKGROUND_CHARACTER,
        val backgroundWidth: Int = DEFAULT_GUI_WIDTH,
        val horizontalShift: Int = 0,
		val verticalShift: Int = 0,
		val subText: GuiText? = null
    )

    /**
     * Display a custom GUI background. Assumes that the background is the same width as the Minecraft GUI (176 pixels)
     * @param guiBackground the GuiBackground representing the background to display
     */

    private fun buildGuiBackground(guiBackground: GuiBackground) =
        shift(-GUI_MARGIN + guiBackground.horizontalShift)
            .append(Component.text(guiBackground.backgroundChar).color(WHITE).font(if (guiBackground.verticalShift == 0) SPECIAL_FONT_KEY else yFontKey(guiBackground.verticalShift)))
            .append(leftShift(guiBackground.backgroundWidth))

    /**
     * Set the custom GUI header
     * @param header the title of the GUI to be displayed
     */
    private fun buildGuiHeader(header: String) = ofChildren(Component.text(header), leftShift(header.minecraftLength))

    /**
     * Checks if a GuiComponent occupies the line and alignment of another GuiComponent
     * @param newComponent the new component whose position will be used to check availability
     * @return `true` if `newComponent` would occupy the position of a current GuiComponent
     */
    private fun GuiComponent.isOccupied(newComponent: GuiComponent) =
        this.isOccupied(newComponent.line, newComponent.alignment)

    /**
     * Checks if a GuiComponent occupies the specified line and alignment
     * @param line the line to check
     * @param alignment the alignment to check
     * @return `true` if a current GuiComponent occupies the specified position
     */
    private fun GuiComponent.isOccupied(line: Int, alignment: TextAlignment) =
        this.line == line && this.alignment == alignment

    /**
     * Contains the three text alignments/justifications
     */
    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }
}

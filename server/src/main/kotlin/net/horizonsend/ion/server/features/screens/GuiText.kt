package net.horizonsend.ion.server.features.screens

import net.horizonsend.ion.common.utils.text.customGuiBackground
import net.horizonsend.ion.common.utils.text.customGuiHeader
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.newShift
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.shiftToLeftOfComponent
import net.horizonsend.ion.common.utils.text.shiftToLine
import net.kyori.adventure.text.Component

class GuiText(
    private val name: String,
    private val backgroundChar: Char = '\uF8FF',
    private val guiWidth: Int = DEFAULT_GUI_WIDTH,
    private val initialShiftDown: Int = INITIAL_SHIFT_DOWN
) {

    companion object {
        // The default width of the Minecraft GUI/Inventory screen
        private const val DEFAULT_GUI_WIDTH = 169
        // The amount of pixels to shift down from the title to the first inventory slot
        private const val INITIAL_SHIFT_DOWN = 3
        // The amount of pixels from the edge of the GUI to the start of text
        private const val MARGIN = 8
    }

    /**
     * The list of GuiComponents added to this GuiText
     */
    private val guiComponents = mutableListOf<GuiComponent>()

    /**
     * Adds a GuiComponent to the GuiText
     * @param component the GuiComponent to add
     */
    fun add(component: GuiComponent) {
        val index = guiComponents.indexOfLast { it.isOccupied(component) }

        if (index >= 0) {
            // an element with the same line and alignment was found; replace it
            guiComponents[index] = component
        } else {
            // append element to the end of the list
            guiComponents.add(component)
        }
    }

    /**
     * Adds a GuiComponent to the GuiText
     * @param line the line that the new GuiComponent should be set at
     * @param component the text representation of the GuiComponent
     * @param alignment the alignment that the new GuiComponent should be set at
     */
    fun add(line: Int, alignment: TextAlignment, shift: Int = 0, component: Component) {
        add(GuiComponent(line, alignment, shift, component))
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
     * Builds the GuiText, returning a Component that can be placed in an Inventory's title
     * @return an Adventure Component for use in an Inventory
     */
    fun build(): Component {
        val renderedComponents = mutableListOf<Component>()

        renderedComponents.add(customGuiBackground(backgroundChar))
        renderedComponents.add(customGuiHeader(name))

        // get sorted list of all lines in the builder
        for (line in guiComponents.map { it.line }.toSet().sorted()) {
            // get the maximum of three GuiComponents on this line
            val leftGuiComponent = guiComponents.find { it.line == line && it.alignment == TextAlignment.LEFT }
            val centerGuiComponent = guiComponents.find { it.line == line && it.alignment == TextAlignment.CENTER }
            val rightGuiComponent = guiComponents.find { it.line == line && it.alignment == TextAlignment.RIGHT }

            // get the TextComponents of the GuiComponents with the proper shift, or an empty component if not present
            val leftTextComponent = ofChildren(
                newShift(leftGuiComponent?.shift ?: 0),
                leftGuiComponent?.component ?: Component.empty())
            val centerTextComponent = ofChildren(
                newShift(centerGuiComponent?.shift ?: 0),
                centerGuiComponent?.component ?: Component.empty())
            val rightTextComponent = ofChildren(
                newShift(rightGuiComponent?.shift ?: 0),
                rightGuiComponent?.component ?: Component.empty())

            // calculate the shift needed to move the text cursor from the end of the left component (or the left edge)
            // to the beginning of the center component
            val centerTextShiftComponent = if (centerTextComponent != Component.empty()) {
                // use the original component's length and not include the offset
                newShift(((guiWidth - MARGIN) / 2) -
                        (centerGuiComponent!!.component.minecraftLength / 2) - leftTextComponent.minecraftLength)
            } else Component.empty()

            // calculate the shift needed to move the text cursor from the end of the center component, the left
            // component, or the left edge, to the beginning of the right component
            val rightTextShiftComponent = if (rightTextComponent != Component.empty())
            {
                newShift(guiWidth - MARGIN -
                        rightGuiComponent!!.component.minecraftLength - centerTextComponent.minecraftLength -
                        centerTextShiftComponent.minecraftLength - leftTextComponent.minecraftLength
                )
            } else Component.empty()

            // assemble the components into one TextComponent
            val currentComponent = ofChildren(
                leftTextComponent,
                centerTextShiftComponent,
                centerTextComponent,
                rightTextShiftComponent,
                rightTextComponent
            ).shiftToLine(line, initialShiftDown).shiftToLeftOfComponent()

            renderedComponents.add(currentComponent)
        }

        return Component.textOfChildren(*renderedComponents.toTypedArray())
    }

    /**
     * Stores line, Component, and alignment information for use in a GuiText
     * @param line the line that the Component should appear on
     * @param alignment the text justification of the Component
     * @param shift the amount to shift the Component by. Negative values shift the Component left while positive
     * values shift the Component right
     * @param component the Component to be displayed
     */
    data class GuiComponent(
        val line: Int,
        val alignment: TextAlignment,
        val shift: Int = 0,
        val component: Component
    )

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
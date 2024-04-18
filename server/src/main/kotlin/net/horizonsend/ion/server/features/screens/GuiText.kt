package net.horizonsend.ion.server.features.screens

import net.horizonsend.ion.common.utils.text.centerJustify
import net.horizonsend.ion.common.utils.text.customGuiBackground
import net.horizonsend.ion.common.utils.text.customGuiHeader
import net.horizonsend.ion.common.utils.text.rightJustify
import net.horizonsend.ion.common.utils.text.shiftToLeftOfComponent
import net.horizonsend.ion.common.utils.text.shiftToLine
import net.horizonsend.ion.common.utils.text.withShift
import net.kyori.adventure.text.Component

class GuiText(
    private val name: String,
    private val backgroundChar: Char = '\uF8FF',
    private val width: Int = DEFAULT_GUI_WIDTH,
    private val initialShiftDown: Int = INITIAL_SHIFT_DOWN
) {

    companion object {
        // The default width of the Minecraft GUI/Inventory screen
        private const val DEFAULT_GUI_WIDTH = 169
        // The amount of pixels to shift down from the title to the first inventory slot
        private const val INITIAL_SHIFT_DOWN = 3
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
        val sortedGuiComponents = guiComponents.sortedWith(compareBy(GuiComponent::line, GuiComponent::alignment))
        val renderedComponents = mutableListOf<Component>()

        renderedComponents.add(customGuiBackground(backgroundChar))
        renderedComponents.add(customGuiHeader(name))

        var currentLine = -1
        var currentComponent: Component? = null

        for (guiComponent in sortedGuiComponents) {
            if (currentLine != guiComponent.line) {
                if (currentComponent != null) renderedComponents.add(currentComponent.shiftToLeftOfComponent())
                currentLine = guiComponent.line

                when (guiComponent.alignment) {
                    TextAlignment.LEFT -> {
                        currentComponent = guiComponent.component
                            .withShift(guiComponent.shift)
                            .shiftToLine(currentLine, initialShiftDown)
                    }
                    TextAlignment.CENTER -> {
                        currentComponent = (Component.text() as Component)
                            .centerJustify(guiComponent.component
                                .withShift(guiComponent.shift)
                            ).shiftToLine(currentLine, initialShiftDown)
                    }
                    TextAlignment.RIGHT -> {
                        currentComponent = (Component.text() as Component)
                            .rightJustify(guiComponent.component
                                .withShift(guiComponent.shift)
                            ).shiftToLine(currentLine, initialShiftDown)
                    }
                }
            } else {
                when (guiComponent.alignment) {
                    // left case will always be created first
                    TextAlignment.LEFT -> continue
                    // currentComponent will never be null as it is created on the previous step
                    TextAlignment.CENTER -> currentComponent!!
                        .centerJustify(guiComponent.component)
                        .withShift(guiComponent.shift)
                    TextAlignment.RIGHT -> currentComponent!!
                        .rightJustify(guiComponent.component)
                        .withShift(guiComponent.shift)
                }
            }
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
package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.CROSSHAIR_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.INTERDICTION_ICON
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.EQUALS
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.block.BlockFace
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

object StarshipsSidebar {
    fun starshipNameComponent(starshipName: String, starshipIcon: String): Component {
        return ofChildren(
            Component.text(starshipIcon, WHITE).font(Sidebar.fontKey),
            Component.text(" $starshipName", WHITE)
        )
    }

    fun hullIntegrityComponent(hullIntegrity: Int): Component {
        return Component.text(hullIntegrity).run { when {
            hullIntegrity == 100 -> return@run this.color(GREEN)
            hullIntegrity > 90 -> return@run this.color(GOLD)
            hullIntegrity > 85 -> return@run this.color(RED)
            else -> return@run this.color(DARK_RED)
        }}
    }

    fun speedComponent(directControl: Boolean, cruising: Boolean, stopped: Boolean): Component {
        val component = Component.text()

        if (directControl) {
            component.append(Component.text("DC", GOLD))
            component.appendSpace()
        }
        if (cruising) {
            component.append(Component.text("»", GREEN))
        } else {
            if (!stopped) {
                component.append(Component.text("«", RED))
            } else {
                component.append(Component.text("□", DARK_GRAY))
            }
        }
        return component.build()
    }

    fun maxSpeedComponent(currentVelocity: Double, maxVelocity: Int, acceleration: Double): Component {
        return ofChildren(
            Component.text(currentVelocity, GREEN),
            Component.text("/", DARK_GRAY),
            Component.text(maxVelocity, DARK_GREEN),
            Component.space(),
            Component.text(acceleration, YELLOW)
        )
    }

    fun powerModeComponent(shield: Int, weapon: Int, thruster: Int): Component {
        return ofChildren(
            Component.text(shield, AQUA),
            Component.text("/", GRAY),
            Component.text(weapon, RED),
            Component.text("/", GRAY),
            Component.text(thruster, YELLOW)
        )
    }

    fun capacitorComponent(capacitor: Int): Component {
        return Component.text(capacitor, percentColor(capacitor))
    }

    fun heavyWeaponChargeComponent(boostTime: Long): Component {
        return if (boostTime.toInt() == -1) {
            Component.text("N/A", RED)
        } else if (boostTime > 0) {
            Component.text(boostTime.nanoseconds.toString(DurationUnit.SECONDS, 1), GOLD)
        } else {
            Component.text("RDY", GREEN)
        }
    }

    fun activeModulesComponent(vararg components: Component): Component {
        val returnComponent = Component.text()
        if (components.isNotEmpty()) {
            for (component in components) {
                returnComponent.append(component)
                returnComponent.appendSpace()
            }
        } else returnComponent.append(Component.text("N/A", RED))
        return returnComponent.build()
    }

    fun interdictionActiveComponent(isInterdicting: Boolean): Component {
        return if (isInterdicting) {
            ofChildren(
                Component.text(INTERDICTION_ICON.text, AQUA).font(Sidebar.fontKey),
            )
        } else Component.empty()
    }

    fun weaponsetActiveComponent(weaponset: String?): Component {
        return if (weaponset != null) {
            ofChildren(
                Component.text(CROSSHAIR_ICON.text, AQUA).font(Sidebar.fontKey),
                Component.text(weaponset, AQUA),
            )
        } else Component.empty()
    }

    fun compassComponent(blockFace: BlockFace, icon: String): MutableList<MutableList<Component>> {
        val compass = mutableListOf(
            mutableListOf(Component.text("\\").asComponent(), Component.text("N").asComponent(), Component.text("/").asComponent()),
            mutableListOf(Component.text("W").asComponent(), Component.text(icon).font(Sidebar.fontKey).asComponent(), Component.text("E").asComponent()),
            mutableListOf(Component.text("/").asComponent(), Component.text("S").asComponent(), Component.text("\\").asComponent())
        )

        when (blockFace) {
            BlockFace.SOUTH -> {
                compass.reverse()
                compass[0].reverse()
                compass[1].reverse()
                compass[2].reverse()
            }
            BlockFace.EAST -> {
                compass[0].reverse()
                compass[1].reverse()
                compass[2].reverse()
                val transposed = transpose(compass)
                compass[0] = transposed[0].toMutableList()
                compass[1] = transposed[1].toMutableList()
                compass[2] = transposed[2].toMutableList()

                compass.forEach { row ->
                    for (elementIndex in 0..<row.size) {
                        row[elementIndex] = replaceSlash(row[elementIndex])
                    }
                }
            }
            BlockFace.WEST -> {
                val transposed = transpose(compass)
                compass[0] = transposed[0].toMutableList()
                compass[1] = transposed[1].toMutableList()
                compass[2] = transposed[2].toMutableList()
                compass[0].reverse()
                compass[1].reverse()
                compass[2].reverse()

                compass.forEach { row ->
                    for (elementIndex in 0..<row.size) {
                        row[elementIndex] = replaceSlash(row[elementIndex])
                    }
                }

            } else -> { }
        }

        return compass
    }

    private fun percentColor(percent: Int): NamedTextColor = when {
        percent <= 5 -> RED
        percent <= 10 -> GOLD
        percent <= 25 -> YELLOW
        percent <= 40 -> GREEN
        percent <= 55 -> DARK_GREEN
        percent <= 70 -> AQUA
        percent <= 85 -> DARK_AQUA
        else -> BLUE
    }

    private fun transpose(xs: List<List<Component>>): List<List<Component>> {
        val cols = xs[0].size
        val rows = xs.size
        return List(cols) { j ->
            List(rows) { i ->
                xs[i][j]
            }
        }
    }

    private val forwardsToBackwards = TextReplacementConfig.builder().matchLiteral("/").replacement("\\")
    private val backwardsToForwards = TextReplacementConfig.builder().matchLiteral("\\").replacement("/")

    private fun replaceSlash(component: Component): Component {

        if (component.contains(Component.text("/"), EQUALS)) {
            return component.replaceText(forwardsToBackwards.build())
        } else if (component.contains(Component.text("\\"), EQUALS)) {
            return component.replaceText(backwardsToForwards.build())
        }

        return component
    }
}
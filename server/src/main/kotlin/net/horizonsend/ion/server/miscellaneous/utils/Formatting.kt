package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.World

val ARENA_PREFIX = ofChildren(bracketed(text("Arena", YELLOW)), space())

fun getArenaPrefix(world: World) = if (world.ion.hasFlag(WorldFlag.ARENA)) ARENA_PREFIX else empty()


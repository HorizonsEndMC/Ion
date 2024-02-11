package net.horizonsend.ion.server.features.npcs

import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServer
import org.bukkit.entity.Player

val isCitizensLoaded get() = IonServer.server.pluginManager.isPluginEnabled("Citizens")
val registries: MutableList<NPCRegistry> = mutableListOf()

fun Player.isNPC() = registries.any { it.isNPC(this) }

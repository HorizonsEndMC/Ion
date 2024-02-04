package net.horizonsend.ion.server.features.npcs

import io.netty.util.internal.logging.Slf4JLoggerFactory
import net.citizensnpcs.api.npc.NPCRegistry
import net.horizonsend.ion.server.IonServer
import org.bukkit.entity.Player

val isCitizensLoaded get() = IonServer.server.pluginManager.isPluginEnabled("Citizens")
val registries: MutableList<NPCRegistry> = mutableListOf()

private val log = Slf4JLoggerFactory.getInstance("CitizensUtils")

fun Player.isNPC() = registries.any { it.isNPC(this) }

package net.horizonsend.ion.server.miscellaneous.utils

import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R3.CraftSound

val Sound.mcName: String get() = CraftSound.getSound(this)

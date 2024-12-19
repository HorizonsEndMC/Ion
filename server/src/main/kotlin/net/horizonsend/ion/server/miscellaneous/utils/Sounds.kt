package net.horizonsend.ion.server.miscellaneous.utils

import org.bukkit.Sound
import org.bukkit.craftbukkit.CraftSound

val Sound.mcName: String get() = CraftSound.getSound(this)

package net.starlegacy.util

import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_18_R1.CraftSound

val Sound.mcName: String get() = CraftSound.getSound(this)

package net.starlegacy.util

import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R2.CraftSound

val Sound.mcName: String get() = CraftSound.getSound(this)

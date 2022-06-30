package net.horizonsend.ion.server.screens

import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit

/**
 * It is important to note that the TextScreen will not ensure the validity of your TextComponent, you have to.
 */
class TextScreen(text: TextComponent) : Screen(Bukkit.createInventory(null, 54, text))
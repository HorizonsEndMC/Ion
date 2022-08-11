package net.horizonsend.ion.server.utilities

import net.horizonsend.ion.common.utilities.enumSetOf
import org.bukkit.Bukkit
import org.bukkit.Material

val forbiddenCraftingItems = enumSetOf(
	Material.WARPED_FUNGUS_ON_A_STICK, Material.NETHERITE_AXE, Material.NETHERITE_HOE, Material.NETHERITE_SWORD,
	Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL
)

inline fun ionCore(execute: () -> Unit) {
	if (!Bukkit.getPluginManager().isPluginEnabled("IonCore")) return
	try { execute() } catch (_: NoClassDefFoundError) {}
}
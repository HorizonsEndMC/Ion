package net.horizonsend.ion.server.utilities

import java.util.EnumSet
import net.horizonsend.ion.common.database.enums.Particle
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

inline fun vaultEconomy(execute: (Economy) -> Unit) {
	execute(Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider ?: return)
}

fun org.bukkit.Particle.getIonEquivalent() : Particle{
	return when(this){
		org.bukkit.Particle.SPIT -> Particle.SPIT
		org.bukkit.Particle.ELECTRIC_SPARK -> Particle.ELECTRIC_SPARK
		org.bukkit.Particle.GLOW -> Particle.GLOW
		org.bukkit.Particle.GLOW_SQUID_INK -> Particle.GLOW_SQUID_INK
		org.bukkit.Particle.SOUL_FIRE_FLAME -> Particle.SOUL_FIRE_FLAME
		org.bukkit.Particle.NOTE -> Particle.NOTE
		org.bukkit.Particle.SCULK_SOUL -> Particle.SCULK_SOUL
		org.bukkit.Particle.VILLAGER_ANGRY -> Particle.ANGRY_VILLAGER
		org.bukkit.Particle.HEART -> Particle.HEART
		org.bukkit.Particle.FLAME -> Particle.FLAME
		org.bukkit.Particle.WAX_ON -> Particle.WAX_ON
		org.bukkit.Particle.SQUID_INK -> Particle.SQUID_INK
		org.bukkit.Particle.SNEEZE -> Particle.SNEEZE
		org.bukkit.Particle.DRAGON_BREATH -> Particle.DRAGON_BREATH
		org.bukkit.Particle.REDSTONE -> Particle.REDSTONE_PARTICLE
		else -> Particle.REDSTONE_PARTICLE
	}
}
inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }

fun Particle.getBukkitEquivalent() : org.bukkit.Particle {
	return when(this){
		Particle.SPIT -> org.bukkit.Particle.SPIT
		Particle.ELECTRIC_SPARK ->  org.bukkit.Particle.ELECTRIC_SPARK
		Particle.GLOW ->  org.bukkit.Particle.GLOW
		Particle.GLOW_SQUID_INK ->  org.bukkit.Particle.GLOW_SQUID_INK
		Particle.SOUL_FIRE_FLAME ->  org.bukkit.Particle.SOUL_FIRE_FLAME
		Particle.NOTE ->   org.bukkit.Particle.NOTE
		Particle.SCULK_SOUL ->  org.bukkit.Particle.SCULK_SOUL
		Particle.ANGRY_VILLAGER ->  org.bukkit.Particle.VILLAGER_ANGRY
		Particle.HEART ->  org.bukkit.Particle.HEART
		Particle.FLAME ->  org.bukkit.Particle.FLAME
		Particle.WAX_ON ->  org.bukkit.Particle.WAX_ON
		Particle.SQUID_INK ->  org.bukkit.Particle.SQUID_INK
		Particle.SNEEZE ->  org.bukkit.Particle.SNEEZE
		Particle.DRAGON_BREATH ->  org.bukkit.Particle.DRAGON_BREATH
		Particle.REDSTONE_PARTICLE ->  org.bukkit.Particle.REDSTONE
		else -> org.bukkit.Particle.REDSTONE
	}
}
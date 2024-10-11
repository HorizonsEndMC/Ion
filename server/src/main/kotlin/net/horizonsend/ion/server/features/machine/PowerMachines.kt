package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object PowerMachines : IonServerComponent() {
	override fun onEnable() {
		// IIRC the below is a hacky fix for generators, it should be removed if possible, moved if not

		val deadBush = ItemStack(Material.DEAD_BUSH)
		if (Bukkit.getRecipesFor(deadBush).size == 0) {
			Bukkit.addRecipe(FurnaceRecipe(
				NamespacedKey(IonServer, "focusing_crystal"),
				deadBush,
				Material.PRISMARINE_CRYSTALS,
				0.0f,
				200
			))

			// Another hacky fix that should be removed to make gas power plants work
			Bukkit.addRecipe(FurnaceRecipe(
				NamespacedKey(IonServer, "gas_canisters"),
				deadBush,
				Material.WARPED_FUNGUS_ON_A_STICK,
				0.0f,
				200
			))

			// Batteries
			Bukkit.addRecipe(FurnaceRecipe(
				NamespacedKey(IonServer, "batteries"),
				deadBush,
				Material.SNOWBALL,
				0.0f,
				200
			))

			// Gas furnaces
			Bukkit.addRecipe(FurnaceRecipe(
				NamespacedKey(IonServer, "gas_furnaces"),
				ItemStack(Material.IRON_INGOT),
				Material.IRON_INGOT,
				0.0f,
				Int.MAX_VALUE
			))
		}
	}

	val prefixComponent = Component.text("E: ", NamedTextColor.YELLOW)

	@JvmOverloads
	fun setPower(sign: Sign, power: Int, fast: Boolean = true): Int {
		val correctedPower: Int = power.coerceAtLeast(0)
		if (!sign.persistentDataContainer.has(NamespacedKeys.MULTIBLOCK)) return power

		sign.persistentDataContainer.set(NamespacedKeys.POWER, PersistentDataType.INTEGER, correctedPower)
//		sign.getSide(Side.FRONT).line(2, Component.text().append(prefixComponent, Component.text(correctedPower, NamedTextColor.GREEN)).build())
//		sign.update(false, false)
		return power
	}

	@JvmOverloads
	fun getPower(sign: Sign, fast: Boolean = true): Int {
		return sign.persistentDataContainer.get(NamespacedKeys.POWER, PersistentDataType.INTEGER)
			?: return setPower(sign, 0)
	}

	fun addPower(sign: Sign, amount: Int) {
		setPower(sign, getPower(sign) + amount)
	}

	fun removePower(sign: Sign, amount: Int) {
		setPower(sign, getPower(sign) - amount, true)
	}
}

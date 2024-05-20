package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import javax.naming.Name

object PowerMachines : IonServerComponent() {
	override fun onEnable() {
		// IIRC the below is a hacky fix for generators, it should be removed if possible, moved if not

		val deadBush = ItemStack(Material.DEAD_BUSH)
		if (Bukkit.getRecipesFor(deadBush).size == 0) {
			val key = NamespacedKey(IonServer, "focusing_crystal")
			val recipe = FurnaceRecipe(key, deadBush, Material.PRISMARINE_CRYSTALS, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

		// Another hacky fix for turret ammo

		val lapisLazuli = ItemStack(Material.LAPIS_LAZULI)
		if (Bukkit.getRecipesFor(lapisLazuli).size >= 6) {
			val key = NamespacedKey(IonServer, "bc_infrastructure_1")
			val recipe = FurnaceRecipe(key, lapisLazuli, Material.PRISMARINE_CRYSTALS, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

		// Another hacky fix that fixes most BC infrastructure furnace recipes
		val ironIngot = ItemStack(Material.IRON_INGOT)
		if (Bukkit.getRecipesFor(ironIngot).size >= 28) {
			val key = NamespacedKey(IonServer, "bc_infrastructure_2")
			val recipe = FurnaceRecipe(key, ironIngot, Material.PRISMARINE_CRYSTALS, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

//		val oxidisedCopperBlock = ItemStack(Material.OXIDIZED_COPPER)
//		if (Bukkit.getRecipesFor(oxidisedCopperBlock).size >= 0) {
//			val key = NamespacedKey(IonServer, "bc_infrastructure_3")
//			val recipe = FurnaceRecipe(key, oxidisedCopperBlock, Material.PRISMARINE_CRYSTALS, 0.0f, 200)
//			Bukkit.addRecipe(recipe)
//		}

		// Another hacky fix that should be removed to make gas power plants work
		val bone = ItemStack(Material.BONE)
		if (Bukkit.getRecipesFor(bone).size >= 0) {
			val key = NamespacedKey(IonServer, "gas_canisters")
			val recipe = FurnaceRecipe(key, deadBush, Material.WARPED_FUNGUS_ON_A_STICK, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

		val yellowFlower = ItemStack(Material.DANDELION)
		if (Bukkit.getRecipesFor(yellowFlower).size == 0) {
			val key = NamespacedKey(IonServer, "dud")
			val recipe = FurnaceRecipe(key, yellowFlower, Material.SNOWBALL, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}
	}

	private val prefixComponent = Component.text("E: ", NamedTextColor.YELLOW)

	@JvmOverloads
	fun setPower(sign: Sign, power: Int, fast: Boolean = true): Int {
		val correctedPower: Int = if (!fast) {
			val multiblock = (Multiblocks[sign] ?: return 0) as? PowerStoringMultiblock ?: return 0
			power.coerceIn(0, multiblock.maxPower)
		} else {
			power.coerceAtLeast(0)
		}

		if (!sign.persistentDataContainer.has(NamespacedKeys.MULTIBLOCK)) return power

		sign.persistentDataContainer.set(NamespacedKeys.POWER, PersistentDataType.INTEGER, correctedPower)
		sign.line(2, Component.text().append(prefixComponent, Component.text(correctedPower, NamedTextColor.GREEN)).build())
		sign.update(false, false)
		return power
	}

	@JvmOverloads
	fun getPower(sign: Sign, fast: Boolean = true): Int {
		if (!fast && Multiblocks[sign] !is PowerStoringMultiblock) {
			return 0
		}

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

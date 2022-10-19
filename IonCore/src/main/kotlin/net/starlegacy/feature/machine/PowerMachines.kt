package net.starlegacy.feature.machine

import co.aikar.timings.Timing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.PLUGIN
import net.starlegacy.SLComponent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.Multiblocks.multiblockNamespacedKey
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.time
import net.starlegacy.util.timing
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object PowerMachines : SLComponent() {
	private lateinit var powerSettingTiming: Timing
	private lateinit var powerGettingTiming: Timing
	private val prefix = "${ChatColor.YELLOW}E: ${ChatColor.GREEN}"

	override fun onEnable() {
		powerSettingTiming = timing("Power Machine Machine Power Setting")
		powerGettingTiming = timing("Power Machine Power Getting")

		// IIRC the below is a hacky fix for generators, it should be removed if possible, moved if not

		val deadBush = ItemStack(Material.DEAD_BUSH)
		if (Bukkit.getRecipesFor(deadBush).size == 0) {
			val key = PLUGIN.namespacedKey("focusing_crystal")
			val recipe = FurnaceRecipe(key, deadBush, Material.PRISMARINE_CRYSTALS, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}

		val yellowFlower = ItemStack(Material.DANDELION)
		if (Bukkit.getRecipesFor(yellowFlower).size == 0) {
			val key = PLUGIN.namespacedKey("dud")
			val recipe = FurnaceRecipe(key, yellowFlower, Material.SNOWBALL, 0.0f, 200)
			Bukkit.addRecipe(recipe)
		}
	}

	private val powerNamespacedKey = NamespacedKey(PLUGIN, "power")
	private val prefixComponent = Component.text("E: ", NamedTextColor.YELLOW)

	@JvmOverloads
	fun setPower(sign: Sign, power: Int, fast: Boolean = true): Int = powerSettingTiming.time {
		val correctedPower: Int = if (!fast) {
			val multiblock = (Multiblocks[sign] ?: return@time 0) as? PowerStoringMultiblock ?: return@time 0
			power.coerceIn(0, multiblock.maxPower)
		} else {
			power.coerceAtLeast(0)
		}

		if (!sign.persistentDataContainer.has(multiblockNamespacedKey)) return@time power

		sign.persistentDataContainer.set(powerNamespacedKey, PersistentDataType.INTEGER, correctedPower)
		sign.line(2, Component.text().append(prefixComponent, Component.text(correctedPower, NamedTextColor.GREEN)).build())
		sign.update(false, false)
		return@time power
	}

	@JvmOverloads
	fun getPower(sign: Sign, fast: Boolean = true): Int {
		if (!fast && Multiblocks[sign] !is PowerStoringMultiblock) {
			return 0
		}

		return sign.persistentDataContainer.get(powerNamespacedKey, PersistentDataType.INTEGER)
			?: return setPower(sign, sign.getLine(2).removePrefix(prefix).toIntOrNull() ?: 0)
	}

	fun addPower(sign: Sign, amount: Int) {
		setPower(sign, getPower(sign) + amount)
	}

	fun removePower(sign: Sign, amount: Int) {
		setPower(sign, getPower(sign) - amount, true)
	}
}
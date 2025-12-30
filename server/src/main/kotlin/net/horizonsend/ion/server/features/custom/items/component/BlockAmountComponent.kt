package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.util.StoredValues
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.IntegerToken
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class BlockAmountComponent(val balancingSupplier: PVPBalancingConfiguration.MeleeWeapons.EnergySwordBalancing) : CustomItemComponent, LoreManager {

	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
		StoredValues.ENERGYSWORDBLOCK.setAmount(baseItem, balancingSupplier.blockAmount)
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> {
		return listOf()
	}

	fun setBlock(itemStack: ItemStack, amount: Int, livingEntity: LivingEntity?) {
		val corrected = amount.coerceAtMost(balancingSupplier.blockAmount).coerceAtLeast(0)

		StoredValues.ENERGYSWORDBLOCK.setAmount(itemStack, corrected)
		StoredValues.TIMELASTUSED.setAmount(itemStack, (System.currentTimeMillis()/1000).toInt())
		itemStack.customItem?.refreshLore(itemStack)
		if (corrected <=0) livingEntity?.clearActiveItem()

		//updateDurability(itemStack, corrected, balancingSupplier.blockAmount)
		sendActionBar(corrected, livingEntity ?: return)
	}

	fun getBlock(itemStack: ItemStack, livingEntity: LivingEntity? = null): Int {
		return updateBlock(itemStack, livingEntity)
	}

	fun updateBlock(itemStack: ItemStack, livingEntity: LivingEntity? = null): Int{
		var speedyBlock = false
		val lastUsed =
			StoredValues.TIMELASTUSED.getAmount(itemStack)
		val amountOfSecondsSinceLastUse =
			TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastUsed
		val amountOfTicksSinceLastUse = amountOfSecondsSinceLastUse * 20.0

		for (item in (livingEntity as Player).inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.DUELIST_ENHANCEMENT)) continue
			speedyBlock = true
		}
		val amountTheBlockShouldveRecharged = if (speedyBlock) {
			amountOfTicksSinceLastUse *  balancingSupplier.blockRechargePerTick*2 }
		else amountOfTicksSinceLastUse *  balancingSupplier.blockRechargePerTick

		val currentBlock =  StoredValues.ENERGYSWORDBLOCK.getAmount(itemStack)
		val newBlock = (currentBlock+amountTheBlockShouldveRecharged).roundToInt().coerceIn(0..balancingSupplier.blockAmount)
		//Show the block as durability
		StoredValues.ENERGYSWORDBLOCK.setAmount(itemStack, newBlock)
		StoredValues.TIMELASTUSED.setAmount(itemStack, (System.currentTimeMillis()/1000).toInt())

		//If the Block ammount to set is 0, put a cooldown on the energy sword untill its recharged
		if (newBlock <= 0.0) {
			(livingEntity as? Player)?.setCooldown(
				itemStack.type,
				(balancingSupplier.blockAmount / balancingSupplier.blockRechargePerTick).toInt()
			)
			livingEntity?.clearActiveItem()
			setBlock(itemStack, 0, livingEntity)
		}

		//updateDurability(itemStack, newBlock, balancingSupplier.blockAmount)
		sendActionBar(newBlock, livingEntity ?: return newBlock)
		return newBlock
	}

	fun sendActionBar(block: Int, livingEntity: LivingEntity){
		livingEntity.sendActionBar(
			Component.text(
				"Block: $block / ${balancingSupplier.blockAmount}",
				NamedTextColor.GREEN,
				TextDecoration.BOLD,
				TextDecoration.UNDERLINED
			)
		)
	}


	override val priority: Int = 200
	override fun shouldIncludeSeparator(): Boolean = false

	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
		return 	listOf(StoredValues.ENERGYSWORDBLOCK.formatLore(StoredValues.ENERGYSWORDBLOCK.getAmount(itemStack), balancingSupplier.blockAmount).itemLore,)
	}

	override fun registerSerializers(serializationManager: SerializationManager) {
		serializationManager.addSerializedData(
			"energy_sword_block",
			IntegerToken,
			{ customItem, itemStack -> customItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).getAmmo(itemStack) },
			{ customItem: CustomItem, itemStack: ItemStack, data: Int -> customItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).setAmmo(itemStack, customItem, data) }
		)
	}
}


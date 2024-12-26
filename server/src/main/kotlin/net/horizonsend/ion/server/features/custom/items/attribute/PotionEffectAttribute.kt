package net.horizonsend.ion.server.features.custom.items.attribute

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.POWER_STORAGE
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PotionEffectAttribute(
	val effect: PotionEffectType,
	val duration: Int,
	val amplifier: Int,
	val powerConsumption: Int,
	val powerConsumptionPredicate: (LivingEntity, CustomItem, ItemStack) -> Boolean
) : CustomItemAttribute {
	fun addPotionEffect(entity: LivingEntity, customItem: CustomItem, itemStack: ItemStack) {
		Tasks.sync {
			entity.addPotionEffect(PotionEffect(effect, duration, amplifier, true, false))

			if (powerConsumptionPredicate.invoke(entity, customItem, itemStack)) {
				customItem.getComponent(POWER_STORAGE).removePower(itemStack, customItem, powerConsumption)
			}
		}
	}
}

package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Balancing
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.components.AmmunitionComponent
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.components.ListenerComponent
import net.horizonsend.ion.server.features.custom.items.objects.AmmunitionHoldingItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class NewBlaster<T : Balancing>(
	identifier: String,
	displayName: Component,
	itemFactory: ItemFactory,
	private val balancingSupplier: Supplier<T>
) : NewCustomItem(
	identifier,
	displayName,
	itemFactory,
	1
) {
	protected val ammoComponent = AmmunitionComponent(balancingSupplier)

	override val customComponents: List<CustomItemComponent> = listOf(
		ammoComponent,
		ListenerComponent.interactListener(this) { event, _, item -> fire(event.player, item) },
		ListenerComponent.playerSwapHandsListener(this) { event, _, item -> reload(event.player, item) }
	)

	val balancing get() = balancingSupplier.get()

	fun fire(shooter: LivingEntity, blasterItem: ItemStack) {

	}

	fun reload(livingEntity: LivingEntity, blasterItem: ItemStack) {
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(blasterItem.type)) return // Cooldown

		val originalAmmo = ammoComponent.getAmmo(blasterItem)

		var ammo = originalAmmo
		if (ammo == balancing.capacity) return

		if (balancing.consumesAmmo) {
			for (magazineItem in livingEntity.inventory.filterNotNull()) {
				val magazineCustomItem: CustomItem = magazineItem.customItem ?: continue // To get magazine properties
				if (ammo >= balancing.capacity) continue // Check if blaster magazine is full
				if (magazineCustomItem.identifier != balancing.magazineIdentifier) continue // Only correct magazine

				val magazineAmmo = (magazineCustomItem as AmmunitionHoldingItem).getAmmunition(magazineItem)
				val amountToTake = (balancing.capacity - ammo).coerceAtMost(magazineAmmo)
				magazineCustomItem.setAmmunition(magazineItem, livingEntity.inventory, magazineAmmo - amountToTake)

				ammo += amountToTake
			}
		}

		if (livingEntity.world.hasFlag(WorldFlag.ARENA) || !balancing.consumesAmmo) {
			ammo = balancing.capacity
		}

		if (ammo - originalAmmo == 0) {
			livingEntity.playSound(sound(key("minecraft:item.bundle.drop_contents"), PLAYER, 5f, 2.00f))
			livingEntity.alert("Out of ammo!")
			return
		}

		livingEntity.setCooldown(blasterItem.type, this.balancing.reload)

		ammoComponent.setAmmo(blasterItem, ammo)

		livingEntity.sendActionBar(template(text("Ammo: {0} / {1}", RED), ammo.coerceIn(0, balancing.capacity), balancing.capacity))
		if (ammo <= 0) livingEntity.playSound(sound(key("minecraft:block.iron_door.open"), PLAYER, 5f, 2.00f))

		// Start reload
		livingEntity.world.playSound(balancing.soundReloadStart.sound, livingEntity)

		// Finish reload
		Tasks.syncDelay(this.balancing.reload.toLong()) {
			livingEntity.world.playSound(balancing.soundReloadFinish.sound, livingEntity)
		}
	}
}

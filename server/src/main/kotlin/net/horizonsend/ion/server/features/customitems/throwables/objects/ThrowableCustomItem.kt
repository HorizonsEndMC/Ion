package net.horizonsend.ion.server.features.customitems.throwables.objects

import org.bukkit.block.Dispenser as DispenserState
import org.bukkit.block.data.type.Dispenser as DispenserData
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.Throwables.ThrowableBalancing
import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class ThrowableCustomItem(
	identifier: String,

	private val customModelData: Int,
	val displayName: Component,

	private val balancingSupplier: Supplier<ThrowableBalancing>
) : CustomItem(identifier) {
	val balancing get() = balancingSupplier.get()
	val material = Material.WARPED_FUNGUS_ON_A_STICK

	override fun constructItemStack(): ItemStack {
		return ItemStack(material).updateMeta {
			it.setCustomModelData(customModelData)
			it.displayName(displayName)
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		}.apply { amount = 1 }
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		throwItem(itemStack, livingEntity)
	}

	open fun throwItem(item: ItemStack, thrower: LivingEntity, maxTicks: Int = balancing.maxTicks) {
		val newItemStack = constructItemStack()
		newItemStack.amount = 1

		if (thrower is Player) {
			if (thrower.hasCooldown(item.type)) return

			thrower.setCooldown(item.type, balancing.throwCooldownTicks)
		}
		val itemEntity = thrower.world.dropItem(thrower.eyeLocation, newItemStack)

		throwItem(
			item,
			itemEntity,
			thrower.location.direction.clone().multiply(balancing.throwVelocityMultiplier),
			thrower,
			maxTicks
		)
	}

	override fun handleDispense(dispenser: DispenserState, slot: Int) {
		val facing = (dispenser.blockData as DispenserData).facing
		val origin = dispenser.location.toCenterLocation().add(facing.direction)
		val droppedItem = dispenser.world.dropItem(origin, constructItemStack())

		val item = dispenser.inventory.getItem(slot) ?: return

		throwItem(
			item,
			droppedItem,
			facing.direction.normalize().multiply(balancing.throwVelocityMultiplier),
			null,
			balancing.maxTicks * 2
		)

		dispenser.inventory.setItem(slot, null)
	}

	open fun throwItem(
		item: ItemStack,
		itemEntity: Item,
		direction: Vector,
		thrower: LivingEntity?,
		maxTicks: Int = balancing.maxTicks
	) {
		val newItemStack = constructItemStack()
		newItemStack.amount = 1

		itemEntity.pickupDelay = Integer.MAX_VALUE
		itemEntity.health = balancing.maxHealth
		itemEntity.velocity = direction

		val thrown = constructThrownRunnable(itemEntity, maxTicks, thrower)

		itemEntity.world.playSound(itemEntity, "laser", 5f, 0.5f)
		thrown.runTaskTimer(IonServer, 1, balancing.tickInterval)
		item.amount = 0
	}

	abstract fun constructThrownRunnable(item: Item, maxTicks: Int = balancing.maxTicks, damageSource: Entity?): ThrownCustomItem
}

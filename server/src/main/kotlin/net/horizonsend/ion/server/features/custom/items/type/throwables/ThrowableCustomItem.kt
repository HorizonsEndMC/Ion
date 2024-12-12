package net.horizonsend.ion.server.features.custom.items.type.throwables

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.Throwables.ThrowableBalancing
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Dispenser as DispenserState
import org.bukkit.block.data.type.Dispenser as DispenserData
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class ThrowableCustomItem(
	identifier: String,

	customModel: String,
	displayName: Component,

	private val balancingSupplier: Supplier<ThrowableBalancing>
) : CustomItem(
	identifier,
	displayName,
	ItemFactory.unStackableCustomItem(customModel)
) {
	val balancing get() = balancingSupplier.get()
	val material = Material.WARPED_FUNGUS_ON_A_STICK

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, Listener.rightClickListener(this@ThrowableCustomItem) { event, _, itemStack ->
			throwItem(itemStack, event.player)
		})
		addComponent(CustomComponentTypes.LISTENER_DISPENSE, Listener.dispenseListener(this@ThrowableCustomItem) { event, _, item ->
			handleDispense(event.block.state as DispenserState, event.slot)
		})
	}

	protected open fun throwItem(item: ItemStack, thrower: LivingEntity, maxTicks: Int = balancing.maxTicks) {
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

	private fun handleDispense(dispenser: DispenserState, slot: Int) {
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

	protected open fun throwItem(
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

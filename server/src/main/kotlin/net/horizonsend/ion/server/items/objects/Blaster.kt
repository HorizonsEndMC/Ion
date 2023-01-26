package net.horizonsend.ion.server.items.objects

import io.papermc.paper.entity.RelativeTeleportFlag
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeapon.Balancing
import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.items.CustomItems.STANDARD_MAGAZINE
import net.horizonsend.ion.server.items.CustomItems.customItem
import net.horizonsend.ion.server.managers.ProjectileManager
import net.horizonsend.ion.server.projectiles.RayTracedParticleProjectile
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE
import net.minecraft.resources.ResourceLocation
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.util.Tasks
import net.starlegacy.util.randomDouble
import org.bukkit.Color
import org.bukkit.Color.RED
import org.bukkit.Color.fromRGB
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Particle.REDSTONE
import org.bukkit.craftbukkit.v1_19_R2.CraftParticle
import org.bukkit.entity.Flying
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.Locale
import java.util.function.Supplier

abstract class Blaster<T : Balancing>(
	identifier: String,

	material: Material,
	customModelData: Int,
	val displayName: Component,

	private val balancingSupplier: Supplier<T>
) : AmmunitionHoldingItem(identifier, material, customModelData, displayName) {
	val balancing get() = balancingSupplier.get()

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		fireWeapon(livingEntity, itemStack)

		if (!balancing.shouldAkimbo) return
		if (livingEntity !is Player) return // getting non-players to akimbo is too hard

		val otherItemStack = livingEntity.inventory.itemInOffHand
		val otherCustomItem = otherItemStack.customItem as? Blaster<*> ?: return

		if (!otherCustomItem.balancing.shouldAkimbo) return

		Tasks.syncDelay((balancing.timeBetweenShots / 2).toLong()) {
			otherCustomItem.fireWeapon(livingEntity, otherItemStack)
		}
	}

	override fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown

		var ammo = getAmmunition(itemStack)

		if (ammo == ((itemStack.customItem as? Blaster<*>)?.getMaximumAmmunition() ?: return)) return

		for (magazineItem in livingEntity.inventory) {
			if (ammo >= balancing.magazineSize) continue // Check if magazine is full
			if (magazineItem == null) continue // Check not null
			if (magazineItem.customItem !is Magazine<*>) continue // Only Magazines

			val magazineAmmo = STANDARD_MAGAZINE.getAmmunition(magazineItem)
			val amountToTake = (balancing.magazineSize - ammo).coerceAtMost(magazineAmmo)
			STANDARD_MAGAZINE.setAmmunition(magazineItem, livingEntity.inventory, magazineAmmo - amountToTake)

			ammo += amountToTake
		}

		if (livingEntity.world.name.lowercase(Locale.getDefault()).contains("arena")) ammo = balancing.magazineSize

		if (ammo == 0) {
			livingEntity.playSound(
				net.kyori.adventure.sound.Sound.sound(
					Key.key("minecraft:item.bundle.drop_contents"),
					net.kyori.adventure.sound.Sound.Source.MASTER,
					5f,
					2.00f
				)
			)
			livingEntity.sendInformation("Out of ammo!")
			return
		}

		livingEntity.setCooldown(itemStack.type, this.balancing.reload)

		setAmmunition(itemStack, livingEntity.inventory, ammo)

		// TODO: Use durability to indicate ammo
		livingEntity.sendActionBar(text("Ammo: $ammo / ${balancing.magazineSize}", NamedTextColor.RED))

		livingEntity.playSound(
			net.kyori.adventure.sound.Sound.sound(
				Key.key("minecraft:block.iron_door.close"),
				net.kyori.adventure.sound.Sound.Source.MASTER,
				5f,
				2.00f
			)
		)
	}

	override fun getMaximumAmmunition(): Int = balancing.magazineSize

	override fun setAmmunition(itemStack: ItemStack, inventory: Inventory, ammunition: Int) {
		super.setAmmunition(itemStack, inventory, ammunition)

		if (getAmmunition(itemStack) == 0) {
			(inventory.holder as? Player)?.playSound(
				net.kyori.adventure.sound.Sound.sound(
					Key.key("minecraft:block.iron_door.open"),
					net.kyori.adventure.sound.Sound.Source.MASTER,
					5f,
					2.00f
				)
			)
		}

		// TODO: Use durability to indicate ammo
		(inventory.holder as? Audience)?.sendActionBar(text("Ammo: ${ammunition.coerceIn(0, balancing.magazineSize)} / ${balancing.magazineSize}", NamedTextColor.RED))
	}

	private fun fireWeapon(livingEntity: LivingEntity, itemStack: ItemStack) {
		if ((livingEntity as? Player)?.hasCooldown(itemStack.type) == true) return // Cooldown
		if (livingEntity is Player && !checkAndDecrementAmmo(itemStack, livingEntity)) return // Ammo

		livingEntity.location.world.playSound(livingEntity.location, "laser", 1f, balancing.pitch)
		fireProjectiles(livingEntity)
	}

	private fun getParticleType(entity: LivingEntity): Particle {
		if (entity !is Player) return REDSTONE // Not Player
		PlayerData[entity.uniqueId].particle?.let { return CraftParticle.toBukkit(PARTICLE_TYPE.get(ResourceLocation(it))) } // Player
		return REDSTONE // Default
	}

	private fun getParticleColor(entity: LivingEntity): Color {
		if (entity !is Player) return RED // Not Player
		SLPlayer[entity.uniqueId]?.nation?.let { return fromRGB(NationCache[it].color) } // Nation
		PlayerData[entity.uniqueId].color?.let { return fromRGB(it) } // Player
		return RED // Not Player
	}

	protected open fun fireProjectiles(livingEntity: LivingEntity) {
		val location = livingEntity.eyeLocation.clone()

		location.y = location.y - 0.5

		if (balancing.shotDeviation > 0) {
			val offsetX = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetY = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetZ = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)

			val newDirection = location.direction.clone().add(Vector(offsetX, offsetY, offsetZ)).normalize()

			location.direction = location.direction.add(Vector(offsetX, offsetY, offsetZ)).normalize()
			location.subtract(newDirection)
		}

		ProjectileManager.addProjectile(
			RayTracedParticleProjectile(
				location,
				livingEntity,
				balancing,
				getParticleType(livingEntity),
				if (getParticleType(livingEntity) == REDSTONE) DustOptions(getParticleColor(livingEntity), 1f) else null
			)
		)

		val recoil = balancing.recoil / balancing.packetsPerShot

		for (iteration in 1..balancing.packetsPerShot) {
			if (livingEntity is Flying) return

			Tasks.syncDelay(iteration.toLong()) {
				val loc = livingEntity.location
				loc.pitch -= recoil

				@Suppress("UnstableApiUsage")
				(livingEntity as? Player)?.teleport(loc, PLUGIN, true, false, *RelativeTeleportFlag.values())
			}
		}
	}

	private fun checkAndDecrementAmmo(itemStack: ItemStack, livingEntity: InventoryHolder): Boolean {
		val ammo = getAmmunition(itemStack)
		if (ammo == 0) {
			(livingEntity as? Player)?.playSound(
				net.kyori.adventure.sound.Sound.sound(
					Key.key("minecraft:block.iron_door.open"),
					net.kyori.adventure.sound.Sound.Source.MASTER,
					5f,
					2.00f
				)
			)
			return false
		}

		setAmmunition(itemStack, livingEntity.inventory, ammo - 1)

		(livingEntity as? Player)?.setCooldown(itemStack.type, (balancing.timeBetweenShots - 1).coerceAtLeast(0))

		return true
	}
}

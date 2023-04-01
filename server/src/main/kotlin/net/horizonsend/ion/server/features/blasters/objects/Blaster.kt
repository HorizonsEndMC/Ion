package net.horizonsend.ion.server.features.blasters.objects

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.server.configuration.BalancingConfiguration.EnergyWeapon.Balancing
import net.horizonsend.ion.server.features.blasters.ProjectileManager
import net.horizonsend.ion.server.features.blasters.RayTracedParticleProjectile
import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
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
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_19_R2.CraftParticle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
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
	displayName: Component,
	val magazineType: Magazine<*>,
	val soundRange: Double,
	val soundFire: String,
	val soundWhizz: String,

	private val balancingSupplier: Supplier<T>
) : AmmunitionHoldingItem(identifier, material, customModelData, displayName) {
	val balancing get() = balancingSupplier.get()

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		fireWeapon(livingEntity, itemStack)
	}

	override fun handleTertiaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown

		val originalAmmo =	getAmmunition(itemStack)

		var ammo = originalAmmo

		if (ammo == ((itemStack.customItem as? Blaster<*>)?.getMaximumAmmunition() ?: return)) return

		for (magazineItem in livingEntity.inventory) {
			if (magazineItem == null) continue // check not null
			val magazineCustomItem: CustomItem = magazineItem.customItem ?: continue // To get magazine properties
			if (ammo >= balancing.magazineSize) continue // Check if blaster magazine is full
			if (magazineCustomItem.identifier != magazineType.identifier) continue // Only correct magazine

			val magazineAmmo = (magazineCustomItem as AmmunitionHoldingItem).getAmmunition(magazineItem)
			val amountToTake = (balancing.magazineSize - ammo).coerceAtMost(magazineAmmo)
			magazineCustomItem.setAmmunition(magazineItem, livingEntity.inventory, magazineAmmo - amountToTake)

			ammo += amountToTake
		}

		if (livingEntity.world.name.lowercase(Locale.getDefault()).contains("arena")) ammo = balancing.magazineSize

		if (ammo - originalAmmo == 0) {
			livingEntity.playSound(
				net.kyori.adventure.sound.Sound.sound(
					Key.key("minecraft:item.bundle.drop_contents"),
					net.kyori.adventure.sound.Sound.Source.MASTER,
					5f,
					2.00f
				)
			)
			livingEntity.alert("Out of ammo!")
			return
		}

		livingEntity.setCooldown(itemStack.type, this.balancing.reload)

		setAmmunition(itemStack, livingEntity.inventory, ammo)

		Tasks.syncDelay(this.balancing.reload.toLong()) {
			livingEntity.playSound(
				net.kyori.adventure.sound.Sound.sound(
					Key.key("minecraft:block.iron_door.close"), // TODO custom sound
					net.kyori.adventure.sound.Sound.Source.MASTER,
					5f,
					2.00f
				)
			)
		}

		// TODO: Use durability to indicate ammo
		livingEntity.sendActionBar(text("Ammo: $ammo / ${balancing.magazineSize}", NamedTextColor.RED))

		livingEntity.playSound(
			net.kyori.adventure.sound.Sound.sound(
				Key.key("minecraft:block.iron_door.close"), // TODO custom sound
				net.kyori.adventure.sound.Sound.Source.MASTER,
				5f,
				2.00f
			)
		)
	}

	override fun getMaximumAmmunition(): Int = balancing.magazineSize
	override fun getTypeRefill(): String = balancing.refillType
	override fun getAmmoPerRefill(): Int = balancing.ammoPerRefill

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
		if (livingEntity is Player) {
			if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown
			if (!checkAndDecrementAmmo(itemStack, livingEntity)) {
				handleTertiaryInteract(livingEntity, itemStack) // Force a reload
				return // No Ammo
			}
		}

		// Shoot sound
		val soundOrigin = livingEntity.location
		soundOrigin.world.players.forEach {

			var distanceFactor = soundRange
			var volumeFactor = 1.0
			var pitchFactor = 1.0

			// No sounds in space (somewhat)
			if (livingEntity.world.toString().contains("Space")) {
				distanceFactor *= 0.5
				volumeFactor *= 0.25
				pitchFactor *= 0.5
			}

			// Sound is unmodified if players within 0.5*range distance of shooter
			// Modify sound until fully inaudible at 2.0*range distance of shooter
			if (it.location.distance(soundOrigin) >= distanceFactor * 0.5 &&
				it.location.distance(soundOrigin) < distanceFactor * 2) {
				volumeFactor *= (-1.0 / (2.0 * distanceFactor)) * it.location.distance(soundOrigin) + 1.25
				pitchFactor *= (-1.0 / (3.0 * distanceFactor)) * it.location.distance(soundOrigin) + 1.165
			}

			if (it.location.distance(soundOrigin) < distanceFactor * 2)
				soundOrigin.world.playSound(
					it.location,
					soundFire,
					SoundCategory.PLAYERS,
					volumeFactor.toFloat(),
					pitchFactor.toFloat()
				)
		}

		fireProjectiles(livingEntity)
	}

	private fun getParticleType(entity: LivingEntity): Particle {
		if (entity !is Player) return REDSTONE // Not Player
		PlayerData[entity.uniqueId]?.particle?.let { return CraftParticle.toBukkit(PARTICLE_TYPE.get(ResourceLocation(it))) } // Player
		return REDSTONE // Default
	}

	private fun getParticleColor(entity: LivingEntity): Color {
		if (entity !is Player) return RED // Not Player
		SLPlayer[entity.uniqueId]?.nation?.let { return fromRGB(NationCache[it].color) } // Nation
		PlayerData[entity.uniqueId]?.color?.let { return fromRGB(it) } // Player
		return RED // Not Player
	}

	protected open fun fireProjectiles(livingEntity: LivingEntity) {
		val location = livingEntity.eyeLocation.clone()

		location.y = location.y - 0.125

		if (balancing.shotDeviation > 0) {
			val offsetX = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetY = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetZ = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)

			location.direction = location.direction.clone().add(Vector(offsetX, offsetY, offsetZ)).normalize()
		}

		location.add(location.direction.clone().multiply(0.125))

		val projectile = RayTracedParticleProjectile(
			location,
			livingEntity,
			balancing,
			getParticleType(livingEntity),
			if (getParticleType(livingEntity) == REDSTONE) DustOptions(getParticleColor(livingEntity), 1f) else null,
			soundWhizz
		)

		ProjectileManager.addProjectile(projectile)

// 		if (livingEntity is CraftPlayer) {
// 			for (i in 0..livingEntity.handle.latency.floorDiv(50)) projectile.tick()
// 		}
	}

	private fun checkAndDecrementAmmo(itemStack: ItemStack, livingEntity: InventoryHolder): Boolean {
		val ammo = getAmmunition(itemStack)
		if (ammo == 0) {
			(livingEntity as? Player)?.playSound(
				net.kyori.adventure.sound.Sound.sound(
					Key.key("minecraft:block.iron_door.open"), // TODO custom sound
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

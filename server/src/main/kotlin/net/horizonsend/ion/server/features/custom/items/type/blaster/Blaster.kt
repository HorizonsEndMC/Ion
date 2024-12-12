package net.horizonsend.ion.server.features.custom.items.type.blaster

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Balancing
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.AmmunitionStorage
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.playerSwapHandsListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.MagazineType
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Color
import org.bukkit.Color.fromRGB
import org.bukkit.Particle.DUST
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.function.Supplier

open class Blaster<T : Balancing>(
	identifier: String,
	displayName: Component,
	itemFactory: ItemFactory,
	private val balancingSupplier: Supplier<T>
) : CustomItem(
	identifier,
	displayName,
	itemFactory,
) {
	val balancing get() = balancingSupplier.get()

	val ammoComponent = AmmunitionStorage(balancingSupplier)
	val magazineComponent = MagazineType(balancingSupplier) { CustomItemRegistry.getByIdentifier(balancing.magazineIdentifier)!! }

	override fun decorateItemStack(base: ItemStack) {
		ammoComponent.setAmmo(base, this, balancing.capacity)
	}

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentTypes.AMMUNITION_STORAGE, ammoComponent)
		addComponent(CustomComponentTypes.MAGAZINE_TYPE, magazineComponent)

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@Blaster) { event, _, item -> fire(event.player, item) })
		addComponent(CustomComponentTypes.LISTENER_PLAYER_SWAP_HANDS, playerSwapHandsListener(this@Blaster) { event, _, item -> reload(event.player, item) })
	}

	open fun fire(shooter: LivingEntity, blasterItem: ItemStack) {
		if (shooter is Player) {
			if (shooter.hasCooldown(blasterItem.type)) return // Cooldown

			if (!checkAndDecrementAmmo(blasterItem, shooter)) return reload(shooter, blasterItem)
		}

		val soundOrigin = shooter.location

		// Shell sound
		/*
		var relativeBlock = livingEntity.location.block.getRelative(DOWN)
		val maxDistance = 4 // Add 1 to this value for the actualStyle distance

		for (i in 0..maxDistance) {
			if (!relativeBlock.isSolid) {
				relativeBlock = relativeBlock.getRelative(DOWN)
				continue
			}

			Tasks.syncDelay(randomInt(5, 10).toLong()) {
				soundOrigin.world.playSound(soundOrigin, soundShell, PLAYERS, 0.5f, 1.0f)
			}
			break
		}
		*/

		// Shoot sound
		soundOrigin.world.players.forEach { player ->
			var distanceFactor = balancing.soundRange
			var volumeFactor = 1.0
			var pitchFactor = 1.0

			// No sounds in space (somewhat)
			if (shooter.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
				distanceFactor *= 0.5
				volumeFactor *= 0.25
				pitchFactor *= 0.5
			}

			// Sound is unmodified if players within 0.5*range distance of shooter
			// Modify sound until fully inaudible at 2.0*range distance of shooter
			if (player.location.distance(soundOrigin) >= distanceFactor * 0.5 && player.location.distance(soundOrigin) < distanceFactor * 2) {
				volumeFactor *= (-1.0 / (2.0 * distanceFactor)) * player.location.distance(soundOrigin) + 1.25
				pitchFactor *= (-1.0 / (3.0 * distanceFactor)) * player.location.distance(soundOrigin) + 1.165
			}

			if (player.location.distance(soundOrigin) < distanceFactor * 2) {
				val modified = balancing.soundFire.copy(
					volume = volumeFactor.toFloat(),
					pitch = pitchFactor.toFloat()
				)

				player.playSound(modified.sound, soundOrigin.x, shooter.y, soundOrigin.z)
			}
		}

		fireProjectiles(shooter)
	}

	open fun fireProjectiles(livingEntity: LivingEntity) {
		val location = livingEntity.eyeLocation.clone()

		location.y -= 0.125

		if (balancing.shotDeviation > 0) {
			val offsetX = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetY = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetZ = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)

			location.direction = location.direction.clone().add(Vector(offsetX, offsetY, offsetZ)).normalize()
		}

		location.add(location.direction.clone().multiply(0.125))

		RayTracedParticleProjectile(
			location,
			livingEntity,
			balancing,
			DUST,
			balancing.explosiveShot,
			DustOptions(
				getParticleColor(livingEntity),
				balancing.particleSize
			),
			balancing.soundWhizz,
		).fire()
	}

	/**
	 * Returns whether the reload was successful
	 **/
	private fun checkAndDecrementAmmo(itemStack: ItemStack, livingEntity: LivingEntity): Boolean {
		val ammo = ammoComponent.getAmmo(itemStack)
		if (ammo == 0) {
			livingEntity.playSound(sound(key("horizonsend:blaster.dry_shoot"), PLAYER, 1.0f, 1.0f))
			return false
		}

		ammoComponent.setAmmo(itemStack, this, ammo - 1)

		(livingEntity as? Player)?.setCooldown(itemStack.type, (balancing.timeBetweenShots - 1).coerceAtLeast(0))

		return true
	}

	fun reload(livingEntity: LivingEntity, blasterItem: ItemStack) {
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(blasterItem.type)) return // Cooldown

		val originalAmmo = ammoComponent.getAmmo(blasterItem)

		var ammo = originalAmmo
		if (ammo == balancing.capacity) return

		if (balancing.consumesAmmo) {
			for (magazineItem in livingEntity.inventory.filterNotNull()) {
				if (ammo >= balancing.capacity) break // Check if blaster magazine is full

				val magazineCustomItem = magazineItem.customItem ?: continue // To get magazine properties
				if (magazineCustomItem !is Magazine) continue // Just to smart cast

				if (magazineCustomItem.identifier != balancing.magazineIdentifier) continue // Only correct magazine

				val magazineAmmo = magazineCustomItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).getAmmo(magazineItem)
				val amountToTake = (balancing.capacity - ammo).coerceAtMost(magazineAmmo)

				magazineCustomItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).setAmmo(magazineItem, magazineCustomItem, magazineAmmo - amountToTake)

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

		ammoComponent.setAmmo(blasterItem, this, ammo)

		livingEntity.sendActionBar(template(text("Ammo: {0} / {1}", RED), ammo.coerceIn(0, balancing.capacity), balancing.capacity))
		if (ammo <= 0) livingEntity.playSound(sound(key("minecraft:block.iron_door.open"), PLAYER, 5f, 2.00f))

		// Start reload
		livingEntity.world.playSound(balancing.soundReloadStart.sound, livingEntity)

		// Finish reload
		Tasks.syncDelay(this.balancing.reload.toLong()) {
			livingEntity.world.playSound(balancing.soundReloadFinish.sound, livingEntity)
		}
	}

	private fun getParticleColor(entity: LivingEntity): Color {
		if (entity !is Player) return Color.RED // Not Player
		SLPlayer[entity.uniqueId]?.nation?.let { return fromRGB(NationCache[it].color) } // Nation
		return Color.RED // Not Player
	}
}

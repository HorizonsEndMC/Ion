package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.EnergyWeapons.Balancing
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.objects.AmmunitionHoldingItem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Color.RED
import org.bukkit.Color.fromRGB
import org.bukkit.Material
import org.bukkit.Material.matchMaterial
import org.bukkit.Particle
import org.bukkit.Particle.DUST
import org.bukkit.Particle.DustOptions
import org.bukkit.SoundCategory.PLAYERS
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.function.Supplier

open class Blaster<T : Balancing>(
	identifier: String,

	override val material: Material,
	override val customModelData: Int,
	override val displayName: Component,
	val magazineType: Magazine<*>,
	val particleSize: Float,
	val soundRange: Double,
	val soundFire: String,
	val soundWhizz: String,
	val soundShell: String,
	val soundReloadStart: String,
	val soundReloadFinish: String,
	val explosiveShot: Boolean,

	private val balancingSupplier: Supplier<T>
) : CustomItem(identifier), AmmunitionHoldingItem {
	val balancing get() = balancingSupplier.get()

	override fun constructItemStack(): ItemStack {
		val base = getFullItem()

		// Should always have lore
		val lore = base.itemMeta.lore()!!.toTypedArray()

		val refillTypeComponent = if (getConsumesAmmo()) text()
				.decoration(ITALIC, false)
				.append(text("Refill: ", GRAY))
				.append(translatable(matchMaterial(getTypeRefill())!!.translationKey(), AQUA))
				.build()
		else null

		val magazineTypeComponent = if (getConsumesAmmo()) text()
				.decoration(TextDecoration.ITALIC, false)
				.append(text("Magazine: ", NamedTextColor.GRAY))
				.append(magazineType.displayName).color(NamedTextColor.AQUA)
				.build()
		else null

		return base.updateMeta {
			it.lore(mutableListOf(
				*lore,
				refillTypeComponent,
				magazineTypeComponent
			))
		}
	}

	override fun handleSwapHands(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown

		val originalAmmo = getAmmunition(itemStack)

		var ammo = originalAmmo

		if (ammo == ((itemStack.customItem as? Blaster<*>)?.getMaximumAmmunition() ?: return)) return

		if (balancing.consumesAmmo) {
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
		}

		if (livingEntity.world.hasFlag(WorldFlag.ARENA) || !balancing.consumesAmmo) {
			ammo = balancing.magazineSize
		}

		if (ammo - originalAmmo == 0) {
			livingEntity.playSound(
				sound(
					key("minecraft:item.bundle.drop_contents"),
					PLAYER,
					5f,
					2.00f
				)
			)
			livingEntity.alert("Out of ammo!")
			return
		}

		livingEntity.setCooldown(itemStack.type, this.balancing.reload)

		setAmmunition(itemStack, livingEntity.inventory, ammo)

		// Finish reload
		Tasks.syncDelay(this.balancing.reload.toLong()) {
			livingEntity.location.world.playSound(
				livingEntity.location,
				soundReloadFinish,
				PLAYERS,
				1.0f,
				1.0f
			)
		}

		livingEntity.sendActionBar(text("Ammo: $ammo / ${balancing.magazineSize}", NamedTextColor.RED))

		// Start reload
		livingEntity.location.world.playSound(
			livingEntity.location,
			soundReloadStart,
			PLAYERS,
			1.0f,
			1.0f
		)
	}

	override fun getMaximumAmmunition(): Int = balancing.magazineSize

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		fireWeapon(livingEntity, itemStack)
	}
	override fun getTypeRefill(): String = magazineType.getTypeRefill()
	override fun getAmmoPerRefill(): Int = balancing.ammoPerRefill
	override fun getConsumesAmmo(): Boolean = balancing.consumesAmmo

	override fun setAmmunition(itemStack: ItemStack, inventory: Inventory, ammunition: Int) {
		super.setAmmunition(itemStack, inventory, ammunition)

		if (getAmmunition(itemStack) == 0) {
			(inventory.holder as? Player)?.playSound(sound(key("minecraft:block.iron_door.open"), PLAYER, 5f, 2.00f))
		}

		(inventory.holder as? Audience)?.sendActionBar(
			text("Ammo: ${ammunition.coerceIn(0, balancing.magazineSize)} / " +
					"${balancing.magazineSize}", NamedTextColor.RED)
		)
	}

	private fun fireWeapon(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity is Player) {
			if (livingEntity.hasCooldown(itemStack.type)) return // Cooldown
			if (!checkAndDecrementAmmo(itemStack, livingEntity)) {
				handleSwapHands(livingEntity, itemStack) // Force a reload
				return // No Ammo
			}
		}

		val soundOrigin = livingEntity.location

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
		soundOrigin.world.players.forEach {

			var distanceFactor = soundRange
			var volumeFactor = 1.0
			var pitchFactor = 1.0

			// No sounds in space (somewhat)
			if (livingEntity.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
				distanceFactor *= 0.5
				volumeFactor *= 0.25
				pitchFactor *= 0.5
			}

			// Sound is unmodified if players within 0.5*range distance of shooter
			// Modify sound until fully inaudible at 2.0*range distance of shooter
			if (it.location.distance(soundOrigin) >= distanceFactor * 0.5 &&
				it.location.distance(soundOrigin) < distanceFactor * 2
			) {
				volumeFactor *= (-1.0 / (2.0 * distanceFactor)) * it.location.distance(soundOrigin) + 1.25
				pitchFactor *= (-1.0 / (3.0 * distanceFactor)) * it.location.distance(soundOrigin) + 1.165
			}

			if (it.location.distance(soundOrigin) < distanceFactor * 2)
				soundOrigin.world.playSound(
					it.location,
					soundFire,
					PLAYERS,
					volumeFactor.toFloat(),
					pitchFactor.toFloat()
				)
		}

		fireProjectiles(livingEntity)
	}

	private fun getParticleType(entity: LivingEntity): Particle = DUST // Default

	private fun getParticleColor(entity: LivingEntity): Color {
		if (entity !is Player) return RED // Not Player
		SLPlayer[entity.uniqueId]?.nation?.let { return fromRGB(NationCache[it].color) } // Nation
		return RED // Not Player
	}

	protected open fun fireProjectiles(livingEntity: LivingEntity) {
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
			explosiveShot,
			DustOptions(
				getParticleColor(livingEntity),
				particleSize
			),
			soundWhizz,
		).fire()
	}

	private fun checkAndDecrementAmmo(itemStack: ItemStack, livingEntity: InventoryHolder): Boolean {
		val ammo = getAmmunition(itemStack)
		if (ammo == 0) {
			(livingEntity as? Player)?.playSound(
				sound(
					key("horizonsend:blaster.dry_shoot"),
					PLAYER,
					1.0f,
					1.0f
				)
			)
			return false
		}

		setAmmunition(itemStack, livingEntity.inventory, ammo - 1)

		(livingEntity as? Player)?.setCooldown(itemStack.type, (balancing.timeBetweenShots - 1).coerceAtLeast(0))

		return true
	}
}

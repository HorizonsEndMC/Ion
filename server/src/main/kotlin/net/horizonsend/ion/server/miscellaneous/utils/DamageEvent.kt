@file:Suppress("DEPRECATION", "UnstableApiUsage")

package net.horizonsend.ion.server.miscellaneous.utils

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.google.common.base.Function
import io.papermc.paper.event.entity.EntityKnockbackEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.min


object DamageEvent {

	fun doDamageEvent(
		baseDamage: Double,
		cause: EntityDamageEvent.DamageCause,
		entity: Damageable,
		damager: Entity,
		damageType: DamageType,
		damageLocation: Location,
		critical: Boolean,
		goesThroughBlocking: Boolean,
	){
		if (entity.isInvulnerable||entity.isDead) return
		val wasSprinting = (entity as? Player)?.isSprinting

		val source = DamageSource.builder(damageType)
			.withDamageLocation(damageLocation)
			.withDirectEntity(entity)
			.withCausingEntity(damager).build()
		//Again I know this is deprecated, but even paper uses it in a non deprecated builder, so I think a mistake was made
		val damageModifiers = mutableMapOf<DamageModifier, Double>()
		val modifierFunctions: MutableMap<DamageModifier, Function<in Double, Double>> = mutableMapOf()

		damageModifiers[DamageModifier.BASE] = baseDamage
		damageModifiers[DamageModifier.ARMOR] = calculateArmourModifier(entity, baseDamage)
		damageModifiers[DamageModifier.BLOCKING] = 0.0
		damageModifiers[DamageModifier.RESISTANCE] = 0.0

		//The other DamageModifiers do not seem to be relevant for HE specific combat, as such they've been excluded
		modifierFunctions[DamageModifier.BASE] = Function { damage: Double -> damage }
		modifierFunctions[DamageModifier.ARMOR] = Function { damage: Double -> damage }
		modifierFunctions[DamageModifier.BLOCKING] = Function { damage: Double -> damage }
		modifierFunctions[DamageModifier.RESISTANCE] = Function { damage: Double -> damage }

		if (!goesThroughBlocking && (entity as? LivingEntity)?.activeItem?.type == Material.SHIELD){
			damageModifiers[DamageModifier.BLOCKING] = -baseDamage
		}

		//If the player has resistance, on HE this will likely never happen however it never hurts to be prepared
		if (entity is LivingEntity && entity.hasPotionEffect(PotionEffectType.RESISTANCE)) {
			val level = entity.getPotionEffect(PotionEffectType.RESISTANCE)?.amplifier ?: 0
			val resistanceReduction = baseDamage * (0.2 * (level + 1))
			damageModifiers[DamageModifier.RESISTANCE] = -resistanceReduction
		}

		val event = EntityDamageByEntityEvent(damager, entity, cause, source, damageModifiers, modifierFunctions, critical)
		event.callEvent()
		if (!event.isCancelled){
			entity.damage(event.finalDamage, damager)
		}

		(entity as? Player)?.isSprinting =  wasSprinting ?: false //We dont want to stop the entity sprinting by accident
	}

	private fun calculateArmourModifier(entity: Damageable, damage: Double): Double {
		//If no equipment, then armour will not be modifying the damage
		val armourPieces = (entity as? LivingEntity)?.equipment ?: return 0.0
		var sumOfTheArmour = 0.0 //Fuck the american spelling, we can afford the letter U in Europe
		var sumOfTheToughness = 0.0
		for (i in armourPieces.armorContents) {
			var sumOfTheArmourValueInThisPiece = 0.0
			var sumOfTheToughnessValueInThisPiece = 0.0
			try {
				for (j in i.itemMeta.attributeModifiers?.get(Attribute.ARMOR)?.toList() ?: continue) {
					when (j.operation) {
						AttributeModifier.Operation.ADD_SCALAR -> sumOfTheArmourValueInThisPiece += j.amount
						AttributeModifier.Operation.ADD_NUMBER -> sumOfTheArmourValueInThisPiece += j.amount
						AttributeModifier.Operation.MULTIPLY_SCALAR_1 -> sumOfTheArmourValueInThisPiece *= j.amount
					}
				}
				for (j in i.itemMeta.attributeModifiers?.get(Attribute.ARMOR_TOUGHNESS)?.toList() ?: continue) {
					when (j.operation) {
						AttributeModifier.Operation.ADD_SCALAR -> sumOfTheToughnessValueInThisPiece += j.amount
						AttributeModifier.Operation.ADD_NUMBER -> sumOfTheToughnessValueInThisPiece += j.amount
						AttributeModifier.Operation.MULTIPLY_SCALAR_1 -> sumOfTheToughnessValueInThisPiece *= j.amount
					}
				}
				sumOfTheArmour += sumOfTheArmourValueInThisPiece
				sumOfTheToughness += sumOfTheToughnessValueInThisPiece
			} catch (_: NullPointerException) {}//Some mobs dont have armour shockingly so for those, we silently fail
		}
		//This formula is found here https://gaming.stackexchange.com/questions/357775/what-is-the-minecraft-armor-equation
		return damage * (1 - min(
			20.0,
			max(sumOfTheArmour / 5, sumOfTheArmour - damage / (2 + (sumOfTheToughness / 4)))
		) / 25.0) - damage
	}
}

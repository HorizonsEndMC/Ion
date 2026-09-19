package net.horizonsend.ion.server.features.starship.subsystem.command_burst

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.starship.StarshipCommandBurstBalancing
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getEnumSettingOrThrow
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsMainMenuGui
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AbstractCommandBurstMultiblock
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadiusMatchingPredicate
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.horizonsend.ion.server.features.transport.items.util.DYEABLE_CUBE_MONO
import net.horizonsend.ion.server.miscellaneous.playSoundInRadius
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.litote.kmongo.out
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.sin

abstract class AbstractCommandBurstSubsystem<T : StarshipCommandBurstBalancing>(
	starship: Starship,
	sign: Sign,
	multiblock: AbstractCommandBurstMultiblock,
	val balancingSupplier: Supplier<T>
) : AbstractMultiblockSubsystem<AbstractCommandBurstMultiblock>(starship, sign, multiblock) {

	/** Balancing values for this subsystem **/
	val balancing get() = balancingSupplier.get()
	var lastActivated: Long = System.currentTimeMillis()

	/** Cooldown between activating abilities of this command burst **/
	open val activateCooldownMillis: Long get() = balancing.activateCooldownMillis

	abstract val color: Color

	fun isCooledDown(): Boolean {
		return System.currentTimeMillis() - lastActivated >= activateCooldownMillis
	}

	fun canCreateSubsystem(): Boolean {
		if (starship.type.eventShip) return true
		if (!balancing.activateRestrictions.canActivate && !starship.type.eventShip) return false
		return starship.initialBlockCount in balancing.activateRestrictions.minBlockCount..balancing.activateRestrictions.maxBlockCount
	}

	fun activate() {
		val starshipsInRange = ActiveStarships.getInWorld(starship.world).filter { otherStarship ->
			otherStarship.centerOfMass.toLocation(otherStarship.world).distanceSquared(starship.centerOfMass.toLocation(starship.world)) <= balancing.range * balancing.range
		}

		playSoundInRadius(
			starship.centerOfMass.toLocation(starship.world),
			balancing.range,
			Sound.sound(key("horizonsend:starship.weapon.command_burst.fire"), Sound.Source.PLAYER, 5.0f, 1.0f))

		activateEffect(starshipsInRange.toSet())
		spawnBeam()
		spawnParticles()
	}

	protected abstract fun activateEffect(starships: Set<Starship>)

	fun postActivate() {
		lastActivated = System.currentTimeMillis()
	}

	abstract fun getName(): Component

	fun spawnParticles() {
		val filter = {
				player: Player ->
			val playerSetting = player.getEnumSettingOrThrow<SettingsMainMenuGui.Companion.DisplayOrParticle>(PlayerSettings::commandBurst)
			when (playerSetting) {
				SettingsMainMenuGui.Companion.DisplayOrParticle.DISPLAY -> false
				else -> true
			}
		}

		val task = Tasks.syncRepeatTask(0L, 2L) {
			val points = starship.centerOfMass.toLocation(starship.world).spherePoints(100.0, 120)

			//512 is the max radius one can see particles when forced
			toPlayersInRadiusMatchingPredicate(starship.centerOfMass.toLocation(starship.world), 512.0, filter){
				for (endPoint in points) {
					it.spawnParticle(
						Particle.TRAIL,
						starship.centerOfMass.toLocation(starship.world),
						1,
						0.5,
						0.5,
						0.5,
						0.0,
						Particle.Trail(endPoint, color, randomInt(90, 100)),
						true
					)
				}
			}
		}
		Tasks.syncDelay(60L) {
			task.cancel()
		}
	}

	fun spawnBeam() {
		val item = DYEABLE_CUBE_MONO.construct {
			t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color))
		}

		val filter = {
			player: Player ->
			val playerSetting = player.getEnumSettingOrThrow<SettingsMainMenuGui.Companion.DisplayOrParticle>(PlayerSettings::commandBurst)
			when (playerSetting) {
				SettingsMainMenuGui.Companion.DisplayOrParticle.PARTICLE -> false
				else -> true
			}
		}

		val outerBeam = ItemDisplayContainer(
			starship.world,
			1.0f,
			starship.centerOfMass.toVector(),
			Vector(0,0,1),
			item,
			1,
			filter
		)
		val innerBeam = ItemDisplayContainer(
			starship.world,
			1.0f,
			starship.centerOfMass.toVector(),
			Vector(0,0,1),
			ItemStack(Material.WHITE_CONCRETE),
			1,
			filter
		)

		var t = 0
		val lifetime = 100L
		val task = Tasks.syncRepeat(0,1){
			val phase = (t.toDouble() / lifetime).coerceIn(0.0, 1.0)
			t++
			val pulse = 0.5f * (1.0f + sin(phase * PI)) // 0..1..0
			val s = 1.1f + 0.5f * pulse.toFloat()
			innerBeam.scale = Vector(s*.8f,400f,s*.8f)
			outerBeam.scale = Vector(s*1.2f,400f,s*1.2f)
			innerBeam.update()
			outerBeam.update()
		}

		Tasks.syncDelay(lifetime) {
			task.cancel()
			outerBeam.remove()
			innerBeam.remove()
		}
	}
}

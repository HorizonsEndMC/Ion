package net.horizonsend.ion.server.features.explosions.presets

import com.google.common.base.Function
import net.horizonsend.ion.server.features.explosions.effects.BurnOptions
import net.horizonsend.ion.server.features.explosions.effects.BurnWaveOptions
import net.horizonsend.ion.server.features.explosions.effects.BurnWavePlacement
import net.horizonsend.ion.server.features.explosions.effects.Burner
import net.horizonsend.ion.server.features.explosions.effects.FlashBurnOptions
import net.horizonsend.ion.server.features.explosions.effects.FlashBurnPlacement
import net.horizonsend.ion.server.features.explosions.effects.ShockWaveOptions
import net.horizonsend.ion.server.features.explosions.effects.ShockWavePlacement
import net.horizonsend.ion.server.features.explosions.effects.flashBurn
import net.horizonsend.ion.server.features.explosions.effects.spawnBurnWave
import net.horizonsend.ion.server.features.explosions.effects.spawnShockwave
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import kotlin.math.log10

class MiniNukeModExplosion(val location: Location){
	fun spawnExplosion(entityThatExploded: Entity){
		val vector = location.toVector()
		val radiationOrigin = vector.clone().add(Vector(.0,5.0, .0))
		val burner = Burner(burner)
		val world = location.world

		val shockWave = shockWave
		val burnWave = burnWave
		if (world.hasFlag(WorldFlag.SPACE_WORLD)){
			shockWave.scanUpwards = 0.0
			burnWave.scanUpwards = 0.0
		}

		world.playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 10f, 1f)
		//You cannot async play a sound
		Tasks.syncDelay(5) {
			world.playSound(location, Sound.BLOCK_CONDUIT_AMBIENT, 10f, 1f)
			world.playSound(location, Sound.ENTITY_WARDEN_SONIC_CHARGE,10f,1f)
		}
		Tasks.syncDelay(40) {
			val placement = ShockWavePlacement(world = world, origin = vector, render = vector)
			spawnShockwave(placement, shockWave, burner, entityThatExploded)
			world.createExplosion(location, 10f)
		}
		Tasks.asyncDelay(46) {
			val placement = BurnWavePlacement(world = world, origin = vector, render = vector)
			spawnBurnWave(placement, burnWave, burner)
		}
		Tasks.asyncDelay(48) {
			val placement = FlashBurnPlacement(world = world, origin = radiationOrigin, render = vector)
			flashBurn(placement, flashBurn, burner)
		}
	}
	companion object {
		val shockWave = ShockWaveOptions()
		val burnWave = BurnWaveOptions()
		val flashBurn = FlashBurnOptions()
		val burner = BurnOptions()

		init {

			//Shockwave
			shockWave.count = 12
			shockWave.destructionRadius = 2.0
			shockWave.destructionCurve = 3
			shockWave.destructionRange = 10.0

			shockWave.speed = 2.5
			shockWave.duration = 2 * 20


			shockWave.shouldPlaySound = true
			shockWave.sounds = listOf(
				Triple("entity.warden.sonic_boom", 10f, 0.5f),
				Triple("item.totem.use", 10f, .5f)
			)
			shockWave.damageDone = true
			shockWave.damageFunction = Function { distance -> if(distance > 60.0){0.0}else if(distance>25.8){80.0/ log10(distance-20) }else 1000.0 }

			shockWave.shouldKnockBack = true
			shockWave.knockBackStrength = 3.0

			//burn wave
			burnWave.count = 10
			burnWave.size = 0.05
			burnWave.durationMin = 2 * 20
			burnWave.durationMax = 3 * 20
			burnWave.speedMin = .2 * 1.5
			burnWave.speedMax = .3 * 1.5
			burnWave.growth = .1

			//flash burn
			flashBurn.rayDistance = 50.0
			flashBurn.horizontalCount = 20
			flashBurn.verticalCount = 10

			//burner
			burner.disabled = true
		}
	}
}


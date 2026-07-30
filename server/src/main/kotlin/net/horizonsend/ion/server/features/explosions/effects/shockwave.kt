@file:Suppress("UnstableApiUsage")

package net.horizonsend.ion.server.features.explosions.effects

import com.google.common.base.Function
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.horizonsend.ion.server.features.explosions.AppState
import net.horizonsend.ion.server.features.explosions.utilities.BurnPalette
import net.horizonsend.ion.server.features.explosions.utilities.blockModel
import net.horizonsend.ion.server.features.explosions.utilities.centredTransform
import net.horizonsend.ion.server.features.explosions.utilities.lerp
import net.horizonsend.ion.server.features.explosions.utilities.raycastGround
import net.horizonsend.ion.server.features.explosions.utilities.ring
import net.horizonsend.ion.server.features.explosions.utilities.sphereBlockOffsets
import net.horizonsend.ion.server.miscellaneous.utils.DamageEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.util.Vector
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

class ShockWaveOptions {
    var count = 120


    var destructionRadius = 3.8
    var destructionCurve = 3
    var destructionRange = 20.0

    var flyingBlockChance = 0.1
    var flyingBlockMinVelocity = 2.7 * 1.2
    var flyingBlockMaxVelocity = 3.0 * 1.2

	var damageDone = false
	var damageFunction = Function { distance: Double -> 100/ log10(distance) }
	var maxDamageRange = 100.0

	var shouldKnockBack = false
	var knockBackStrength = 10.0

	var shouldPlaySound= false
	var sounds = listOf<Triple<String, Float, Float>>() //Sound to Volume and Pitch

    var size = .1
    var growth = .125
    var duration = 4 * 20
    var speed = 3.2

    var scanUpwards = 15.0

    var markDestroyedBlocks = false
}

class ShockWavePlacement(
    val world: World,
    val origin: Vector,
    val render: Vector
)

val shockWavePalette = listOf(Material.WHITE_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS).map { it.createBlockData() }

/**
 * Spawn shockwave
 *
 * @param placement
 * @param options
 * @param burner
 * @param entitythatExploded included if u want it to do damage
 */

fun spawnShockwave(placement: ShockWavePlacement, options: ShockWaveOptions, burner: Burner, entitythatExploded: Entity? = null) {
    val visitedPlayers = mutableListOf<Player>()

    for ((x,z) in ring(options.count)) Cloud(
        renderLocation = placement.render.toLocation(placement.world),
        location = placement.origin.toLocation(placement.world),
        velocity = Vector(x, .02, z).multiply(options.speed),
        size = options.size,
        growth = options.growth,
        maxAge = options.duration,
        blocks = listOf(shockWavePalette.random()),
    ).apply {
        onUpdate = onUpdate@{
            val radiusSquared = location.toVector().add(velocity).distanceSquared(placement.origin)
            for (player in placement.world.players) {
                if (player in visitedPlayers) continue
                if (player.location.toVector().distanceSquared(placement.origin) > radiusSquared) continue
                visitedPlayers += player

				if (options.shouldPlaySound){
					for(sound in options.sounds){
						player.playSound(player.location, sound.first, SoundCategory.PLAYERS, sound.second, sound.third)
					}
				}

				if (options.damageDone && player.location.distance(location) < options.maxDamageRange && entitythatExploded != null){
					val vector = location.toVector().subtract(player.location.toVector())
					if (player.world.rayTraceBlocks(player.location, vector, player.location.distance(location), FluidCollisionMode.SOURCE_ONLY)?.hitBlock == null) {
						DamageEvent.doDamageEvent(
							options.damageFunction.apply(location.distance(player.location)),
							EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
							player,
							entitythatExploded, DamageType.EXPLOSION, location, true, true
						)
					}
				}

				if (options.shouldKnockBack){
					val vector = location.toVector().subtract(player.location.toVector()).multiply(options.knockBackStrength)
					val event = EntityKnockbackEvent(player, EntityKnockbackEvent.Cause.EXPLOSION, vector)
					event.callEvent()
					if (!event.isCancelled){
						(event.entity as LivingEntity).knockback(vector.length(), vector.x, vector.z)
					}
				}
            }

            val top = location.clone().add(.0,options.scanUpwards,.0)
            val ground = raycastGround(top, Vector(0, -1, 0), options.scanUpwards)?.hitPosition?.toLocation(location.world!!) ?: location

            if (location.block.type.isOccluding) {
                location.y = location.y.lerp(ground.y + size / 2, .1)
            }

            val radiusFraction = (radiusSquared / options.destructionRange.pow(2))

            if (radiusFraction > 1) return@onUpdate

            val removeRadius = ((1 - radiusFraction).pow(options.destructionCurve) * options.destructionRadius).roundToInt()
                .coerceAtLeast(if (options.destructionRadius == .0) 0 else 1) // remove at least one block, unless radius is 0

            if (removeRadius == 0) return@onUpdate

            for (offset in sphereBlockOffsets(removeRadius)) {
                val block = ground.block.getRelative(offset.x, offset.y, offset.z)

                if (options.markDestroyedBlocks) {
                    AppState.renderer.render(block, blockModel( // "shockwave" to x to z to offset
                        location = block.location.add(Vector(.5, .5, .5)),
                        init = {
                            it.block = Material.RED_CONCRETE.createBlockData()
                            it.transformation = centredTransform(1.0f, 1.0f, 1.0f)
//                            it.teleportDuration = 1
                        }
                    ))
                }

                if (block.type.isAir) continue
                if (block.isLiquid) continue

                if (Random.nextFloat() < options.flyingBlockChance) {
                    spawnFlyingBlock(block, velocity, radiusFraction, renderLocation, burner.options.palette.burn, options)
                }

                removeBlock(block)
            }
        }
    }
}

private fun removeBlock(block: Block) {
    if (hasWater(block)) {
        //setBlock(block, Material.WATER)
    } else {
        //setBlock(block, Material.AIR)
    }
}

private fun spawnFlyingBlock(block: Block, shockWaveVelocity: Vector, radiusFraction: Double, renderLocation: Location, burnPalette: BurnPalette, options: ShockWaveOptions) {
    val palette = burnPalette.burn(block, heat = 1.0f) ?: listOf(0L to block.blockData)
    val blockData = block.blockData

    val velocity = shockWaveVelocity.clone()
    val power = 1.0 - radiusFraction

    velocity.y = velocity.length() * power
    velocity.normalize()
    velocity.multiply(Random.nextDouble(options.flyingBlockMinVelocity, options.flyingBlockMaxVelocity) * power.pow(2))

    Tasks.asyncDelay(Random.nextLong(5)) {
        val flying = FlyingBlock(
            renderLocation = renderLocation,
            location = block.location,
            velocity = velocity,
            blockData = blockData,
        )

        for ((delay, data) in palette) {
			Tasks.asyncDelay(delay){
				flying.blockData = data
			}
        }
    }
}

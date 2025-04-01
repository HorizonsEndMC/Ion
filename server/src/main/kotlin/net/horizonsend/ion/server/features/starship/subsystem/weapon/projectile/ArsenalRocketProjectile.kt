package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import com.mojang.math.Transformation
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.getAddEntityPacket
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockPos
import net.horizonsend.ion.server.miscellaneous.utils.gayColors
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class ArsenalRocketProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	direction: Vector,
	shooter: Damager,
	var face: BlockFace, //Up = true, down = false
	damageType: DamageType
) : SimpleProjectile<StarshipWeapons.ArsenalRocketBalancing.ArsenalRocketProjectileBalancing>(source, name, loc, direction, shooter, damageType), DisplayEntityProjectile {
	override var displayEntities: MutableMap<Player, ItemDisplay?> = mutableMapOf()
	override val originLocation: Location = loc
	private var age: Int = 0 //Ticks
	private var initialVelocity = this.direction
	private var hasSwitchedToNormaldirectionection = false
	override val scale: Vector3f = Vector3f(3f,3f,3f)
	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds
		/*Age is increased by 1 every tick. Between 10-15 ticks, the missile move straight up, to "escape" the silo and the ship
		after the set period of time, it switches to a normal directionection,
		normal directionection refers to it not moving straight up anymore
		 */
		age++
		if (age< randomInt(10,15) && !hasSwitchedToNormaldirectionection) {
			if (face == BlockFace.UP) direction = Vector(0,1,0).multiply(direction.length()) //move it up
			else direction = Vector(0,-1,0).multiply(direction.length()) //move it down
			val yFactor = when(face) {
				BlockFace.UP-> 1
				BlockFace.DOWN-> -1
				else -> 1
			}
			val predictedNewLoc = location.clone().add(0.0, delta * speed/2 * yFactor, 0.0)
			//We're not doing any impact stuff as we dont want the projectile to be able to hit stuff at this stage
			val travel = location.distance(predictedNewLoc)

			moveVisually(location, predictedNewLoc, travel)

			location = predictedNewLoc

			distance += travel

			if (distance >= range) {
				destroyAllDisplayEntities()
				return
			}

			lastTick = System.nanoTime()
			reschedule()
			return
		}
		else {
			if (!hasSwitchedToNormaldirectionection) {
				if (source is StarshipProjectileSource) {
					for (nearbyPlayer in source.starship.world.getNearbyPlayers(
						source.starship.centerOfMass.toLocation(source.starship.world),
						250.0
					)) {
						nearbyPlayer.playSound(
							Sound.sound(
								Key.key("horizonsend:starship.weapon.arsenal_missile.ignite"),
								Sound.Source.AMBIENT,
								5f,
								1.0f
							)
						)
					}
				}
				//1 is for up, -1 is for down
				if (face == BlockFace.UP) this.direction = initialVelocity.clone().setY(1).normalize()
				else this.direction = initialVelocity.clone().setY(-1).normalize()
			}
			hasSwitchedToNormaldirectionection = true
			val predictedNewLoc = location.clone().add(direction.clone().multiply(delta * speed))
			val result: RayTraceResult? = location.world.rayTrace(location, direction, delta * speed, FluidCollisionMode.NEVER, true, 0.1) { it.type != org.bukkit.entity.EntityType.ITEM_DISPLAY }
			val newLoc = result?.hitPosition?.toLocation(location.world) ?: predictedNewLoc
			val travel = location.distance(newLoc)

			moveVisually(location, newLoc, travel)

			var impacted = false

			if (result != null) {
				impacted = tryImpact(result, newLoc)
			}

			location = newLoc

			distance += travel

			if (impacted) {
				destroyAllDisplayEntities()
				return
			}

			if (distance >= range) {
				destroyAllDisplayEntities()
				return
			}

			lastTick = System.nanoTime()
			reschedule()
		}
		//lerp-to code
		//Desired vector is the vector straight to the target
		val desiredVector = (source as? StarshipProjectileSource)?.starship?.targetedPosition?.clone()?.subtract(location.clone())?.toVector() ?: return
		desiredVector.normalize()
		//this is the current directionection of the projectile, written as a Vector3f
		val directionAsVec3f = direction.clone().toVector3f().normalize()
		//0.05f is a tolerance of 5 for each lerp, so the direction will lerp only 5% every tick towards the desiredVector
		var tolerance = .1f
		if (age>35) tolerance = 0.25f
		if (age>55) tolerance = 0.01f
		directionAsVec3f.lerp(desiredVector.toVector3f(), tolerance).normalize()
		this.direction = Vector(directionAsVec3f.x, directionAsVec3f.y, directionAsVec3f.z)
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val color: Color = if ((source as? StarshipProjectileSource)?.starship?.rainbowToggle == true) gayColors.random() else Color.GRAY
		newLocation.world.spawnParticle(Particle.DUST, newLocation.x, newLocation.y, newLocation.z, 2,0.0,0.0,0.0, 0.0,DustOptions(color, 3f), true)
		newLocation.world.spawnParticle(Particle.SOUL_FIRE_FLAME,newLocation, 3)
		updateDisplayEntity(newLocation, direction.clone().normalize().multiply(speed))
	}

	override fun fire() {
		val starship = (source as? StarshipProjectileSource)?.starship
		if (starship == null) return
		if (starship.targetedPosition == null) {
			starship.audiences().forEach { it.userError("Error: Arsenal Missiles need a targeted position to fire, do /targetposition x y z to target a Position!") }
			return
		}
		initialVelocity = starship.targetedPosition?.clone()?.subtract(location.clone())?.toVector()?.normalize() ?: return
		makeDisplayEntities()
		super.fire()
	}

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		newLoc.world.spawnParticle(Particle.EXPLOSION, newLoc, 4)
		newLoc.world.spawnParticle(Particle.FLAME, newLoc, 10)
		newLoc.world.spawnParticle(Particle.FLASH, newLoc, 3)
		for (nearbyPlayer in newLoc.world.getNearbyPlayers(newLoc, 200.0)) {
			nearbyPlayer.playSound(Sound.sound(Key.key("horizonsend:starship.weapon.arsenal_missile.impact"), Sound.Source.AMBIENT, 5f, 1.0f))
		}
		super.impact(newLoc, block, entity)
	}

	override fun makeDisplayEntities() {
		for (playerBukkit in Bukkit.getServer().onlinePlayers.filter { it.world == this.location.world }) {
			val player = (playerBukkit as CraftPlayer)
			val connection = player.handle.connection
			val itemDisplay = ItemDisplay(EntityType.ITEM_DISPLAY, player.minecraft.level()).apply {
				this.itemStack = CraftItemStack.asNMSCopy(CustomItemKeys.ACTIVATED_ARSENAL_MISSILE.getValue().constructItemStack())
				setPos(player.location.toBlockPos().center)
				val translation = originLocation.toVector().subtract(Vector(this.x, this.y, this.z)).toVector3f()
				val transformation = Transformation(
					translation,
					Quaternionf(),
					Vector3f(3.0f, 3.0f, 3.0f),
					Quaternionf()
				) //Set the new transformation
				this.setTransformation(
					transformation
				)
				this.transformationInterpolationDuration = 0
				this.viewRange = 1000f
			}
			displayEntities[playerBukkit] = itemDisplay

			connection.send(getAddEntityPacket(itemDisplay))
			itemDisplay.refreshEntityData(player.handle)
		}
	}

	override fun makeDisplayEntity(player: Player): ItemDisplay {
		val nmsPlayer = (player as CraftPlayer)
		val connection = nmsPlayer.handle.connection
		val itemDisplay = ItemDisplay(EntityType.ITEM_DISPLAY, nmsPlayer.minecraft.level()).apply {
			this.itemStack = CraftItemStack.asNMSCopy(CustomItemKeys.ACTIVATED_ARSENAL_MISSILE.getValue().constructItemStack())
			setPos(nmsPlayer.location.toBlockPos().center)
			val translation = originLocation.toVector().subtract(Vector(this.x, this.y, this.z)).toVector3f()
			val transformation = Transformation(
				translation,
				Quaternionf(),
				scale,
				Quaternionf()
			) //Set the new transformation
			this.setTransformation(
				transformation
			)
			this.transformationInterpolationDuration = 0
			this.viewRange = 1000f
		}
		displayEntities[player] = itemDisplay

		connection.send(getAddEntityPacket(itemDisplay))
		itemDisplay.refreshEntityData(player.handle)
		return itemDisplay
	}
}


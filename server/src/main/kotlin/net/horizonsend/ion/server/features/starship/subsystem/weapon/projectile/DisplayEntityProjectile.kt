package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import com.mojang.math.Transformation
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.world.entity.Display
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f


interface DisplayEntityProjectile {
	var displayEntities: MutableMap<Player, Display.ItemDisplay?>

	val originLocation: Location //The position where the displayEntity was spawned, this is used in the translation maths

	var scale: Vector3f

	fun updateDisplayEntity(newLocation: Location, velocity: Vector) {
		for (player in Bukkit.getServer().onlinePlayers.filter { it.world == newLocation.world }) {
			//if the location of the projectile to the player exceeds 160, we remove it
			if (newLocation.distance(player.location) > Bukkit.getServer().viewDistance*16) {
				destroyDisplayEntity(player)
				continue
			}

			val displayEntity = displayEntities[player as CraftPlayer] ?: makeDisplayEntity(player) ?: continue

			if (player.location.distance(Vector(displayEntity.x, displayEntity.y, displayEntity.z).toLocation(player.world)) > 100.0) {
				displayEntity.setPos(player.x,player.y,player.z)
				val teleportEntityPacket = ClientboundTeleportEntityPacket(displayEntity)
				player.handle.connection.send(teleportEntityPacket)
			}

			//We multiply the velocity to pretend we're interpolating for 3 ticks, to make it smooth
			velocity.multiply(3)
			//Get the translation needed from the originalLocation.?.clone()
			val translation =
				newLocation.toVector().subtract(Vector(displayEntity.x, displayEntity.y, displayEntity.z))
					.toVector3f()
			val invertedVector = velocity.clone().multiply(-1).toVector3f().normalize()
			val transformation = Transformation(
				translation,
				ClientDisplayEntities.rotateToFaceVector(invertedVector),
				scale,
				Quaternionf()
			) //Set the new transformation

			displayEntity.setTransformation(transformation)
			displayEntity.transformationInterpolationDuration = 3
			val packet = ClientboundSetEntityDataPacket(
				displayEntity.id,
				displayEntity.entityData.packDirty()!!
			)

			player.handle.connection.send(packet)
			displayEntity.entityData.refresh(player.handle)
		}
	}

	fun destroyAllDisplayEntities() {
		for ((player, displayEntity) in displayEntities) {
			val packet = ClientboundRemoveEntitiesPacket(displayEntity?.id ?: return)
			(player as CraftPlayer).handle.connection.send(packet)
		}
	}

	fun destroyDisplayEntity(player: Player) {
		val displayEntity = displayEntities[player]
		val packet = ClientboundRemoveEntitiesPacket(displayEntity?.id ?: return)
		displayEntities.remove(player)
		(player as CraftPlayer).handle.connection.send(packet)
	}

	fun makeDisplayEntities() {}

	fun makeDisplayEntity(player: Player): Display.ItemDisplay? { return null}

}

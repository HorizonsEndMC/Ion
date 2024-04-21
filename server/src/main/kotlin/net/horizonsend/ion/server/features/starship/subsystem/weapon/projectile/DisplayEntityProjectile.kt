package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import com.mojang.math.Transformation
import io.netty.buffer.Unpooled
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.minecraft.network.FriendlyByteBuf
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

	var oldLocation: Location

	var scale: Vector3f

	fun updateDisplayEntity(newLocation: Location, velocity: Vector) {
		for (player in Bukkit.getServer().onlinePlayers.filter { it.world == newLocation.world }) {
			val displayEntity = displayEntities[player as CraftPlayer] ?: continue
			//if (player.location.distance(Location(newLocation.world ,displayEntity.x, displayEntity.y, displayEntity.y)) > 100.0){
			//	val buf = FriendlyByteBuf(Unpooled.buffer())
			//	buf.writeVarInt(displayEntity.id)
			//	buf.writeDouble(newLocation.x)
			//	buf.writeDouble(newLocation.y)
			//	buf.writeDouble(newLocation.z)
			//	buf.writeBoolean(false)
			//	val teleportEntityPacket = ClientboundTeleportEntityPacket(buf)
			//	player.handle.connection.send(teleportEntityPacket)
			//	displayEntity.entityData.refresh(player.handle)
			//}

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

	fun destroyDisplayEntity() {
		for ((player, displayEntity) in displayEntities) {
			val packet = ClientboundRemoveEntitiesPacket(displayEntity?.id ?: return)
			(player as CraftPlayer).handle.connection.send(packet)
		}
	}

	fun makeDisplayEntities() {}
}

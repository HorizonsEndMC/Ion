package net.starlegacy.util

import java.lang.reflect.Field
import net.minecraft.server.v1_16_R3.EntityPlayer
import net.minecraft.server.v1_16_R3.PacketPlayOutPosition
import net.minecraft.server.v1_16_R3.PacketPlayOutPosition.EnumPlayerTeleportFlags.X
import net.minecraft.server.v1_16_R3.PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT
import net.minecraft.server.v1_16_R3.PacketPlayOutPosition.EnumPlayerTeleportFlags.Y
import net.minecraft.server.v1_16_R3.PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT
import net.minecraft.server.v1_16_R3.PacketPlayOutPosition.EnumPlayerTeleportFlags.Z
import net.minecraft.server.v1_16_R3.PlayerConnection
import net.minecraft.server.v1_16_R3.Vec3D
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object ConnectionUtils {
	private val OFFSET_DIRECTION = setOf(X_ROT, Y_ROT)
	private val OFFSET_ALL = setOf(X_ROT, Y_ROT, X, Y, Z)

	private var justTeleportedField: Field = getField("justTeleported")
	private var teleportPosField: Field = getField("teleportPos")
	private var lastPosXField: Field = getField("lastPosX")
	private var lastPosYField: Field = getField("lastPosY")
	private var lastPosZField: Field = getField("lastPosZ")
	private var teleportAwaitField: Field = getField("teleportAwait")
	private var AField: Field = getField("A")
	private var eField: Field = getField("e")

	@Throws(NoSuchFieldException::class)
	private fun getField(name: String): Field {
		val field = PlayerConnection::class.java.getDeclaredField(name)
		field.isAccessible = true
		return field
	}

	fun move(player: Player, loc: Location, theta: Float = 0.0f, offsetPos: Vector? = null) {
		val handle = (player as CraftPlayer).handle
		val connection = handle.playerConnection
		val x = loc.x
		val y = loc.y
		val z = loc.z

		if (handle.activeContainer !== handle.defaultContainer) {
			handle.closeInventory()
		}

		var teleportAwait: Int
		justTeleportedField.set(connection, true)
		teleportPosField.set(connection, Vec3D(x, y, z))
		lastPosXField.set(connection, x)
		lastPosYField.set(connection, y)
		lastPosZField.set(connection, z)
		teleportAwait = teleportAwaitField.getInt(connection).plus(1)

		if (teleportAwait == 2147483647) {
			teleportAwait = 0
		}

		teleportAwaitField.set(connection, teleportAwait)
		AField.set(connection, eField.get(connection))

		val px: Double
		val py: Double
		val pz: Double

		if (offsetPos == null) {
			px = x
			py = y
			pz = z
		} else {
			px = offsetPos.x
			py = offsetPos.y
			pz = offsetPos.z
		}

		handle.setLocation(x, y, z, handle.yaw + theta, handle.pitch)
		handle.worldServer.chunkCheck(handle)

		val flags = if (offsetPos != null) OFFSET_ALL else OFFSET_DIRECTION
		val packet = PacketPlayOutPosition(px, py, pz, theta, 0f, flags, teleportAwait)
		connection.sendPacket(packet)
	}

	fun teleport(player: Player, loc: Location) {
		move(player, loc, 0.0f, null)
	}

	fun teleportRotate(player: Player, loc: Location, theta: Float) {
		move(player, loc, theta, null)
	}

	fun move(player: Player, loc: Location, dx: Double, dy: Double, dz: Double) {
		move(player, loc, 0.0f, Vector(dx, dy, dz))
	}

	fun isTeleporting(player: EntityPlayer?): Boolean {
		if (player == null) return false
		return try {
			teleportPosField.get(player.playerConnection) != null
		} catch (e: IllegalAccessException) {
			false
		}
	}

	fun unfreeze(player: Player?) {
		if (player == null || !player.isOnline) return
		if (Bukkit.isPrimaryThread()) move(player, player.location, 0.0, 0.0, 0.0)
		val handle = (player as CraftPlayer).handle
		while (player.isOnline && teleportPosField.get(handle.playerConnection) != null) Thread.sleep(0)
	}
}

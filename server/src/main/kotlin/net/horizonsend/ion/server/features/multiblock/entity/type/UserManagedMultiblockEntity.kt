package net.horizonsend.ion.server.features.multiblock.entity.type

import com.manya.pdc.base.UuidDataType
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Bukkit.getPlayer
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.UUID

interface UserManagedMultiblockEntity {
	val userManager: UserManager

	/**
	 * Holds information about who is currently using this multiblock.
	 **/
	class UserManager(data: PersistentMultiblockData, private val persistent: Boolean) {
		private var user: UUID? = data.getAdditionalData(NamespacedKeys.DRILL_USER, UuidDataType())
		private var userName: String? = data.getAdditionalData(NamespacedKeys.DRILL_USER_NAME, STRING)

		fun clear() {
			user = null
			userName = null
		}

		fun setUser(player: Player) {
			user = player.uniqueId
			userName = player.name
		}

		/**
		 *
		 **/
		fun currentlyUsed() = (user != null)

		/**
		 * Gets the player of the user, if there is one
		 **/
		fun getUserPlayer(): Player? = user?.let(::getPlayer)

		fun saveUserData(store: PersistentMultiblockData) {
			if (!persistent) return
			user?.let { store.addAdditionalData(NamespacedKeys.DRILL_USER, UuidDataType(), it) }
			userName?.let { store.addAdditionalData(NamespacedKeys.DRILL_USER_NAME, STRING, it) }
		}
	}
}

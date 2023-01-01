package net.starlegacy.command.nations.roles

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.starlegacy.command.SLCommand
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.Role
import net.starlegacy.database.schema.nations.RoleCompanion
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.uuid
import net.starlegacy.feature.nations.gui.editRoleGUI
import net.starlegacy.feature.nations.gui.editRolePermissionGUI
import net.starlegacy.feature.nations.gui.guiButton
import net.starlegacy.feature.nations.gui.lore
import net.starlegacy.feature.nations.gui.manageRolesGUI
import net.starlegacy.feature.nations.gui.memberRoleGUI
import net.starlegacy.feature.nations.gui.membersRoleGUI
import net.starlegacy.feature.nations.gui.name
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.gui.skullItem
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Tasks
import net.starlegacy.util.isAlphanumeric
import net.starlegacy.util.msg
import org.bukkit.Material
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.pull
import java.sql.Timestamp
import java.util.UUID

/** Abstract role command logic class, has the logic but not the command description/tab completions due to ACF's restrictions */
internal abstract class RoleCommand<Parent : DbObject, Permission : Enum<Permission>, T : Role<Parent, Permission>> :
	SLCommand() {
	protected abstract val roleCompanion: RoleCompanion<Parent, Permission, T>

	protected abstract val manageRolesPermission: Permission

	protected abstract val allPermissions: Array<Permission>

	protected abstract fun requireParent(sender: Player): Oid<Parent>

	protected abstract fun requirePermission(sender: Player, parent: Oid<Parent>, permission: Permission)

	protected abstract fun isLeader(slPlayerId: SLPlayerId, parent: Oid<Parent>): Boolean

	protected abstract fun isMember(slPlayerId: SLPlayerId, parent: Oid<Parent>): Boolean

	protected abstract fun getMembers(parent: Oid<Parent>): List<SLPlayerId>

	private fun requireManageableRole(sender: Player, parent: Oid<Parent>, name: String): T {
		val role = roleCompanion.findOne(and(roleCompanion.parentProperty eq parent, roleCompanion.nameQuery(name)))
			?: fail { "Role $name not found!" }

		val senderWeight = getWeight(sender.slPlayerId, parent)
		failIf(role.weight >= senderWeight && senderWeight != 1001 /* since 1001 would mean they're the leader */) {
			"You can't manage a role with a weight equal to or greater than your weight! " +
				"(Weight of {${role.name} is ${role.weight}, your weight is $senderWeight})"
		}

		return role
	}

	private fun requireManageablePlayer(sender: Player, parent: Oid<Parent>, playerName: String): SLPlayerId {
		val id: UUID = resolveOfflinePlayer(playerName)
		val slPlayerId = id.slPlayerId

		failIf(!isMember(slPlayerId, parent)) { "$playerName is not a member" }

		requireCanManage(sender, slPlayerId, parent)

		return slPlayerId
	}

	private fun getWeight(slPlayerId: SLPlayerId, parent: Oid<Parent>): Int = when {
		isLeader(slPlayerId, parent) -> 1001
		else -> roleCompanion.getHighestRole(slPlayerId)?.weight ?: -1
	}

	private fun validateColor(color: SLTextStyle): Unit = when (color) {
		SLTextStyle.OBFUSCATED -> fail { "${color.name} is not allowed to be used as a role color" }
		else -> run { }
	}

	private fun validateWeight(sender: Player, weight: Int, parent: Oid<Parent>) {
		val playerWeight = getWeight(sender.slPlayerId, parent)
		failIf(weight >= playerWeight) { "You can't sent the weight of a role to greater than or equal to your own weight ($playerWeight)!" }

		failIf(weight !in 0..1000) { "Weight must be between 0 and 1000 (higher = more authority, leader is always 1001)" }
	}

	private fun validateName(parent: Oid<Parent>, name: String) {
		failIf(!name.isAlphanumeric()) { "Name must only use numbers and letters" }

		failIf(name.length !in 3..20) { "Name must be between 3 and 20 characters" }

		failIf(!roleCompanion.none(and(roleCompanion.parentProperty eq parent, roleCompanion.nameQuery(name)))) { "A role with that name already exists" }
	}

	private fun requireCanManage(sender: Player, other: SLPlayerId, parent: Oid<Parent>) {
		if (isLeader(sender.slPlayerId, parent)) {
			return
		}
		failIf(getWeight(sender.slPlayerId, parent) <= getWeight(other, parent)) { "${SLPlayer.getName(other)}'s weight is higher than yours, so you can't manage them." }
	}

	private fun requireManageableParent(sender: Player): Oid<Parent> {
		val parent = requireParent(sender)
		requirePermission(sender, parent, manageRolesPermission)
		return parent
	}

	@Suppress("UNCHECKED_CAST")
	private fun getId(role: T) = role._id as Oid<T>

	open fun onManage(sender: Player) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)

		val roleItems = roleCompanion.find(roleCompanion.parentProperty eq parent)
			.sortedByDescending { it.weight }
			.map { role: T ->
				val roleName = role.name // store here to avoid keeping role in memory

				guiButton(Material.PAPER) {
					playerClicker.performCommand("$name edit $roleName")
				}.name(role.coloredName).lore(
					"Weight: ${role.weight}",
					"Color: ${role.color.name}",
					"Members: ${role.members.size}",
					"Permissions:",
					role.permissions.joinToString("\n")
				)
			}

		Tasks.sync {
			sender.manageRolesGUI(name, roleItems)
		}
	}

	open fun onCreate(sender: Player, name: String, color: SLTextStyle, weight: Int) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)

		failIf(roleCompanion.count(roleCompanion.parentProperty eq parent) >= 27) { "You can only make 27 roles." }

		validateName(parent, name)
		validateColor(color)
		validateWeight(sender, weight, parent)

		roleCompanion.create(parent, name, color, weight)

		sender msg "&3Created role $name"
	}

	open fun onEdit(sender: Player, role: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		Tasks.sync {
			editRoleGUI(sender, this.name, roleData.name, roleData.color, roleData.weight)
		}
	}

	open fun onPermissionGUI(sender: Player, role: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		Tasks.sync {
			editRolePermissionGUI(sender, name, roleData.name, roleData.color, roleData.permissions, allPermissions)
		}
	}

	open fun onPermissionAdd(sender: Player, role: String, permission: Permission) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		failIf(roleData.permissions.contains(permission)) { "Role ${roleData.name} already has permission $permission!" }

		roleCompanion.updateById(getId(roleData), addToSet(roleCompanion.permissionsProperty, permission))

		sender msg "&aAdded permission $permission to role ${roleData.name}"
	}

	open fun onPermissionList(sender: Player, role: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)
		val permissions = roleData.permissions

		failIf(permissions.isEmpty()) { "${roleData.name} has no permissions. To add permissions, use /$name permission add <permission>." }

		sender msg "&7{${permissions.joinToString()}}"
	}

	open fun onPermissionRemove(sender: Player, role: String, permission: Permission) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		failIf(!roleData.permissions.contains(permission)) { "${roleData.name} doesn't have the $permission permission!" }

		roleCompanion.updateById(getId(roleData), pull(roleCompanion.permissionsProperty, permission))

		sender msg "&aRemoved permission $permission from role ${roleData.name}"
	}

	open fun onEditName(sender: Player, role: String, newName: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		validateName(parent, newName)

		roleCompanion.updateById(getId(roleData), org.litote.kmongo.setValue(roleCompanion.nameProperty, newName))

		sender msg "&aRenamed ${roleData.name} to $newName"
	}

	open fun onEditColor(sender: Player, role: String, newColor: SLTextStyle) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		validateColor(newColor)

		roleCompanion.updateById(getId(roleData), org.litote.kmongo.setValue(roleCompanion.colorProperty, newColor))

		sender msg "&aChanged color of ${roleData.name} from ${roleData.color.name} to ${newColor.name}"
	}

	open fun onEditWeight(sender: Player, role: String, newWeight: Int) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		validateWeight(sender, newWeight, parent)

		roleCompanion.updateById(getId(roleData), org.litote.kmongo.setValue(roleCompanion.weightProperty, newWeight))

		sender msg "&aChanged weight of ${roleData.name} from ${roleData.weight} to $newWeight"
	}

	open fun onDelete(sender: Player, role: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val roleData: T = requireManageableRole(sender, parent, role)

		roleCompanion.delete(getId(roleData))

		sender msg "&aDeleted role ${roleData.name}"
	}

	open fun onMembersGUI(sender: Player) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)

		val members = getMembers(parent)

		// this is a long running operation due to skin retrieval, do it on a new async task
		Tasks.async {
			val weights: Map<SLPlayerId, Int> = members.associateWith { getWeight(it, parent) }

			val memberItems: List<GuiItem> = members.mapNotNull { SLPlayer[it] }
				.sortedBy { it.lastKnownName }
				.sortedByDescending { weights[it._id] }
				.parallelStream()
				.map { player: SLPlayer ->
					guiButton(skullItem(player._id.uuid, player.lastKnownName)) {
						playerClicker.performCommand("$name member gui ${player.lastKnownName}")
					}.lore(
						"Last Seen: ${Timestamp(player.lastSeen.time).toLocalDateTime()}",
						"Weight: ${weights[player._id]}"
					)
				}.toList()

			Tasks.sync { membersRoleGUI(sender, name, memberItems) }
		}
	}

	open fun onMemberGUI(sender: Player, player: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val playerId: SLPlayerId = requireManageablePlayer(sender, parent, player)

		val playerRoles: Set<String> = roleCompanion.getRoles(playerId)
			.mapNotNull { roleCompanion.findPropById(it, roleCompanion.nameProperty) }.toSet()

		val allRoles = roleCompanion.find(roleCompanion.parentProperty eq parent)
			.sortedByDescending { it.weight }
			.map { it.name }

		Tasks.sync {
			sender.memberRoleGUI(name, player, playerRoles, allRoles)
		}
	}

	open fun onMemberAdd(sender: Player, player: String, role: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val playerId: SLPlayerId = requireManageablePlayer(sender, parent, player)
		val roleData: T = requireManageableRole(sender, parent, role)

		failIf(roleCompanion.getRoles(playerId).contains(getId(roleData))) { "$player already has role ${roleData.name}." }

		roleCompanion.updateById(getId(roleData), addToSet(roleCompanion.membersProperty, playerId))

		sender msg "&aGave role ${roleData.coloredName} to $player"
	}

	open fun onMemberRemove(sender: Player, player: String, role: String) = asyncCommand(sender) {
		val parent: Oid<Parent> = requireManageableParent(sender)
		val playerId: SLPlayerId = requireManageablePlayer(sender, parent, player)
		val roleData: T = requireManageableRole(sender, parent, role)

		val result = roleCompanion.updateById(getId(roleData), pull(roleCompanion.membersProperty, playerId))

		failIf(result.matchedCount <= 0) { "$player doesn't have role ${roleData.name}." }

		sender msg "&aTook role ${roleData.coloredName} from $player"
	}
}

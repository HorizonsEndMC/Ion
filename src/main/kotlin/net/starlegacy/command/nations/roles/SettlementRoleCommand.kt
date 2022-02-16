package net.starlegacy.command.nations.roles

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementRole
import net.starlegacy.util.SLTextStyle
import org.bukkit.entity.Player

@CommandAlias("settlementrole|srole")
internal object SettlementRoleCommand : RoleCommand<Settlement, SettlementRole.Permission, SettlementRole>() {
	override val allPermissions = SettlementRole.Permission.values()

	override val roleCompanion = SettlementRole.Companion

	override val manageRolesPermission = SettlementRole.Permission.MANAGE_ROLES

	override fun requireParent(sender: Player): Oid<Settlement> {
		return requireSettlementIn(sender)
	}

	override fun requirePermission(sender: Player, parent: Oid<Settlement>, permission: SettlementRole.Permission) {
		requireSettlementPermission(sender, parent, permission)
	}

	override fun isLeader(slPlayerId: SLPlayerId, parent: Oid<Settlement>): Boolean {
		return SettlementCache[parent].leader == slPlayerId
	}

	override fun isMember(slPlayerId: SLPlayerId, parent: Oid<Settlement>): Boolean {
		return SLPlayer.isMemberOfSettlement(slPlayerId, parent)
	}

	override fun getMembers(parent: Oid<Settlement>): List<SLPlayerId> {
		return Settlement.getMembers(parent).toList()
	}

	@Subcommand("manage")
	@Description("GUI Role Manager")
	override fun onManage(sender: Player) {
		super.onManage(sender)
	}

	@Subcommand("create")
	@CommandCompletion("@nothing @chatcolors @range:1000")
	@Description("Create a role")
	override fun onCreate(sender: Player, name: String, color: SLTextStyle, weight: Int) {
		super.onCreate(sender, name, color, weight)
	}

	@Subcommand("edit")
	@CommandCompletion("@roles")
	@Description("Open role's edit menu")
	override fun onEdit(sender: Player, role: String) {
		super.onEdit(sender, role)
	}

	@Subcommand("permission gui")
	@CommandCompletion("@roles")
	@Description("GUI Role Permission Manager")
	override fun onPermissionGUI(sender: Player, role: String) {
		super.onPermissionGUI(sender, role)
	}

	@Subcommand("permission add")
	@CommandCompletion("@roles @permissions")
	@Description("Give a role a permission")
	override fun onPermissionAdd(sender: Player, role: String, permission: SettlementRole.Permission) {
		super.onPermissionAdd(sender, role, permission)
	}

	@Subcommand("permission list")
	@CommandCompletion("@roles")
	@Description("List a role's permissions")
	override fun onPermissionList(sender: Player, role: String) {
		super.onPermissionList(sender, role)
	}

	@Subcommand("permission remove")
	@CommandCompletion("@roles @permissions")
	@Description("Take a role's permission")
	override fun onPermissionRemove(sender: Player, role: String, permission: SettlementRole.Permission) {
		super.onPermissionRemove(sender, role, permission)
	}

	@Subcommand("edit name")
	@CommandCompletion("@roles @nothing")
	@Description("Edit a role's name")
	override fun onEditName(sender: Player, role: String, newName: String) {
		super.onEditName(sender, role, newName)
	}

	@Subcommand("edit color")
	@CommandCompletion("@roles @chatcolors")
	@Description("Edit a role's newColor")
	override fun onEditColor(sender: Player, role: String, newColor: SLTextStyle) {
		super.onEditColor(sender, role, newColor)
	}

	@Subcommand("edit weight")
	@CommandCompletion("@roles @range:1000")
	@Description("Edit a role's weight")
	override fun onEditWeight(sender: Player, role: String, newWeight: Int) {
		super.onEditWeight(sender, role, newWeight)
	}

	@Subcommand("delete")
	@CommandCompletion("@roles")
	@Description("Delete a role")
	override fun onDelete(sender: Player, role: String) {
		super.onDelete(sender, role)
	}

	@Subcommand("members")
	@Description("Members role GUI editor")
	override fun onMembersGUI(sender: Player) {
		super.onMembersGUI(sender)
	}

	@Subcommand("member gui")
	@CommandCompletion("@members")
	@Description("Member role list GUI editor")
	override fun onMemberGUI(sender: Player, player: String) {
		super.onMemberGUI(sender, player)
	}

	@Subcommand("member add")
	@CommandCompletion("@members @roles")
	@Description("Give a role to a member")
	override fun onMemberAdd(sender: Player, player: String, role: String) {
		super.onMemberAdd(sender, player, role)
	}

	@Subcommand("member remove")
	@CommandCompletion("@members @roles")
	@Description("Take a role from a member")
	override fun onMemberRemove(sender: Player, player: String, role: String) {
		super.onMemberRemove(sender, player, role)
	}
}

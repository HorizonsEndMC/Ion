package net.horizonsend.ion.server.command.nations.roles

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNationRole
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.eq

@CommandAlias("frontiernationrole|fnrole")
internal object FrontierNationRoleCommand : RoleCommand<FrontierNation, FrontierNationRole.Permission, FrontierNationRole>() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("frontierNationMembers") {
			val player = it.player
			val cached = PlayerCache[player]
			val nation = cached.frontierNationOid ?: return@registerAsyncCompletion listOf()

			SLPlayer.findProp(SLPlayer::frontierNation eq nation, SLPlayer::lastKnownName).toList()
		}

		manager.commandCompletions.registerAsyncCompletion("onlineFrontierNationMembers") {
			val player = it.player
			val cached = PlayerCache[player]
			val nation = cached.frontierNationOid ?: return@registerAsyncCompletion listOf()

			Bukkit.getOnlinePlayers().filter { otherPlayer -> PlayerCache[otherPlayer].frontierNationOid == nation }.map { otherPlayer -> otherPlayer.name }
		}

		manager.commandCompletions.registerAsyncCompletion("frontierNationRoles") {
			val player = it.player
			val cached = PlayerCache[player]
			val nation = cached.frontierNationOid ?: return@registerAsyncCompletion listOf()

			FrontierNationRole.findProp(FrontierNationRole::parent eq nation, FrontierNationRole::name).toList()
		}

		manager.commandCompletions.registerAsyncCompletion("frontierNationPermissions") {
			FrontierNationRole.Permission.entries.map { it.toString() }
		}
	}

	override val allPermissions = FrontierNationRole.Permission.entries.toTypedArray()

	override val roleCompanion = FrontierNationRole.Companion

	override val manageRolesPermission = FrontierNationRole.Permission.MANAGE_ROLES

	override fun requireParent(sender: Player): Oid<FrontierNation> {
		return requireFrontierNationIn(sender)
	}

	override fun requirePermission(sender: Player, parent: Oid<FrontierNation>, permission: FrontierNationRole.Permission) {
		requireFrontierNationPermission(sender, parent, permission)
	}

	override fun isLeader(slPlayerId: SLPlayerId, parent: Oid<FrontierNation>): Boolean {
		return FrontierNationCache[parent].leader == slPlayerId
	}

	override fun isMember(slPlayerId: SLPlayerId, parent: Oid<FrontierNation>): Boolean {
		return SLPlayer.isMemberOfFrontierNation(slPlayerId, parent)
	}

	override fun getMembers(parent: Oid<FrontierNation>): List<SLPlayerId> {
		return FrontierNation.getMembers(parent).toList()
	}

	@Subcommand("manage")
	@Description("GUI Role Manager")
	override fun onManage(sender: Player) {
		super.onManage(sender)
	}

	@Subcommand("create")
	@CommandCompletion("@nothing @chatcolors @range:1000 @nothing")
	@Description("Create a role")
	override fun onCreate(sender: Player, name: String, color: SLTextStyle, weight: Int) {
		super.onCreate(sender, name, color, weight)
	}

	@Subcommand("edit")
	@CommandCompletion("@frontierNationRoles @nothing")
	@Description("Open role's edit menu")
	override fun onEdit(sender: Player, role: String) {
		super.onEdit(sender, role)
	}

	@Subcommand("permission gui")
	@CommandCompletion("@frontierNationRoles @nothing")
	@Description("GUI Role Permission Manager")
	override fun onPermissionGUI(sender: Player, role: String) {
		super.onPermissionGUI(sender, role)
	}

	@Subcommand("permission add")
	@CommandCompletion("frontierNationRoles @frontierNationPermissions @nothing")
	@Description("Give a role a permission")
	override fun onPermissionAdd(sender: Player, role: String, permission: FrontierNationRole.Permission) {
		super.onPermissionAdd(sender, role, permission)
	}

	@Subcommand("permission list")
	@CommandCompletion("@frontierNationRoles @nothing")
	@Description("List a role's permission")
	override fun onPermissionList(sender: Player, role: String) {
		super.onPermissionList(sender, role)
	}

	@Subcommand("permission remove")
	@CommandCompletion("@frontierNationRoles @frontierNationPermissions @nothing")
	@Description("Take a role's permission")
	override fun onPermissionRemove(sender: Player, role: String, permission: FrontierNationRole.Permission) {
		super.onPermissionRemove(sender, role, permission)
	}

	@Subcommand("edit name")
	@CommandCompletion("@frontierNationRoles @nothing")
	@Description("Edit a role's name")
	override fun onEditName(sender: Player, role: String, newName: String) {
		super.onEditName(sender, role, newName)
	}

	@Subcommand("edit color")
	@CommandCompletion("@frontierNationRoles @nothing")
	@Description("Edit a role's newColor")
	override fun onEditColor(sender: Player, role: String, newColor: SLTextStyle) {
		super.onEditColor(sender, role, newColor)
	}

	@Subcommand("edit weight")
	@CommandCompletion("@frontierNationRoles @range:1000 @nothing")
	@Description("Edit a role's weight")
	override fun onEditWeight(sender: Player, role: String, newWeight: Int) {
		super.onEditWeight(sender, role, newWeight)
	}

	@Subcommand("delete")
	@CommandCompletion("@frontierNationRoles @nothing")
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
	@CommandCompletion("@frontierNationMembers @nothing")
	@Description("Member role list GUI editor")
	override fun onMemberGUI(sender: Player, player: String) {
		super.onMemberGUI(sender, player)
	}

	@Subcommand("member add")
	@CommandCompletion("@frontierNationMembers @frontierNationRoles @nothing")
	@Description("Give a role to a member")
	override fun onMemberAdd(sender: Player, player: String, role: String) {
		super.onMemberAdd(sender, player, role)
	}

	@Subcommand("member remove")
	@CommandCompletion("@frontierNationMembers @frontierNationRoles @nothing")
	@Description("Take a role from a member")
	override fun onMemberRemove(sender: Player, player: String, role: String) {
		super.onMemberRemove(sender, player, role)
	}
}

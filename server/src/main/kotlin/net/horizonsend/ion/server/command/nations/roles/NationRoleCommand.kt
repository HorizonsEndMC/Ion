package net.horizonsend.ion.server.command.nations.roles

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import org.bukkit.entity.Player
import org.litote.kmongo.eq

@CommandAlias("nationrole|nrole")
internal object NationRoleCommand : RoleCommand<Nation, NationRole.Permission, NationRole>() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("nationMembers") {
			val player = it.player
			val cached = PlayerCache[player]
			val nation = cached.nationOid ?: return@registerAsyncCompletion listOf()

			SLPlayer.findProp(SLPlayer::nation eq nation, SLPlayer::lastKnownName).toList()
		}

		manager.commandCompletions.registerAsyncCompletion("nationRoles") {
			val player = it.player
			val cached = PlayerCache[player]
			val nation = cached.nationOid ?: return@registerAsyncCompletion listOf()

			NationRole.findProp(NationRole::parent eq nation, NationRole::name).toList()
		}

		manager.commandCompletions.registerAsyncCompletion("nationPermissions") {
			NationRole.Permission.values().map { it.toString() }
		}
	}

	override val allPermissions = NationRole.Permission.values()

	override val roleCompanion = NationRole.Companion

	override val manageRolesPermission = NationRole.Permission.MANAGE_ROLES

	override fun requireParent(sender: Player): Oid<Nation> {
		return requireNationIn(sender)
	}

	override fun requirePermission(sender: Player, parent: Oid<Nation>, permission: NationRole.Permission) {
		requireNationPermission(sender, parent, permission)
	}

	override fun isLeader(slPlayerId: SLPlayerId, parent: Oid<Nation>): Boolean {
		return SettlementCache[NationCache[parent].capital].leader == slPlayerId
	}

	override fun isMember(slPlayerId: SLPlayerId, parent: Oid<Nation>): Boolean {
		return SLPlayer.isMemberOfNation(slPlayerId, parent)
	}

	public override fun getMembers(parent: Oid<Nation>): List<SLPlayerId> {
		return Nation.getMembers(parent).toList()
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
	@CommandCompletion("@nationRoles @nothing")
	@Description("Open role's edit menu")
	override fun onEdit(sender: Player, role: String) {
		super.onEdit(sender, role)
	}

	@Subcommand("permission gui")
	@CommandCompletion("@nationRoles @nothing")
	@Description("GUI Role Permission Manager")
	override fun onPermissionGUI(sender: Player, role: String) {
		super.onPermissionGUI(sender, role)
	}

	@Subcommand("permission add")
	@CommandCompletion("@nationRoles @nationPermissions @nothing")
	@Description("Give a role a permission")
	override fun onPermissionAdd(sender: Player, role: String, permission: NationRole.Permission) {
		super.onPermissionAdd(sender, role, permission)
	}

	@Subcommand("permission list")
	@CommandCompletion("@nationRoles @nothing")
	@Description("List a role's permissions")
	override fun onPermissionList(sender: Player, role: String) {
		super.onPermissionList(sender, role)
	}

	@Subcommand("permission remove")
	@CommandCompletion("@nationRoles @nationPermissions @nothing")
	@Description("Take a role's permission")
	override fun onPermissionRemove(sender: Player, role: String, permission: NationRole.Permission) {
		super.onPermissionRemove(sender, role, permission)
	}

	@Subcommand("edit name")
	@CommandCompletion("@nationRoles @nothing")
	@Description("Edit a role's name")
	override fun onEditName(sender: Player, role: String, newName: String) {
		super.onEditName(sender, role, newName)
	}

	@Subcommand("edit color")
	@CommandCompletion("@nationRoles @chatcolors")
	@Description("Edit a role's newColor")
	override fun onEditColor(sender: Player, role: String, newColor: SLTextStyle) {
		super.onEditColor(sender, role, newColor)
	}

	@Subcommand("edit weight")
	@CommandCompletion("@nationRoles @range:1000 @nothing")
	@Description("Edit a role's weight")
	override fun onEditWeight(sender: Player, role: String, newWeight: Int) {
		super.onEditWeight(sender, role, newWeight)
	}

	@Subcommand("delete")
	@CommandCompletion("@nationRoles @nothing")
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
	@CommandCompletion("@nationMembers @nothing")
	@Description("Member role list GUI editor")
	override fun onMemberGUI(sender: Player, player: String) {
		super.onMemberGUI(sender, player)
	}

	@Subcommand("member add")
	@CommandCompletion("@nationMembers @nationRoles @nothing")
	@Description("Give a role to a member")
	override fun onMemberAdd(sender: Player, player: String, role: String) {
		super.onMemberAdd(sender, player, role)
	}

	@Subcommand("member remove")
	@CommandCompletion("@nationMembers @nationRoles @nothing")
	@Description("Take a role from a member")
	override fun onMemberRemove(sender: Player, player: String, role: String) {
		super.onMemberRemove(sender, player, role)
	}
}

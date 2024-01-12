package net.horizonsend.ion.server.command.nations.roles

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import org.bukkit.entity.Player
import org.litote.kmongo.eq

@CommandAlias("settlementrole|srole")
internal object SettlementRoleCommand : RoleCommand<Settlement, SettlementRole.Permission, SettlementRole>() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("settlementMembers") {
			val player = it.player
			val cached = PlayerCache[player]
			val settlement = cached.settlementOid ?: return@registerAsyncCompletion listOf()

			SLPlayer.findProp(SLPlayer::settlement eq settlement, SLPlayer::lastKnownName).toList()
		}

		manager.commandCompletions.registerAsyncCompletion("settlementRoles") {
			val player = it.player
			val cached = PlayerCache[player]
			val settlement = cached.settlementOid ?: return@registerAsyncCompletion listOf()

			SettlementRole.findProp(SettlementRole::parent eq settlement, SettlementRole::name).toList()
		}

		manager.commandCompletions.registerAsyncCompletion("settlementPermissions") {
			SettlementRole.Permission.values().map { it.toString() }
		}
	}

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
	@CommandCompletion("@settlementRoles @nothing")
	@Description("Open role's edit menu")
	override fun onEdit(sender: Player, role: String) {
		super.onEdit(sender, role)
	}

	@Subcommand("permission gui")
	@CommandCompletion("@settlementRoles @nothing")
	@Description("GUI Role Permission Manager")
	override fun onPermissionGUI(sender: Player, role: String) {
		super.onPermissionGUI(sender, role)
	}

	@Subcommand("permission add")
	@CommandCompletion("@settlementRoles @settlementPermissions @nothing")
	@Description("Give a role a permission")
	override fun onPermissionAdd(sender: Player, role: String, permission: SettlementRole.Permission) {
		super.onPermissionAdd(sender, role, permission)
	}

	@Subcommand("permission list")
	@CommandCompletion("@settlementRoles @nothing")
	@Description("List a role's settlementPermissions")
	override fun onPermissionList(sender: Player, role: String) {
		super.onPermissionList(sender, role)
	}

	@Subcommand("permission remove")
	@CommandCompletion("@settlementRoles @settlementPermissions @nothing")
	@Description("Take a role's permission")
	override fun onPermissionRemove(sender: Player, role: String, permission: SettlementRole.Permission) {
		super.onPermissionRemove(sender, role, permission)
	}

	@Subcommand("edit name")
	@CommandCompletion("@settlementRoles @nothing")
	@Description("Edit a role's name")
	override fun onEditName(sender: Player, role: String, newName: String) {
		super.onEditName(sender, role, newName)
	}

	@Subcommand("edit color")
	@CommandCompletion("@settlementRoles @chatcolors")
	@Description("Edit a role's newColor")
	override fun onEditColor(sender: Player, role: String, newColor: SLTextStyle) {
		super.onEditColor(sender, role, newColor)
	}

	@Subcommand("edit weight")
	@CommandCompletion("@settlementRoles @range:1000")
	@Description("Edit a role's weight")
	override fun onEditWeight(sender: Player, role: String, newWeight: Int) {
		super.onEditWeight(sender, role, newWeight)
	}

	@Subcommand("delete")
	@CommandCompletion("@settlementRoles @nothing")
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
	@CommandCompletion("@settlementMembers @nothing")
	@Description("Member role list GUI editor")
	override fun onMemberGUI(sender: Player, player: String) {
		super.onMemberGUI(sender, player)
	}

	@Subcommand("member add")
	@CommandCompletion("@settlementMembers @settlementRoles @nothing")
	@Description("Give a role to a member")
	override fun onMemberAdd(sender: Player, player: String, role: String) {
		super.onMemberAdd(sender, player, role)
	}

	@Subcommand("member remove")
	@CommandCompletion("@settlementMembers @settlementRoles @nothing")
	@Description("Take a role from a member")
	override fun onMemberRemove(sender: Player, player: String, role: String) {
		super.onMemberRemove(sender, player, role)
	}
}

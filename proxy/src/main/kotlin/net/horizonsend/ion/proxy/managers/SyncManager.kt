package net.horizonsend.ion.proxy.managers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.InheritanceNode
import java.util.concurrent.TimeUnit

open class SyncManager(jda: JDA, private val configuration: ProxyConfiguration) {
	private val guild = jda.getGuildById(configuration.discordServer)
	val api = LuckPermsProvider.get()

	private val mappedRoles: Map<InheritanceNode, Role> = guild?.let { guild: Guild ->
		configuration.roleMap
			.mapKeys { api.nodeBuilderRegistry.forInheritance().group(it.key).value(true).build() }
			.mapValues { guild.getRoleById(it.value)!! }
	} ?: mapOf()

	open fun onEnable() {
		if (guild == null) return

		IonProxy.Ion.proxy.scheduler.schedule(
			IonProxy.Ion,
			{ sync() },
			0,
			900,
			TimeUnit.SECONDS
		)
	}

	open fun sync(): List<String> {
		if (guild == null) return listOf()

		val changeLog = mutableListOf<String>()

		val linkBypassRole = guild.getRoleById(configuration.linkBypassRole)
		val membersWithLinkBypass = guild.getMembersWithRoles(linkBypassRole)

		for (member in guild.members) {
			if (member.user.isBot || membersWithLinkBypass.contains(member)) continue

			val playerData = PlayerData[member.idLong]
			val luckPermsUser = playerData?.let { api.userManager.getUser(it.uuid.value) } ?: continue

			// Sync Minecraft -> Discord
			for (group in mappedRoles.keys) {
				changeLog +=
					if (luckPermsUser.nodes.contains(group)) {
						if (member.roles.contains(mappedRoles[group])) continue

						guild.addRoleToMember(member, mappedRoles[group] ?: continue).queue()

						"- Granted ${mappedRoles[group]?.asMention} to ${member.asMention}"
					} else {
						if (!member.roles.contains(mappedRoles[group])) continue

						guild.removeRoleFromMember(member, mappedRoles[group] ?: continue).queue()

						"- Removed ${mappedRoles[group]?.asMention} from ${member.asMention}"
					}
			}

			// Sync Discord -> Minecraft
			for (role in mappedRoles.values) {
				val group = mappedRoles.entries.first { it.value == role }.key

				changeLog +=
					if (member.roles.contains(role)) {
						if (luckPermsUser.nodes.contains(group)) continue

						luckPermsUser.data().add(group)

						"- Granted role $role to ${member.asMention}"
					} else {
						if (!luckPermsUser.nodes.contains(group)) continue

						luckPermsUser.data().remove(group)

						"- Removed role $role from ${member.asMention}"
					}
			}

			playerData.let {
				try {
					if (member.effectiveName != it.username) {
						member.modifyNickname(it.username).queue()
						changeLog += "- Updated name of ${member.asMention}"
					} else {
						if (member.nickname == member.user.name) {
							member.modifyNickname(null).queue()
							changeLog += "- Updated name of ${member.asMention}"
						}
					}
				} catch (exception: Exception) {
					exception.printStackTrace()
				}
			}
		}

		return changeLog
	}
}

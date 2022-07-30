package net.horizonsend.ion.common.utilities

import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider

fun constructPlayerListNameAsync(username: String, uuid: UUID): CompletableFuture<Component> = luckPerms {
		it.userManager.loadUser(uuid).thenApplyAsync { user ->
			constructPlayerListName(username, user.cachedData.metaData.prefix, user.cachedData.metaData.suffix)
		}
	} ?: CompletableFuture.completedFuture(Component.text(username))

fun constructPlayerListName(username: String, prefix: String?, suffix: String?): Component {
	val displayName = Component.text()

	prefix?.let {
		displayName.append(LegacyComponentSerializer.legacyAmpersand().deserialize(it))
		if (!it.endsWith(" &r")) displayName.append(Component.text(' '))
	}

	displayName.append(Component.text(username))

	suffix?.let {
		if (!it.startsWith(" ")) displayName.append(Component.text(' '))
		displayName.append(LegacyComponentSerializer.legacyAmpersand().deserialize(it))
	}

	return displayName.build()
}
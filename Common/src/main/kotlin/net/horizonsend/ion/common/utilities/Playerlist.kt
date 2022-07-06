package net.horizonsend.ion.common.utilities

import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider

fun constructPlayerListNameAsync(username: String, uuid: UUID): CompletableFuture<Component> {
	// Attempt to get the LuckPerms API, and safely fallback if the API is absent
	val luckPerms =
		try { LuckPermsProvider.get() }
		catch (_: NoClassDefFoundError) { return CompletableFuture.completedFuture(Component.text(username)) }

	return luckPerms.userManager.loadUser(uuid).thenApplyAsync { user ->
		constructPlayerListName(username, user.cachedData.metaData.prefix, user.cachedData.metaData.suffix)
	}
}

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
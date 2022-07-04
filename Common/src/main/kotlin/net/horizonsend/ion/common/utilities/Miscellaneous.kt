package net.horizonsend.ion.common.utilities

import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.CompletableFuture
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply {
		addAll(elems)
	}

fun constructPlayerListName(username: String, uuid: UUID): CompletableFuture<Component> {
	// Attempt to get the LuckPerms API, and safely fallback if the API is absent
	val luckPerms =
		try { LuckPermsProvider.get() }
		catch (_: IllegalStateException) { return CompletableFuture.completedFuture(Component.text(username)) }

	return luckPerms.userManager.loadUser(uuid).thenApplyAsync { user ->
		val displayName = Component.text()

		user.cachedData.metaData.prefix?.let {
			displayName.append(LegacyComponentSerializer.legacyAmpersand().deserialize(it))
			if (!it.endsWith(' ')) displayName.append(Component.text(' '))
		}

		displayName.append(Component.text(username))

		user.cachedData.metaData.suffix?.let {
			if (!it.startsWith(' ')) displayName.append(Component.text(' '))
			displayName.append(LegacyComponentSerializer.legacyAmpersand().deserialize(it))
		}

		displayName.build()
	}
}
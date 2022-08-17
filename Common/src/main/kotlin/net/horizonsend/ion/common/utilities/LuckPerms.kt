package net.horizonsend.ion.common.utilities

import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider

inline fun <T> luckPerms(execute: (LuckPerms) -> T): T? {
	return execute(
		try {
			LuckPermsProvider.get()
		} catch (_: NoClassDefFoundError) {
			return null
		} catch (_: IllegalStateException) {
			return null
		}
	)
}
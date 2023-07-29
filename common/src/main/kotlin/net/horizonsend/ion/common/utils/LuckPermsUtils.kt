package net.horizonsend.ion.common.utils

import net.luckperms.api.LuckPermsProvider
import java.util.UUID

val luckPerms = LuckPermsProvider.get()

fun UUID.lpHasPermission(s: String) = luckPerms.userManager.getUser(this)?.cachedData?.permissionData?.checkPermission(s)?.asBoolean() == true

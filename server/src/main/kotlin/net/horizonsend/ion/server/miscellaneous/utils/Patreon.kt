package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.utils.lpHasPermission
import org.bukkit.entity.Player

fun Player.isFiveDollar() = this.uniqueId.lpHasPermission("patron5")
fun Player.isTenDollar() = this.uniqueId.lpHasPermission("patron10")
fun Player.isFifteenDollar() = this.uniqueId.lpHasPermission("patron15")

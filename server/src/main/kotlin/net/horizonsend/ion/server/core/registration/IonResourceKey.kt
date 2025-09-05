package net.horizonsend.ion.server.core.registration

import net.horizonsend.ion.server.IonServer
import org.bukkit.NamespacedKey

open class IonResourceKey<T: Any>(val key: String) {
	val ionNapespacedKey = NamespacedKey(IonServer, key)
}

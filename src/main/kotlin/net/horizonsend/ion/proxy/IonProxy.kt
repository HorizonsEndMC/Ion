package net.horizonsend.ion.proxy

import com.google.inject.Inject
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger

@Plugin(
	id = "ion",
	name = "Ion",
	version = "unspecified",
	authors = ["Peter Crawley"]
)
@Suppress("Unused")
class IonProxy @Inject constructor(
	@Suppress("Unused_Parameter") server: ProxyServer,
	@Suppress("Unused_Parameter") slF4JLogger: Logger
)
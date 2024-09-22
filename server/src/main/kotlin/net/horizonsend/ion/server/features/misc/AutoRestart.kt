package net.horizonsend.ion.server.features.misc

import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import java.io.File
import java.util.concurrent.TimeUnit

object AutoRestart : IonServerComponent() {
	private val file = File(IonServer.dataFolder, "this_is_a_restart")

	private val isRestart = file.exists()

	override fun onEnable() {
		if (isRestart) {
			file.delete()
		}

		Tasks.asyncAtHour(IonServer.configuration.restartHour) {
			fun sleep(length: Int, timeUnit: TimeUnit) {
				Thread.sleep(timeUnit.toMillis(length.toLong()))
			}

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("15", WHITE, BOLD), text(" minutes", YELLOW, BOLD)))
			sleep(5, TimeUnit.MINUTES)

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("10", WHITE, BOLD), text(" minutes", YELLOW, BOLD)))
			sleep(5, TimeUnit.MINUTES)

			for (i in 5 downTo 2) {
				Bukkit.broadcast(template(text("The server is rebooting in {0} minutes", YELLOW, BOLD), i))
				sleep(1, TimeUnit.MINUTES)
			}

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("60", WHITE, BOLD), text(" seconds", YELLOW, BOLD)))
			sleep(15, TimeUnit.SECONDS)

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("45", WHITE, BOLD), text(" seconds", YELLOW, BOLD)))
			sleep(15, TimeUnit.SECONDS)

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("30", WHITE, BOLD), text(" seconds", YELLOW, BOLD)))
			sleep(15, TimeUnit.SECONDS)

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("15", WHITE, BOLD), text(" seconds", YELLOW, BOLD)))
			sleep(5, TimeUnit.SECONDS)

			for (i in 10 downTo 2) {
				Bukkit.broadcast(template(text("The server is rebooting in {0} seconds", YELLOW, BOLD), i))
				sleep(1, TimeUnit.SECONDS)
			}

			Bukkit.broadcast(ofChildren(text("The server is rebooting in ", YELLOW, BOLD), text("1", WHITE, BOLD), text(" second", YELLOW, BOLD)))
			sleep(1, TimeUnit.SECONDS)

			file.createNewFile()
			Tasks.sync {
				Bukkit.shutdown()
			}
		}
	}
}

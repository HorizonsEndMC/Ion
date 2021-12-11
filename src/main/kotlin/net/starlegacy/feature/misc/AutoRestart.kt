package net.starlegacy.feature.misc

import net.starlegacy.SETTINGS
import net.starlegacy.SLComponent
import net.starlegacy.util.Tasks
import net.starlegacy.util.bold
import net.starlegacy.util.text
import net.starlegacy.util.yellow
import org.bukkit.Bukkit
import java.io.File
import java.util.concurrent.TimeUnit

object AutoRestart : SLComponent() {
    private val file = File(plugin.dataFolder, "this_is_a_restart")

    val isRestart = file.exists()

    override fun onEnable() {
        if (isRestart) {
            file.delete()
        }

        Tasks.asyncAtHour(SETTINGS.restartHour) {
            fun sleep(length: Int, timeUnit: TimeUnit) {
                Thread.sleep(timeUnit.toMillis(length.toLong()))
            }

            Bukkit.broadcast("The server is rebooting in 15 minutes".text().yellow().bold())
            sleep(5, TimeUnit.MINUTES)

            Bukkit.broadcast("The server is rebooting in 10 minutes".text().yellow().bold())
            sleep(5, TimeUnit.MINUTES)

            for (i in 5 downTo 2) {
                Bukkit.broadcast("The server is rebooting in $i minutes".text().yellow().bold())
                sleep(1, TimeUnit.MINUTES)
            }

            for (i in 60 downTo 2) {
                Bukkit.broadcast("The server is rebooting in $i seconds".text().yellow().bold())
                sleep(1, TimeUnit.SECONDS)
            }

            Bukkit.broadcast("The server is rebooting in 1 second".text().yellow().bold())
            sleep(1, TimeUnit.SECONDS)

            file.createNewFile()
            Tasks.sync {
                Bukkit.shutdown()
            }
        }
    }

    override fun supportsVanilla(): Boolean {
        return true
    }
}

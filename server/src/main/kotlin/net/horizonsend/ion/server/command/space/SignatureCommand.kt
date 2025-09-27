package net.horizonsend.ion.server.command.space

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.space.signatures.SignatureManager
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

@CommandAlias("signature")
object SignatureCommand : SLCommand() {

    @Default
    fun onList(sender: CommandSender) {
        val signatures = SignatureManager.activeSignatures

        val body = formatPaginatedMenu(
            signatures.size,
            "/signature",
            1
        ) { index ->
            ofChildren(
                signatures[index].signatureType.displayName,
                Component.text(" at ${signatures[index].location}"))
        }

        if (signatures.isEmpty()) sender.userError("No signatures")

        sender.sendMessage(body)
    }
}
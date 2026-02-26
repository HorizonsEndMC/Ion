package net.horizonsend.ion.server.command.space

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.keys.SignatureTypeKeys
import net.horizonsend.ion.server.features.space.signatures.SignatureManager
import net.horizonsend.ion.server.features.space.signatures.SignatureType
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

@CommandAlias("signature")
object SignatureCommand : SLCommand() {
    override fun onEnable(manager: PaperCommandManager) {
        manager.commandCompletions.registerCompletion("signatures") { SignatureTypeKeys.allStrings() }

        manager.commandContexts.registerContext(SignatureType::class.java) {
            val id = it.popFirstArg()
            SignatureTypeKeys[id]?.getValue() ?: throw InvalidCommandArgument("Signature $id not found!")
        }

        manager.commandCompletions.setDefaultCompletion("signatures", SignatureType::class.java)
    }

    @Default
    fun onList(sender: CommandSender) {
        val signatures = SignatureManager.activeSignatures.keys.toList()

        val body = formatPaginatedMenu(
            signatures.size,
            "/signature",
            1
        ) { index ->
            ofChildren(
                signatures[index].signatureType.displayName,
                Component.text(" at ${signatures[index].location.world} x ${signatures[index].location.x} z ${signatures[index].location.z}"))
        }

        if (signatures.isEmpty()) sender.userError("No signatures")

        sender.sendMessage(body)
    }

    @Subcommand("spawn")
    @CommandPermission("ion.signature.spawn")
    fun onSpawn(sender: CommandSender, signature: SignatureType) {
        IonRegistries.SIGNATURE_TYPE[signature.key].nextSpawnTimeMillis = System.currentTimeMillis()
        sender.success("Immediately triggered spawn for signature ${signature.displayName.plainText()}")
    }
}

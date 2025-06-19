package net.horizonsend.ion.common.utils.text.gui

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

fun sendWithdrawMessage(audience: Audience, amount: Number) = audience.sendMessage(template(Component.text("{0} has been withdrawn from your account.", HE_MEDIUM_GRAY), amount.toCreditComponent()))
fun sendDepositMessage(audience: Audience, amount: Number) = audience.sendMessage(template(Component.text("{0} has been added to your account.", HE_MEDIUM_GRAY), amount.toCreditComponent()))

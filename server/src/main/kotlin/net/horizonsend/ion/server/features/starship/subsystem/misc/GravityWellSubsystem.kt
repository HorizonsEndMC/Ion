package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign
import java.time.Duration
import kotlin.math.cbrt
import kotlin.math.pow

class GravityWellSubsystem(starship: ActiveStarship, sign: Sign, multiblock: GravityWellMultiblock
) : AbstractMultiblockSubsystem<GravityWellMultiblock>(starship, sign, multiblock){
		val baseCooldown = Duration.ofMinutes(10).toMillis()
		val baseDuration = Duration.ofMinutes(5).toMillis()
		val shipSizeModifier = cbrt(500.0)
		var activatedTimer = 0L
		var cooldownTimer = 0L
}

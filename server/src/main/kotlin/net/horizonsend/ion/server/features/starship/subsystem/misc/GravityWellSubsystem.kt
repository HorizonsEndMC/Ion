package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class GravityWellSubsystem(starship: ActiveStarship, sign: Sign, multiblock: GravityWellMultiblock) :
	AbstractMultiblockSubsystem<GravityWellMultiblock>(starship, sign, multiblock)

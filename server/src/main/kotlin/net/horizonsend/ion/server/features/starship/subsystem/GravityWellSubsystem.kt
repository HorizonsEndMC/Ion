package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class GravityWellSubsystem(starship: ActiveStarship, sign: Sign, multiblock: GravityWellMultiblock) :
	AbstractMultiblockSubsystem<GravityWellMultiblock>(starship, sign, multiblock)

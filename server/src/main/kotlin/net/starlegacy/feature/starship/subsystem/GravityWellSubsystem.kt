package net.starlegacy.feature.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.gravitywell.GravityWellMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.block.Sign

class GravityWellSubsystem(starship: ActiveStarship, sign: Sign, multiblock: GravityWellMultiblock) :
	AbstractMultiblockSubsystem<GravityWellMultiblock>(starship, sign, multiblock)

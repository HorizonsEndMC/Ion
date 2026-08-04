package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.starship.HornMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell.GravityWellMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class HornSubsystem(starship: ActiveStarship, sign: Sign, multiblock: HornMultiblock) :
	AbstractMultiblockSubsystem<HornMultiblock>(starship, sign, multiblock)

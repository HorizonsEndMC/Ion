package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell.DisruptorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class DisruptorSubsystem(starship: ActiveStarship, sign: Sign, multiblock: DisruptorMultiblock) :
	AbstractMultiblockSubsystem<DisruptorMultiblock>(starship, sign, multiblock)

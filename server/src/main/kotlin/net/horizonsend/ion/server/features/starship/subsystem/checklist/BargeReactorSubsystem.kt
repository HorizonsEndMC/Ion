package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class BargeReactorSubsystem (starship: ActiveStarship, sign: Sign, multiblock: net.horizonsend.ion.server.features.multiblock.type.checklist.BargeReactorMultiBlock) :
        AbstractMultiblockSubsystem<net.horizonsend.ion.server.features.multiblock.type.checklist.BargeReactorMultiBlock>(starship, sign, multiblock)

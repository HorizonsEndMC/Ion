package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.navigationcomputer.NavigationComputerMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class NavCompSubsystem(starship: ActiveStarship, sign: Sign, multiblock: NavigationComputerMultiblock) :
	AbstractMultiblockSubsystem<NavigationComputerMultiblock>(starship, sign, multiblock)

package net.starlegacy.feature.starship.subsystem

import net.starlegacy.feature.multiblock.navigationcomputer.NavigationComputerMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.block.Sign

class NavCompSubsystem(starship: ActiveStarship, sign: Sign, multiblock: NavigationComputerMultiblock) :
	AbstractMultiblockSubsystem<NavigationComputerMultiblock>(starship, sign, multiblock)

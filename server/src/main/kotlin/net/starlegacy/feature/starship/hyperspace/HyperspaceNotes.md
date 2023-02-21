## Hyperspace seqeunce

1. Command is run, from [net.starlegacy.command.starship.MiscStarshipCommands.tryJump] and runs
[net.starlegacy.feature.starship.hyperspace.Hyperspace.beginJumpWarmup]
2. In function, it sets up destionation, calucualtes speed and inits
[net.starlegacy.feature.starship.hyperspace.HyperspaceWarmup] object
3. Object handels countdown and when done calls
[net.starlegacy.feature.starship.hyperspace.Hyperspace.completeJumpWarmup]
4.



Intent:

1. Draw warm up marker
2. after 30 seconds after warm up get rid of aroww
3. track player though hyperspace

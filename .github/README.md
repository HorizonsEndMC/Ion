## Ion
Ion is the main plugin used on Horizon's End, it is split into 2 plugins, a Waterfall Proxy Plugin and a Paper Server
Plugin. Ion uses the released Star Legacy Plugin as its foundations, the original source of which can be found at
[MicleBrick/StarLegacy](https://github.com/MicleBrick/StarLegacy), although we are slowly looking to replace
this code with more modern and stable implementations.

As Ion is developed and maintained specifically for Horizon's End, pre-compiled builds are not distributed and no
support will be provided for those wishing to use the plugin for their own server.

In the future we may consider separating parts of Ion and developing them into standalone plugins / mods / libraries,
however there are no plans for this in the near future.

If you wish to help develop Ion, then it's best to coordinate with Horizon's End Staff in #development in the
[Discord](https://discord.gg/RPvgQsGzKM).

### Building & Testing

Building the plugin is done like any other Gradle project. The resulting Jar files are located in `build/IonProxy.jar`
and `build/IonServer.jar`.

This repository includes a script that will set up a functioning system comprising of Ion, IonCore, and its required
dependencies. To use it simply ensure that Docker and Docker Compose are installed and running, and then use
`testServer`. This script is a bash script, using it on Windows will require Windows Subsystem for Linux or Git Bash.

To use the test server run `sh testServer setup` and then start it with `sh testServer run`, if there are any issues try
`sh testServer run-fallback`.

You can also start, view logs, and stop the server independently with `sh testServer start`, `sh testServer logs`,
and `sh testServer stop`.

If the test server breaks, use `sh testServer reset` to reset it back to it's default state.

### Contributing

Contributions must follow the following rules:

1) Lines should be 120 characters long at most, this is not a strict requirement, lines *can* be longer.

2) Never use wildcard imports.

3) Sometimes there can be name conflicts when importing, import them with a custom name, prefixed by the source. For
   example "LibAListener" and "LibBListener".

4) Avoid excessive use of `.apply {}` or similar.

5) If there is a large block of mostly similar code, align it with spaces, as it makes things more readable.

6) To prevent IntelliJ from complaining, please `@Suppress("Unused")`for any entry points. Don't just tell IntelliJ to
   ignore them for that class as that only applies to you, not everyone else.

7) Code standards are generally higher in `net.horizonsend.ion.server` and `net.horizonsend.ion.proxy`. Code in
   `net.horizonsend.ion.server.legacy` and `net.starlegacy` is generally older code which we intend to replace anyway
   and as such code standards are lower.

8) Don't go out of your way to resolve deprecations in older code, however if you are working with deprecated code, you
   are expected to update it.

9) Always prefer using Minecraft's internal code instead of the Bukkit API, this is for a few reasons:
   - The Bukkit API is slow and full of old legacy code.
   - A distant plan is to convert Ion into a Fabric / Quilt Mod.

10) Within the server plugin coordinates should be handled internally as 3 separate numbers, or when returning from a
    function, a `Triple<T, T, T>` should be used.

### Building
The project can be built by downloading it, navigating into the directory, and running `./gradlew build` or
`gradlew.bat build` on Windows.

The compiled jar be `build/Ion.jar` and `build/IonCore.jar`.

### Contributing
Contributions must follow the following rules:
1) Lines should be 120 characters long at most, this is not a strict requirement, lines *can* be longer.

2) Never use wildcard imports.

3) Sometimes there can be name conflicts when importing, import them with a custom name, prefixed by the source. For
   example "LibAListener" and "LibBListener".

4) Avoid excessive use of `.apply {}` or similar.

5) Do not statically import individual elements from enums or objects.

6) If there is a large block of mostly similar code, align it with spaces, as it makes things more readable.

7) Any additions that absolutely requires IonCore to function should go there, otherwise it goes in the main project.

8) All event listeners must specify a priority based on the criteria below:
	- Does the listener unconditionally cancel the event? If so, use LOWEST.
    - Does the listener conditionally cancel the event? If so, use LOW.
    - Does the listener alter the events data? If so, use NORMAL.
    - Does the listener simply act on the result of the event? If so, use MONITOR.
    - HIGH and HIGHEST should not be used right now.

9) To prevent IntelliJ from complaining, please `@Suppress("unused")`for any entry points. Don't just tell IntelliJ to
   ignore them for that class as that only applies to you, not everyone else.

10) These rules are more relaxed with IonCore, but should still be kept to if possible.
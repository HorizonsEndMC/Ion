[Go Back](../README.md)

# Debugging

Debugging an essential process for any programmer. Generally, it is very
helpful to use a *debugger* to do this. IntelliJ has a good debugger built in.
To use it with the test server, you must first make a bridge to the test server
through an SSH tunnel. Fortunately, we have a script for this.

First, run the `./debug_test_server.sh` script in Git Bash.
*(Don't do this if you are using the local test server!)*
Then, you can add a configuration allowing you to link with it.

![Image of Add Config Button](https://i.imgur.com/ACsbM18.png)

Click that button. Then, click the plus icon:
![Image of Plus Icon](https://i.imgur.com/k33M0Le.png)

It will give you a list of options. Click remote. You should see this menu:
![Image of Menu](https://i.imgur.com/VuI1TJm.png).

Leave the options default (the values they are in the screenshot should be
fine), except change the name to "Test Server". Click "OK".

Now, all you have to do is click the green icon on the top right with the
debug configuration selected (if you have no other, it's the only one, so
you shouldn't need to select it):

![Image of Debug Button](https://i.imgur.com/i2dsyjs.png)

For instructions on how to use IntelliJ IDEA's debugger, please read the
![official guide](https://www.jetbrains.com/help/idea/debugging-code.html).

[Go Back](../README.md)

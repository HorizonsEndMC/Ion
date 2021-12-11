[Go Back](../README.md)

# Test Server

There are two "test server"s you can use: The shared test
server and the (new) local test server system.

## Local Test Server
This uses Docker to containerize the various services required.
You might be able to get this working on plain old Windows,
but I have only tested it using WSL 2 (look it up if you don't know what that is)
and Linux itself.

### Setup

#### Linux
1. Install Docker. Docker has specific instructions for popular distros.
Here are the instructions for Ubuntu: https://docs.docker.com/engine/install/ubuntu/.
2. Enable using Docker as a non-root user (see https://docs.docker.com/engine/install/linux-postinstall/)
3. Make Docker start on startup (see previous link)
4. Install Docker compose: https://docs.docker.com/compose/install/
5. Make sure the `unzip` command is installed, e.g. `sudo apt install unzip`
6. `cd` to the directory where you cloned this repository in a terminal
7. `cd local-server`
8. `./setup.sh`

#### Windows
1. Get WSL 2 (use the Ubuntu distribution if you don't know what you're doing):
   https://docs.microsoft.com/en-us/windows/wsl/install-win10
2. Get Docker with the WSL 2 backend.
   See here for instructions: https://docs.docker.com/docker-for-windows/install/
3. In the WSL 2 (Ubuntu) terminal,
   make sure the `unzip` command is installed,
   e.g. `sudo apt install unzip`
4. Clone this repository in your WSL 2 terminal
5. `cd` to the repository
6. `cd local-server`
7. `./setup.sh`

> NOTE: IntelliJ IDEA supports WSL 2 and lets you open projects
> that are stored in the WSL 2 filesystem. Simply go to File->Open,
> scroll below the C: drive to find the wsl "drive", and find the
> folder you cloned to (try `realpath` if you can't find it)

### Usage
In order to restart the server with an updated compilation
of the plugins from your working space, use `./update.sh` in `local-server`.
You can stop the server using `./stop.sh` and start it using `./start.sh`.
To access the console, use `./console.sh`. To escape the console,
try Ctrl + P, Ctrl + Q. You can mess around with the server files including
the log files and plugin configurations in `local-server/data`.
Finally, make sure to get enough sleep.

## Shared Test Server

In addition to the main server, we also have a test server
(IP: `test.starlegacy.net`). The test server is not always online (it turns off
automatically every day at midnight), so you must turn it on when you need it.

To make it easier to use the test server, we have a variety of Bash scripts
that you may use. The most essential one is `update_test_server.sh`. Here's how to use it.

1. Open Git Bash and navigate to the project directory.
2. Run the command `./update_test_server.sh`
3. If you get errors, go fix them. To find the errors without running the
command, go to "Build" (top menu) and click "Build Project".
4. Log on to the test server (`test.starlegacy.net`) and test the features
relevant to your code changes.

If you need to see the console, another command is `./test_server_ssh.sh`.
This will connect you to a Bash terminal on the test server itself. Run the
command and then you can run the command `./titusc` (through the test server
Bash), which will give you access to the test server. You could also do
`cat ~/titus_data/logs/latest.log` to print the latest log, and you could do
`cat ~/titus_data/logs/latest.log | nc termbin.com 9999` to share the logs
(although you should not share the logs with anyone except other authorized
SL developers).

To learn more about Bash, see
[Steve Parker's well-known Bash tutorial](https://www.shellscript.sh/).

[Go Back](../README.md)

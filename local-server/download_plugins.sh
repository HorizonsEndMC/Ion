#!/bin/bash

cd "$(dirname "$0")" || exit

wget "https://media.forgecdn.net/files/3066/271/worldguard-bukkit-7.0.4.jar" -O ./data/plugins/worldguard.jar
wget "https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar" -O data/plugins/vault.jar
wget "https://github.com/webbukkit/dynmap/releases/download/v3.1-beta-7/Dynmap-3.1-beta7-spigot.jar" -O data/plugins/dynmap.jar
wget "https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar" -O data/plugins/protocollib.jar
wget "https://github.com/EssentialsX/Essentials/releases/download/2.18.2/EssentialsX-2.18.2.0.jar" -O data/plugins/essentialsx.jar

function download_and_extract () {
  mkdir ./tmp
  cd ./tmp || exit
  wget "$1" -O tmp.zip
  unzip -j tmp.zip
  ls
  mv ./*.jar ../data/plugins
  cd ..
  rm ./tmp -r
}

download_and_extract "https://ci.citizensnpcs.co/job/Citizens2/lastSuccessfulBuild/artifact/dist/target/*zip*/target.zip"
download_and_extract "https://ci.athion.net/job/FastAsyncWorldEdit-1.16/lastSuccessfulBuild/artifact/worldedit-bukkit/build/libs/*zip*/libs.zip"
download_and_extract "https://ci.lucko.me/job/LuckPerms/lastSuccessfulBuild/artifact/bukkit/loader/build/libs/*zip*/libs.zip"

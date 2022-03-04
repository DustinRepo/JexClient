![](https://img.shields.io/badge/Based-Very-9080c2)

![](https://forthebadge.com/images/badges/built-by-codebabes.svg)
![](https://forthebadge.com/images/badges/0-percent-optimized.svg)
![](https://forthebadge.com/images/badges/as-seen-on-tv.svg)
![](https://forthebadge.com/images/badges/built-by-crips.svg)
![](https://forthebadge.com/images/badges/contains-technical-debt.svg)
![](https://forthebadge.com/images/badges/designed-in-ms-paint.svg)
![](https://forthebadge.com/images/badges/works-on-my-machine.svg)
![](https://forthebadge.com/images/badges/60-percent-of-the-time-works-every-time.svg)
![](https://forthebadge.com/images/badges/mom-made-pizza-rolls.svg)
![](https://forthebadge.com/images/badges/not-a-bug-a-feature.svg)
![](https://forthebadge.com/images/badges/reading-6th-grade-level.svg)
![](https://forthebadge.com/images/badges/kinda-sfw.svg)
![](https://forthebadge.com/images/badges/built-by-neckbeards.svg)
# Jex Utility Client
[Join the Discord](https://discord.gg/BUcUGu6gfA)

Jex Client is a Minecraft Utility Client made for Anarchy servers. It has support for mods like custom Baritone Processes, ViaFabric compatibility, a Sodium compatible Xray, and more

##Developers Please note
If your JexClient path has any spaces or special characters in it's path, the Features, Commands, and Config Manager will not properly load. [Why?](https://github.com/google/guava/issues/2152)

You can download Jex Client in the discord or in the releases tab
## Things needed:
1. [Git](https://git-scm.com/downloads)
2. [Java 17 JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
3. A Java IDE, like [Intellij](https://www.jetbrains.com/idea/download/) or [Eclipse](https://www.eclipse.org/downloads/)

## Setup Intellij:
Manual
```
1. Open build.gradle as an Intellij project.
2. If you don't see run profiles after it finishes loading, close and re-open the project
```
Automatic 
```
Click the green "Code" button above and open an Intellij project from Git with the link it provides
```

## Setup Eclipse:
```
1. Download the repo and run the command "gradlew eclipse" (use a ./ before command if using PowerShell)
2. Open Eclipse and go to File -> Import -> Import from existing project
3. Click the drop-down box next to the Run button in Eclipse
4. Select "Java Application" then select "jexclient-main_client"
5. Run
```

## How to build
```
1. Run the gradle Build script
2. Drag & drop JexClient.jar into your mods folder
```
## Want to test features before they're released?
Sometimes features will be pushed to github before the update is fully released. If you'd like to try them, you can find auto-builds [here](https://github.com/DustinRepo/JexClient-main/actions?query=event%3Apush)
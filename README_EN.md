# MineClear - Mine Sweeper Game Plugin

A mine sweeper game plugin developed for Nukkit servers, supporting custom game areas, timing functions, and game record saving.

## Features

- ✅ Custom mine sweeper game areas
- ✅ Game timing function
- ✅ Game record saving
- ✅ Automatic game reset when player leaves the area
- ✅ Automatic game reset when player switches maps
- ✅ Custom mine count setting
- ✅ Beautiful game interface

## Installation

1. Download the plugin's JAR file
2. Put the JAR file into the server's `plugins` folder
3. Restart the server
4. The plugin will automatically generate configuration files

## Command Instructions

### Create Game Area

Use the following command to open the form for creating a game area:

```
/mt
```

In the form, you can set:
- Room name
- Game area width (5-20 blocks)
- Game area height (5-20 blocks)
- Mine count (1-50)

### Delete Game Area

Use the following command to delete the specified game area:

```
/mt remove <room name>
```

## Gameplay

1. After creating a game area, click on the blocks in the area to start the game
2. When clicking blocks, it will be processed according to mine sweeper rules:
   - Hit a mine: Game over, display time used
   - Hit a number: Display the number of mines around
   - Hit a blank: Automatically chain mine around blocks
3. When all non-mine blocks are mined, the game is won, display time used and save the record

## Configuration Files

### room.yml

Stores game area configuration information, format as follows:

```yaml
room:
- startX: 100
  endX: 108
  y: 64
  startZ: 100
  endZ: 108
  mine: 10
  levelName: world
  roomName: Test Room
```

### player.yml

Stores player game records, format as follows:

```yaml
PlayerName:
  RoomName:
    timeUsed: 10
    lastPlayed: 1620000000000
```

## Resource Pack

The plugin includes a resource pack that provides textures and models needed for the mine sweeper game:
- Mine textures and models
- Number textures and models
- Flag textures and models
- Block textures and models

## Permission Instructions

- Only OP players can use the `/mt` command to create and delete game areas
- All players can participate in the mine sweeper game

## Notes

- Game area size is recommended to be between 5x5 and 20x20
- Mine count is recommended not to exceed 30% of the total blocks
- The game will automatically reset when the player leaves the game area for more than 5 meters
- The game will automatically reset when the player switches maps

Game Effects:

| ![Game Effect](img/p1.png) | ![Game Effect](img/p2.png) |
| --- | --- |

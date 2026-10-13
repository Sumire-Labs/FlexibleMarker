# Flexible Marker CRE (Cleanroom Edition)

This mod requires Cleanroom Loader.

A Cleanroom port of [Flexible Marker](https://www.curseforge.com/minecraft/mc-mods/flexiblemarker).

## Description

### Flexible Marker

A marker that lets you configure a machine's working area by setting the distances upward, downward, left, right, and forward independently. You can adjust the area through its GUI after placement, without having to place multiple markers far apart.

Useful for defining areas around structures or limiting mining and terrain clearing to specific heights.

### Chunk Marker

A marker that lets you configure a machine's working area in chunks. The horizontal area aligns with chunk boundaries, and you can adjust the minimum and maximum Y coordinates through its GUI.

Useful for chunk-aligned mining and clearing larger areas.

## Requirements

- Cleanroom Loader 0.6.13 or later
- ModularUI 3.1.6 or later

## Changes

- Rewrote the GUIs using ModularUI.
- Rewrote the Scala code in Java, removing the dependency on a Scala runtime provider.
- Migrated to Java 25 and Cleanroom Loader.

## License

All Rights Reserved

## Credits

- [Kotori316](https://github.com/Kotori316) (Original author)
  - Creator of the original mod.
  - Original project: [Flexible Marker](https://github.com/Kotori316/FlexibleMarker) — All Rights Reserved.
  - Granted special permission to port this mod to Cleanroom and distribute the modified version.

- CleanroomMC
  - CleanroomModTemplate
  - CleanroomVersioning
  - TokenEnvoy

This project is an unofficial fork, not an official release by the original author.

This fork retains the original project's All Rights Reserved status. The modified version is published and distributed with permission from the original author. This permission does not grant third parties unrestricted rights to reuse or redistribute the project.

# Splinecart for Forge 1.20.1 (unofficial port)

A port of [Splinecart](https://modrinth.com/mod/splinecart) by FoundationGames to Forge 1.20.1. The code comes from [hedgehog1029's NeoForge 1.21.1 port](https://github.com/hedgehog1029/Splinecart/tree/1.21.1-neoforge), moved to Forge 1.20.1.

Place Track Ties blocks, then use a Track item on one and then on the next to join them with a curve that can climb, drop and turn. Send a minecart into the first ties block and it follows the track. There are three tracks: Track, Chain Drive Track (pulls carts along) and Magnetic Track (speeds carts up or slows them down with redstone).

All the design is FoundationGames' work. See [NOTICE.md](NOTICE.md) for the credits and [CHANGELOG.md](CHANGELOG.md) for what changed.

## Requirements

- Minecraft 1.20.1
- Forge 47 or newer
- Install it on the server and on the clients. No other mods needed.

## Client options

Set them with `/splinecartc config <option> [value]`. They are saved in `config/splinecart/splinecart_client.properties`.

| Option | Default | |
|---|---|---|
| `rotate_camera` | true | Your camera turns with the track while you ride. Turn it off if it makes you dizzy |
| `vbos` | false | Draws tracks with vertex buffer objects |
| `track_resolution` | 3 | Segments per track when drawn (1 to 16) |
| `track_render_distance` | 8 | Chunks before a track stops drawing (4 to 32) |

## Tested

- Client: ties placed on the ground and on pillars, linked with the track item, a cart ridden through a curve, up and down a climb and down a 51 block drop, the chain drive track on a slope, a ties block broken, the config command, saving and reopening the world.
- Not tested: magnetic track, survival recipes, a dedicated server with players, Create, shaders.

If something breaks, open an issue here with your `latest.log` and your mod list.

## Building

```
./gradlew build
```

The jar ends up in `build/libs`. You need JDK 17.

## License

MIT, same as the original. See [LICENSE](LICENSE).

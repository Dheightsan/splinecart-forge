# Changelog

## 0.3.1-1.20.1.0

First Forge 1.20.1 build of Splinecart by FoundationGames. It is derived from hedgehog1029's NeoForge 1.21.1 port of Splinecart 0.3.1, so it keeps that branch's own changes, listed below.
Same four items as the original: Track Ties, Track, Chain Drive Track and Magnetic Track.

Inherited from hedgehog1029's branch:

- NeoForge and Mojang names instead of Fabric and Yarn. The Fabric API is not needed.
- The track shader is registered through an event, and the original's game renderer mixin is not present.
- Track Ties block models are marked as cutout.
- The camera mixin returns early when the camera entity is missing.

Changed in this build:

- NeoForge entry classes replaced by the Forge mod lifecycle and Forge deferred registers. Client setup, renderers, the shader and the `/splinecartc` client command go through Forge events.
- Java 21 code built for Java 17. The two mixin config files are merged into one.
- Data components rewritten for 1.20.1: the first selected Track Ties block is stored in the item NBT, and item descriptions are tooltips with the same translation keys.
- Network data written with `FriendlyByteBuf` instead of `StreamCodec`. Block and block entity methods use their 1.20.1 signatures, and `useWithoutItem` became `use`.
- Rendering calls moved to the 1.20.1 API. The render bounding box moved from the renderer to the block entity.
- The two entity mixin handlers that used MixinExtras became cancellable injections. The camera mixin targets the 1.20.1 `setRotation`.
- The chunk culling mixin uses a different hook, because 1.20.1 lacks the 1.21 method it relied on.
- Track shader rewritten for the 1.20.1 core shader format. The custom render type moved into a `RenderType` subclass.
- Data files use the 1.20.1 folder names and recipe result key. Ingredients are unchanged.
- Textures, icon and translations are unchanged.

Tested in the client: placing and linking ties, riding through curves, climbs and drops, the chain drive track, breaking a ties block, the config command, and saving and reopening a world.
Not tested: magnetic track, survival recipes, a dedicated server with players, other mods, shaders. Please report problems on the issue tracker of this port.

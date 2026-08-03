# Auto Place Light

Automatically place a light source from your hands wherever it's dark.

Any spot at or below your configured light level gets a light source placed in it as long as you're holding one.

The toggle key is grave/tilde by default "\`"

Works with any light source, the item list accepts IDs, item tags, and wildcards, covers most mods.

Client-side only, no server install needed.

## Config options (autoplacelight-client.toml.)

- `checkWalls`: Require a line of sight before placing.
- `horizontalRadius`: How far to search horizontally in blocks.
- `verticalRadius`: How far to search vertically in blocks.
- `lightLevel`: Lights will be placed whenever the light level is at or below this value.
- `ignoreSunlight`: Do not count sunlight as lit.
- `dontPlaceInFluid`: Skip spots occupied by fluid.
- `placeIntervalTicks`: Client tick delay between placements.
- `placeItem`: Item id's of the items to place. Accepts tags(#) and wild cards(*).

## Item list example

- Exact ID: `minecraft:torch`
- Tag: `#c:torches`
- Wildcard: `*:*_lantern*`

Search radius is capped at 4 blocks because thats how far the server lets you place. Anything larger would just be rejected.

Torches are placed on floors. Wall-mounted torches don't work.

Items that place unlit blocks such as candles and redstone lamps will keep being placed until light is provided.

This mod is licensed under CC0-1.0. Do whatever you like with this mod, you do not have to credit me.

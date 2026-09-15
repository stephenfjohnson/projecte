# ProjectE on Fabric — where the port stands

This branch (`claude/fabric-modloader-port-mz351d`) is a hard fork of ProjectE onto **Fabric for
Minecraft 1.21.1**. NeoForge support is gone, not kept alongside; there is no Architectury layer.

The mod **compiles, builds a jar, and a dedicated server boots to `Done (…)!` with the mod loaded**.
Nothing has been played yet — no mechanic in here has been checked in game.

```sh
./gradlew build      # jar lands in build/libs/projecte-1.1.0.jar
./gradlew runServer  # headless dedicated server
./gradlew runClient  # client
```

Drop `build/libs/projecte-1.1.0.jar` in a 1.21.1 Fabric instance together with **Fabric API** and
**Forge Config API Port** (`forgeconfigapiport-fabric`, 21.1.6+). Those two are hard dependencies;
`fabric.mod.json` declares them.

## What to try first, in roughly this order

Everything below compiles but has never run, so expect the first session to be about crashes and
obviously-wrong behaviour rather than balance.

1. **Transmutation table**, learning items, and the EMC values themselves. EMC mapping runs on
   datapack reload/login; watch the log for mapper errors.
2. **Klein stars** charging and discharging, and the `full_star_*` recipes (see *Generated data*).
3. **Philosopher's stone** — both the in-world transmutation and its crafting-grid behaviour, which
   goes through two of the port's own mechanisms (a use-block event and a recipe-remainder mixin).
4. **Tools and armour**: mining modes, AOE, charge, the katar/sword sweep, armour damage reduction,
   step assist on the gem boots, flight from the swiftwolf ring and Arcana.
5. **Alchemical bags and chests**, including hopper and pipe interaction both ways.
6. **Collectors, relays, condensers** — the EMC machines, and their hopper interaction.
7. **Nova catalyst/cataclysm**: redstone, flint and steel, a flaming arrow, and **fire spreading into
   them** all have to prime ProjectE's own entity, not vanilla TNT (see *Mixins*).
8. **Shears and katar shearing** — drops should be doubled, and they now land next to the sheared mob
   rather than at the player.

## Known behaviour differences from the NeoForge build

These are deliberate, each because Fabric has no counterpart for what NeoForge offered. Worth
knowing before filing any of them as bugs:

- **Block placement can no longer be vetoed.** NeoForge let other mods cancel a placement; claim and
  protection mods will not stop ProjectE placing blocks.
- **Flight** from the swiftwolf ring and Arcana comes from a per-tick check of the hotbar and bag,
  not an attribute. Carried in a non-hotbar slot it will not grant flight.
- **Armour full-set reduction** zeroes the damage rather than cancelling the hit outright.
- **Sheared drops** land where vanilla puts them (next to the mob), not at the player's feet.
- **Permissions** are vanilla op levels; there are no permission nodes. (`fabric-permissions-api`
  could not be fetched here — `maven.lucko.me` is blocked by this environment's egress proxy — and
  the version on nucleoid's maven declares 1.21.9.)
- **Helmet and boots keybinds share X**, told apart by whether shift is held. The controls screen
  will list them as conflicting; they still work.
- **No in-game config screen.** ModMenu would be the Fabric equivalent and is not wired up.
- **Modded tillable blocks** are not recognised by the hoe AOE. Vanilla's tilling is an action rather
  than a table, so `ToolActions.TILLABLES` restates vanilla's own list.
- **No extra brewing recipes.** NeoForge had a registry mods could add brewing recipes to, which EMC
  mapping read; only vanilla's mixes are mapped now.
- **The pedestal's effect range is no longer drawn** as a debug box with F3+B.
- **Trinkets is not wired up yet** — `IntegrationHelper.registerAccessoryCapability` is a no-op, so a
  ring in a Trinkets slot is not seen.
- **Igniting a flammable non-TNT block** where fire cannot be placed now returns PASS rather than
  consuming the action. On NeoForge it consumed the action and did nothing visible.

## Still to do

- **JEI** (the one the mod's users actually want) and **Jade**. Both integrations are still in the
  tree, untouched, and are excluded from compilation by two lines in `build.gradle` — search for
  `Fabric port` there. They are entered only through JEI's and Jade's own plugin discovery, so
  nothing in the core references them. Bringing them back means porting
  `integration/recipe_viewer/**` (written against NeoForge-era JEI) and `integration/jade/**`.
- **Trinkets**: `integration/IntegrationHelper` and `integration/IExposesAccessoryAttributes` are the
  seams. Trinkets 3.10.0 is already declared in `gradle.properties`.
- **Datagen** (`src/datagen`, 28 files) is not in the build. It is written against NeoForge's data
  providers. Note that when it comes back it must emit **Fabric's** JSON for conditions and component
  ingredients, not NeoForge's — see *Generated data*, whose output was converted by hand.
- **Unit tests** (`src/test`) are held out of the build by `java.srcDirs = []` in `build.gradle`.
  They stand up an ephemeral server through NeoForge's test framework, which has no Fabric
  counterpart. The EMC maths tests are worth rescuing.
- **1.20.1** was mentioned as a maybe. Nothing here is written with it in mind.

## How the port is put together

Knowing these five things explains most of the diff.

### Mappings

`officialMojangMappings()` layered with Parchment, **not Yarn**. Every one of the ~46k vanilla
references in the mod is therefore unchanged from upstream, which keeps the diff readable and makes
merging future upstream work possible. Do not switch to Yarn casually.

### Registration order is load-bearing

Fabric registers an entry the moment you ask, where NeoForge deferred everything. `PECore` therefore
registers in a specific order — blocks, then block entities and items, then creative tabs last,
because a tab names the items it shows. `gameObjs/registration/DeferredRegister` collects suppliers
and creates and binds them in one `register()` pass to keep the mod's static-field style working.

### Item handlers

NeoForge's slot-indexed `IItemHandler` contract is kept (`src/api/java/.../api/inventory/`) because
~95 call sites in the mod are slot-indexed, while Fabric's Transfer API is transaction-based with no
stable slot index. `capability/Capabilities#init` bridges the two **in both directions**, so
ProjectE's tools read vanilla chests and hoppers read ProjectE's machines.

### Fluid amounts stay in millibuckets

`api/fluid/FluidStack` keeps amounts in millibuckets (1000/bucket) and converts to droplets
(81000/bucket) only where Fabric's fluid storage is actually touched. This means **no EMC value or
conversion file needed rescaling**.

### Access widener, not access transformer

`src/main/resources/projecte.accesswidener`. Every entry has a comment saying what needs it. Vanilla
descriptors in it were checked with `javap` against the real Mojang-mapped jar, and
`./gradlew validateAccessWidener` runs as part of `build`.

### Mixins

ProjectE had **zero** mixins on NeoForge; the port has 13. Each replaces a NeoForge hook or event,
and each has a class comment saying which. The ones most worth knowing:

| Mixin | Replaces |
| --- | --- |
| `TntBlockMixin`, `FireBlockMixin` | `ProjectETNT#onCaughtFire` — every ignition path must prime ProjectE's own entity. `FireBlockMixin` is separate because vanilla clears the block before priming it, so the block has to come from the state fire captured. |
| `RecipeMixin` | stack-sensitive crafting remainders, so the stone and the amulets come back out of the grid with their data. |
| `EnchantmentMixin` | the four per-item "no enchantments" hooks. Vanilla's own `isEnchantable` only covers the enchanting table. |
| `EntityShearDropMixin` | NeoForge's shearing contract handed the drops back; vanilla spawns them, so they are caught on the way out and doubled. |
| `ItemStackAttributesMixin` | the event NeoForge fired while gathering a stack's attribute modifiers (charge-scaled damage, step assist). |
| `PlayerSweepMixin` | `getSweepHitBox` — the charged sweep of the swords and katar. |
| `PlayerAttachmentsMixin`, `PlayerOfflineCacheMixin` | NeoForge's data attachments, for knowledge and bags. Saved NBT lives under `projecte:attachments`, the layout the offline-knowledge code already expected. |
| `ItemInHandRendererMixin` (client) | `shouldCauseReequipAnimation`, so the arm does not bob every time stored EMC ticks. |

### Client-only code

Fabric refuses to load a client-only class on a dedicated server, and it checks a class as a whole:
**one mention of `Minecraft` in a class the server also loads brings the server down** as that class
links. Everything common goes through `client/ClientAccess`, which is the only place in common code
that touches `Minecraft`. This already caused one crash during bring-up; if a server crash mentions
"Cannot load class net.minecraft.client…", this is why, and `ClientAccess` is where the fix goes.

### Generated data

`src/datagen/generated` ships in the jar. 24 of those files carried NeoForge JSON and were converted
by hand:

- `neoforge:conditions` → `fabric:load_conditions`, and each condition's `type` key → `condition`
  (Fabric's key), with `neoforge:not` → `fabric:not`.
- `neoforge:components` ingredients → `fabric:components`, keeping only `projecte:stored_emc` — the
  component that actually says a klein star is full. The rest of what NeoForge's datagen wrote were
  the item's own defaults.

**The `full_star_*` and `tome*` recipes are the ones to check in game**, because they are the only
data in the mod whose meaning was re-expressed rather than copied.

## Deliberately not done

- Nothing was skipped, disabled or stubbed to make the build pass, with two exceptions, both marked
  with `TODO - Fabric port` in `build.gradle` and both listed above: the JEI/Jade sources and the
  unit tests.
- No EMC value, recipe, or piece of balance was changed.

# Levels and tiers

Ten levels, five tiers, two levels per tier except the first (which has three).

| Tier | Level | Name | Days |
| --- | --- | --- | --- |
| **I · The Struggle** | 1 | Fresh Spawn | 0 |
| | 2 | Still Standing | 1 |
| | 3 | The Determined | 3 |
| **II · Getting Serious** | 4 | Week Warrior | 7 |
| | 5 | Monk in Training | 14 |
| **III · Built Different** | 6 | Certified Monk | 30 |
| | 7 | Bonk Resistant | 60 |
| **IV · Questionably Powerful** | 8 | Grass Toucher | 90 |
| | 9 | Beyond Human | 180 |
| **V · Final Form** | 10 | The Bonkening | 365 |

## The model

`data/Levels.kt`:

```kotlin
enum class Tier(val title: String, val color: Long)
data class Level(val number: Int, val name: String, val days: Int, val tier: Tier)
```

The property is called `title` and not `name` because `Enum.name` already exists and cannot
be overridden.

- `levelFor(days)` returns the highest level a streak of that length has reached.
- `nextLevel(after)` returns the one after it, or null at the top.
- `levelsIn(tier)` returns the levels inside a tier.

The tiers hold 3/2/2/2/1 levels.

## The progress bar

The bar on the Levels page is driven by the streak's **elapsed time**, not by the whole-day
count, so it creeps forward second by second rather than jumping once a day:

```
progress = (elapsedDays - currentLevel.days) / (nextLevel.days - currentLevel.days)
```

The level itself is still measured in whole days, which is what the tiers mean.

## The art

Each level has a picture: one character per level, drawn for the app. The files are in
`app/src/main/assets/levels/`, named after the level — `1.png` is level 1.

They are 2048 px squares, which is far more than the app draws them at, so `ui/LevelArt.kt`
decodes each one down to 384 px and keeps it, one per level, for as long as the app runs.
Ten of them are about six megabytes.

The art stands in for the tiers: the tiers have no picture of their own. A level's picture
appears on the home screen, on its card in the history, and on its row in the Levels page,
where it is dimmed to 30% until the level is reached.

The tier colours are still used, for the progress bar. They run from dim to bright as you
climb:

| Tier | Colour |
| --- | --- |
| The Struggle | `#8A8F98` grey |
| Getting Serious | `#7FA0C4` steel |
| Built Different | `#8FB9A0` sage |
| Questionably Powerful | `#C9AE7E` bronze |
| Final Form | `#E8D08A` gold |

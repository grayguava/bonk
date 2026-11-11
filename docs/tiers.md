# Levels and tiers

Ten levels, five tiers, two levels per tier except the first (which has three).

| Tier | Level | Name | Days |
| --- | --- | --- | --- |
| **I · Awakening** | 1 | Fresh Start | 0 |
| | 2 | Beginner | 1 |
| | 3 | Initiate | 3 |
| **II · Building** | 4 | Week One | 7 |
| | 5 | Disciplined | 14 |
| **III · Strength** | 6 | Consistent | 30 |
| | 7 | Resilient | 60 |
| **IV · Mastery** | 8 | Dedicated | 90 |
| | 9 | Unshaken | 180 |
| **V · Transcendence** | 10 | Year One | 365 |

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

The tier grouping follows the original table literally: level 3 (Initiate) had an empty tier
cell in the source table, so it continues Tier I. That gives the tiers 3/2/2/2/1 levels.

## The progress bar

The bar on the Levels page is driven by the streak's **elapsed time**, not by the whole-day
count, so it creeps forward second by second rather than jumping once a day:

```
progress = (elapsedDays - currentLevel.days) / (nextLevel.days - currentLevel.days)
```

The level itself is still measured in whole days — that is what the tiers mean.

## The badges

Each tier has its own mark, drawn as a vector path in `LevelsScreen.kt` rather than shipped
as an icon. The marks are:

| Tier | Mark |
| --- | --- |
| Awakening | a sapling — a stem and two leaves |
| Building | three blocks, each taller than the last |
| Strength | a mountain |
| Mastery | a cut gem |
| Transcendence | an eight-point star |

They are drawn in the tier's colour, dimmed to 28% alpha when the tier has not been
reached. The colours run from dim to bright as you climb:

| Tier | Colour |
| --- | --- |
| Awakening | `#8A8F98` grey |
| Building | `#7FA0C4` steel |
| Strength | `#8FB9A0` sage |
| Mastery | `#C9AE7E` bronze |
| Transcendence | `#E8D08A` gold |

The same marks appear larger in the tier avatar on the home screen, inside a thin round ring.

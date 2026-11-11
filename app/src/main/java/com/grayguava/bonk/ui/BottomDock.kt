package com.grayguava.bonk.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** One destination: its name, its outline icon, and the filled one shown while it's open. */
class DockTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector = icon)

/** How the indicator's two edges travel when the selection moves. */
enum class DockLensStyle { STRETCH, JELLY }

/** The dock's height, and the width each destination gets. */
private val DOCK_HEIGHT = 67.dp
private val DOCK_SLOT = 96.dp
private val DOCK_INSET = 6.dp
private val DOCK_SHAPE = RoundedCornerShape(percent = 50)
private val LENS_SHAPE = RoundedCornerShape(27.dp)

/** The lens's fill. */
private val LENS_TOP = Color.White.copy(alpha = 0.16f)
private val LENS_BOTTOM = Color.White.copy(alpha = 0.07f)

/**
 * The dock: one pill holding every destination, with the open one sitting in a lens
 * that stretches toward it as it travels.
 */
@Composable
fun BottomDock(
    tabs: List<DockTab>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    lensStyle: DockLensStyle = DockLensStyle.JELLY,
) {
    val grain = rememberFrostGrain()
    Box(
        modifier
            .width(DOCK_SLOT * tabs.size + DOCK_INSET * 2)
            .height(DOCK_HEIGHT)
            .glassShadow(DOCK_SHAPE)
            .background(GlassFilm, DOCK_SHAPE)
            .border(1.dp, Brush.verticalGradient(listOf(GlassRimTop, GlassRimBottom, GlassRimTop)), DOCK_SHAPE)
            .clip(DOCK_SHAPE)
            .drawBehind { drawRect(grain) },
    ) {
        val (left, right) = rememberStretchingLens(selected, DOCK_SLOT, lensStyle)
        Box(
            Modifier
                .offset { IntOffset((DOCK_INSET + left()).roundToPx(), 0) }
                .layout { measurable, constraints ->
                    val w = (right() - left()).roundToPx().coerceAtLeast(0)
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
                .fillMaxHeight()
                .padding(vertical = DOCK_INSET)
                .background(Brush.verticalGradient(listOf(LENS_TOP, LENS_BOTTOM)), LENS_SHAPE)
                .border(1.dp, Brush.verticalGradient(listOf(GlassRimTop, GlassRimBottom)), LENS_SHAPE),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = DOCK_INSET)
        ) {
            tabs.forEachIndexed { index, tab ->
                DockItem(
                    tab = tab,
                    selected = index == selected,
                    modifier = Modifier
                        .width(DOCK_SLOT)
                        .fillMaxHeight(),
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

/** One destination: the icon, filled and lifted a little when open, over its name. */
@Composable
private fun DockItem(
    tab: DockTab,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.9f
            selected -> 1.06f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 320f),
        label = "dockScale",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.62f),
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "dockFg",
    )

    Column(
        modifier
            .clip(LENS_SHAPE)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .semantics { role = Role.Tab; this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Crossfade(
            targetState = selected,
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            label = "dockIcon",
        ) { on ->
            Icon(
                imageVector = if (on) tab.selectedIcon else tab.icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            )
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/**
 * The lens's two edges, which travel separately: the edge in the direction of
 * travel leads and the far edge follows, so the lens stretches toward the new
 * tab and settles into it rather than sliding over as one block.
 */
@Composable
private fun rememberStretchingLens(index: Int, slot: Dp, style: DockLensStyle): Pair<() -> Dp, () -> Dp> {
    val left = remember { Animatable(slot.value * index) }
    val right = remember { Animatable(slot.value * (index + 1)) }
    LaunchedEffect(index, slot) {
        val toLeft = slot.value * index
        val toRight = slot.value * (index + 1)
        val movingRight = toLeft > left.value
        val (lead, follow) = lensSprings(style)
        launch { left.animateTo(toLeft, if (movingRight) follow else lead) }
        launch { right.animateTo(toRight, if (movingRight) lead else follow) }
    }
    return ({ left.value.dp } to { right.value.dp })
}

/** The springs for the lens's leading and trailing edges, for each [DockLensStyle]. */
private fun lensSprings(style: DockLensStyle): Pair<SpringSpec<Float>, SpringSpec<Float>> = when (style) {
    DockLensStyle.STRETCH ->
        spring<Float>(dampingRatio = 1f, stiffness = 520f) to spring(dampingRatio = 1f, stiffness = 170f)
    // Underdamped edges: the front overshoots, the back catches up late and both wobble home.
    DockLensStyle.JELLY ->
        spring<Float>(dampingRatio = 0.5f, stiffness = 480f) to spring(dampingRatio = 0.55f, stiffness = 190f)
}


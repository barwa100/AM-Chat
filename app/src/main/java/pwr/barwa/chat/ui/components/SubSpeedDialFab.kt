package de.charlex.compose

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Place this component in the scaffold FloatingActionButton slot
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : FloatingActionButtonItem> SubSpeedDialFloatingActionButtons(
    items: List<T>,
    showLabels: Boolean = true,
    state: SpeedDialFloatingActionButtonState,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    modifier: Modifier,
    labelContent: @Composable (T) -> Unit = {
        val backgroundColor = MaterialTheme.colorScheme.primaryContainer
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(12.0.dp),
            shadowElevation = 2.dp,
            onClick = { it.onFabItemClicked() }
        ) {
            Text(
                text = it.label,
                color = contentColorFor(backgroundColor = backgroundColor),
                modifier = Modifier
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    },
    fabContent: @Composable (T) -> Unit = {
        SmallFloatingActionButton(
            modifier = Modifier
                .padding(4.dp),
            onClick = { it.onFabItemClicked() },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 2.dp,
                hoveredElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = it.icon,
                contentDescription = it.label
            )
        }
    }
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = verticalArrangement,
        modifier = modifier
    ) {
        items.forEach { item ->
            AnimatedSmallFloatingActionButtonWithLabel(
                state = state,
                showLabel = showLabels,
                labelContent = {
                    labelContent(item)
                },
                fabContent = {
                    fabContent(item)
                }
            )
        }
    }
}


@Composable
internal fun AnimatedSmallFloatingActionButtonWithLabel(
    modifier: Modifier = Modifier,
    showLabel: Boolean,
    labelContent: @Composable () -> Unit,
    fabContent: @Composable () -> Unit,
    state: SpeedDialFloatingActionButtonState
) {
    val alpha: State<Float>? = state.transition?.animateFloat(
        transitionSpec = {
            tween(durationMillis = 50)
        },
        label = "",
        targetValueByState = {
            if (it == SpeedDialState.EXPANDED) 1f else 0f
        }
    )
    val scale: State<Float>? = state.transition?.animateFloat(
        label = "",
        targetValueByState = {
            if (it == SpeedDialState.EXPANDED) 1.0f else 0f
        }
    )
    Row(
        modifier = modifier
            .alpha(animateFloatAsState((alpha?.value ?: 0f)).value)
            .scale(animateFloatAsState(targetValue = scale?.value ?: 0f).value),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showLabel) {
            labelContent.invoke()
        }
        fabContent.invoke()
    }
}


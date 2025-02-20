package no.nordicsemi.android.blinky.ui.control.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.blinky.ui.R

@Composable
internal fun LedControlView(
    state1: Boolean,
    onState1Changed: (Boolean) -> Unit,
    state2: Boolean,
    onState2Changed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedCard(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .clickable { onState1Changed(!state1) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = stringResource(R.string.blinky_led1),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.blinky_led1_descr),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = state1, onCheckedChange = onState1Changed)
                }
            }
        }

        OutlinedCard(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .clickable { onState2Changed(!state2) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = stringResource(R.string.blinky_led2),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.blinky_led2_descr),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = state2, onCheckedChange = onState2Changed)
                }
            }
        }
    }
}

@Composable
@Preview
private fun LecControlViewPreview() {
    LedControlView(
        state1 = true,
        onState1Changed = {},
        state2 = true,
        onState2Changed = {},
        modifier = Modifier.padding(16.dp),
    )
}
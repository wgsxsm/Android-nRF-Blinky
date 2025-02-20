package no.nordicsemi.android.blinky.ui.control.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun BlinkyControlView(
    led1State: Boolean,
    led2State: Boolean,
    button1State: Boolean,
    button2State: Boolean,
    onState1Changed: (Boolean) -> Unit,
    onState2Changed: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LedControlView(
            state1 = led1State,
            onState1Changed = onState1Changed,
            state2 = led2State,
            onState2Changed = onState2Changed,
        )

        ButtonControlView(
            state1 = button1State,
            state2 = button2State,
        )
    }
}

@Preview
@Composable
private fun BlinkyControlViewPreview() {
    BlinkyControlView(
        led1State = true,
        led2State = true,
        button1State = true,
        button2State = true,
        onState1Changed = {},
        onState2Changed = {},
        modifier = Modifier.padding(16.dp),
    )
}
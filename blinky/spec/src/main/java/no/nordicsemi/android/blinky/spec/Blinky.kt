package no.nordicsemi.android.blinky.spec

import kotlinx.coroutines.flow.StateFlow

interface Blinky {

    enum class State {
        LOADING,
        READY,
        NOT_AVAILABLE
    }

    /**
     * Connects to the device.
     */
    suspend fun connect()

    /**
     * Disconnects from the device.
     */
    fun release()

    /**
     * The current state of the blinky.
     */
    val state: StateFlow<State>

    /**
     * The current state of the LED1.
     */
    val led1State: StateFlow<Boolean>
    /**
     * The current state of the LED2.
     */
    val led2State: StateFlow<Boolean>

    /**
     * The current state of the button1.
     */
    val button1State: StateFlow<Boolean>
    /**
     * The current state of the button2.
     */
    val button2State: StateFlow<Boolean>

    /**
     * Controls the LED1 state.
     *
     * @param state the new state of the LED1.
     */
    suspend fun turnLed1(state: Boolean)
    /**
     * Controls the LED2 state.
     *
     * @param state the new state of the LED.
     */
    suspend fun turnLed2(state: Boolean)
}
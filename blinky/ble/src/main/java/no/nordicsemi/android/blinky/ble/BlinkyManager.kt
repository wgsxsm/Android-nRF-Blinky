package no.nordicsemi.android.blinky.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.blinky.ble.data.ButtonCallback
import no.nordicsemi.android.blinky.ble.data.ButtonState
import no.nordicsemi.android.blinky.ble.data.LedCallback
import no.nordicsemi.android.blinky.ble.data.LedData
import no.nordicsemi.android.blinky.spec.Blinky
import no.nordicsemi.android.blinky.spec.BlinkySpec
import timber.log.Timber

class BlinkyManager(
    context: Context,
    device: BluetoothDevice
): Blinky by BlinkyManagerImpl(context, device)

private class BlinkyManagerImpl(
    context: Context,
    private val device: BluetoothDevice,
): BleManager(context), Blinky {
    private val scope = CoroutineScope(Dispatchers.IO)

    private var led1Characteristic: BluetoothGattCharacteristic? = null
    private var led2Characteristic: BluetoothGattCharacteristic? = null
    private var button1Characteristic: BluetoothGattCharacteristic? = null
    private var button2Characteristic: BluetoothGattCharacteristic? = null

    private val _led1State = MutableStateFlow(false)
    override val led1State = _led1State.asStateFlow()
    private val _led2State = MutableStateFlow(false)
    override val led2State = _led2State.asStateFlow()

    private val _button1State = MutableStateFlow(false)
    override val button1State = _button1State.asStateFlow()
    private val _button2State = MutableStateFlow(false)
    override val button2State = _button2State.asStateFlow()

    override val state = stateAsFlow()
        .map {
            when (it) {
                is ConnectionState.Connecting,
                is ConnectionState.Initializing -> Blinky.State.LOADING
                is ConnectionState.Ready -> Blinky.State.READY
                is ConnectionState.Disconnecting,
                is ConnectionState.Disconnected -> Blinky.State.NOT_AVAILABLE
            }
        }
        .stateIn(scope, SharingStarted.Lazily, Blinky.State.NOT_AVAILABLE)


    private val button1Callback by lazy {
        object : ButtonCallback() {
            override fun onButtonStateChanged(device: BluetoothDevice, state: Boolean) {
                _button1State.tryEmit(state)
            }
        }
    }
    private val button2Callback by lazy {
        object : ButtonCallback() {
            override fun onButtonStateChanged(device: BluetoothDevice, state: Boolean) {
                _button2State.tryEmit(state)
            }
        }
    }

    private val led1Callback by lazy {
        object : LedCallback() {
            override fun onLedStateChanged(device: BluetoothDevice, state: Boolean) {
                _led1State.tryEmit(state)
            }
        }
    }
    private val led2Callback by lazy {
        object : LedCallback() {
            override fun onLedStateChanged(device: BluetoothDevice, state: Boolean) {
                _led2State.tryEmit(state)
            }
        }
    }

    override suspend fun connect() = connect(device)
        .retry(3, 300)
        .useAutoConnect(false)
        .timeout(3000)
        .suspend()

    override fun release() {
        // Cancel all coroutines.
        scope.cancel()

        val wasConnected = isReady
        // If the device wasn't connected, it means that ConnectRequest was still pending.
        // Cancelling queue will initiate disconnecting automatically.
        cancelQueue()

        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    override suspend fun turnLed1(state: Boolean) {
        // Write the value to the characteristic.
        writeCharacteristic(
            led1Characteristic,
            LedData.from(state),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()

        // Update the state flow with the new value.
        _led1State.value = state
    }
    override suspend fun turnLed2(state: Boolean) {
        // Write the value to the characteristic.
        writeCharacteristic(
            led2Characteristic,
            LedData.from(state),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).suspend()

        // Update the state flow with the new value.
        _led2State.value = state
    }

    override fun log(priority: Int, message: String) {
        Timber.log(priority, message)
    }

    override fun getMinLogPriority(): Int {
        // By default, the library logs only INFO or
        // higher priority messages. You may change it here.
        return Log.VERBOSE
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        // Get the LBS Service from the gatt object.
        gatt.getService(BlinkySpec.BLINKY_SERVICE_UUID)?.apply {
            // Get the LED1 characteristic.
            led1Characteristic = getCharacteristic(
                BlinkySpec.BLINKY_LED_CHARACTERISTIC_UUID,
                // Mind, that below we pass required properties.
                // If your implementation supports only WRITE_NO_RESPONSE,
                // change the property to BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE.
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            // Get the LED2 characteristic.
            led2Characteristic = getCharacteristic(
                BlinkySpec.BLINKY_LED_CHARACTERISTIC_UUID,
                // Mind, that below we pass required properties.
                // If your implementation supports only WRITE_NO_RESPONSE,
                // change the property to BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE.
                BluetoothGattCharacteristic.PROPERTY_WRITE
            )
            // Get the Button1 characteristic.
            button1Characteristic = getCharacteristic(
                BlinkySpec.BLINKY_BUTTON_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY
            )
            // Get the Button2 characteristic.
            button2Characteristic = getCharacteristic(
                BlinkySpec.BLINKY_BUTTON_CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY
            )

            // Return true if all required characteristics are supported.
            return led1Characteristic != null && button1Characteristic != null
                    && led2Characteristic != null && button2Characteristic != null
        }
        return false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        // Enable notifications for the button characteristic.
        val flow1: Flow<ButtonState> = setNotificationCallback(button1Characteristic)
            .asValidResponseFlow()

        // Forward the button state to the buttonState flow.
        scope.launch {
            flow1.map { it.state }.collect { _button1State.tryEmit(it) }
        }

        enableNotifications(button1Characteristic)
            .enqueue()

        // Read the initial value of the button characteristic.
        readCharacteristic(button1Characteristic)
            .with(button1Callback)
            .enqueue()

        // Read the initial value of the LED characteristic.
        readCharacteristic(led1Characteristic)
            .with(led1Callback)
            .enqueue()

        // Enable notifications for the button characteristic.
        val flow2: Flow<ButtonState> = setNotificationCallback(button2Characteristic)
            .asValidResponseFlow()

        // Forward the button state to the buttonState flow.
        scope.launch {
            flow2.map { it.state }.collect { _button2State.tryEmit(it) }
        }

        enableNotifications(button2Characteristic)
            .enqueue()

        // Read the initial value of the button characteristic.
        readCharacteristic(button2Characteristic)
            .with(button2Callback)
            .enqueue()

        // Read the initial value of the LED characteristic.
        readCharacteristic(led2Characteristic)
            .with(led2Callback)
            .enqueue()
    }

    override fun onServicesInvalidated() {
        led1Characteristic = null
        led2Characteristic = null
        button1Characteristic = null
        button2Characteristic = null
    }
}
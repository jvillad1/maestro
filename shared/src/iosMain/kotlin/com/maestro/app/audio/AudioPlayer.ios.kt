package com.maestro.app.audio

import platform.AudioToolbox.AudioServicesPlaySystemSound

actual class AudioPlayer actual constructor() {
    actual fun playClick(isAccent: Boolean) {
        // System tick sounds: 1057 (tink) for accents, 1104 (keyboard tap) otherwise
        AudioServicesPlaySystemSound(if (isAccent) 1057u else 1104u)
    }
}

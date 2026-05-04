package com.maestro.app.audio

import kotlin.js.ExperimentalWasmJsInterop

private external interface JsAudioContext {
    val currentTime: Double
    val destination: JsAudioNode
    fun createOscillator(): JsOscillatorNode
    fun createGain(): JsGainNode
}

private external interface JsAudioNode

private external interface JsOscillatorNode : JsAudioNode {
    val frequency: JsAudioParam
    fun connect(destination: JsAudioNode)
    fun start(time: Double)
    fun stop(time: Double)
}

private external interface JsGainNode : JsAudioNode {
    val gain: JsAudioParam
    fun connect(destination: JsAudioNode)
}

private external interface JsAudioParam {
    fun setValueAtTime(value: Double, startTime: Double): JsAudioParam
    fun exponentialRampToValueAtTime(value: Double, endTime: Double): JsAudioParam
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun createAudioContext(): JsAudioContext =
    js("new (window.AudioContext || window.webkitAudioContext)()")

actual class AudioPlayer actual constructor() {
    actual fun playClick(isAccent: Boolean) {
        try {
            val ctx = createAudioContext()
            val osc = ctx.createOscillator()
            val gainNode = ctx.createGain()
            osc.connect(gainNode)
            gainNode.connect(ctx.destination)
            osc.frequency.setValueAtTime(if (isAccent) 1200.0 else 800.0, ctx.currentTime)
            gainNode.gain.setValueAtTime(if (isAccent) 0.4 else 0.25, ctx.currentTime)
            gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08)
            osc.start(ctx.currentTime)
            osc.stop(ctx.currentTime + 0.08)
        } catch (e: Exception) { }
    }
}

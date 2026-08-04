package com.maestro.app.network

import kotlinx.browser.window

// The server serves the wasm app itself, so the API is always same-origin.
actual fun apiBaseUrl(): String = window.location.origin

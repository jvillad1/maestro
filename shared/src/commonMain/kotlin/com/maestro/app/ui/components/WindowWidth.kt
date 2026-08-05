package com.maestro.app.ui.components

import androidx.compose.runtime.compositionLocalOf

/**
 * Window width class provided by AppScaffold from the real container width.
 * Compact (< 840dp): phone layout — bottom bar, stacked sections.
 * Expanded: desktop/tablet layout — sidebar, multi-column body.
 */
enum class WindowWidthClass { Compact, Expanded }

val LocalWindowWidthClass = compositionLocalOf { WindowWidthClass.Expanded }

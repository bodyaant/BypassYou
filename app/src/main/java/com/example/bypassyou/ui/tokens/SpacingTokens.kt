package com.example.bypassyou.ui.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Expressive Spacing Tokens.
 * Based strictly on the 4dp / 8dp spatial grid specification (m3.material.io).
 */
object SpacingTokens {
    val None: Dp = 0.dp

    val Space25: Dp = 2.dp      // 0.25x
    val Space50: Dp = 4.dp      // 0.5x
    val Space75: Dp = 6.dp      // 0.75x
    val Space100: Dp = 8.dp     // 1.0x Baseline grid unit
    val Space125: Dp = 10.dp    // 1.25x
    val Space150: Dp = 12.dp    // 1.5x
    val Space175: Dp = 14.dp    // 1.75x
    val Space200: Dp = 16.dp    // 2.0x
    val Space250: Dp = 20.dp    // 2.5x
    val Space300: Dp = 24.dp    // 3.0x
    val Space400: Dp = 32.dp    // 4.0x
    val Space450: Dp = 36.dp    // 4.5x
    val Space500: Dp = 40.dp    // 5.0x
    val Space600: Dp = 48.dp    // 6.0x
    val Space700: Dp = 56.dp    // 7.0x
    val Space800: Dp = 64.dp    // 8.0x
    val Space900: Dp = 72.dp    // 9.0x
    val Space1000: Dp = 80.dp   // 10.0x

    // Semantic Aliases
    val ExtraSmall: Dp = Space50
    val Small: Dp = Space100
    val MediumSmall: Dp = Space150
    val Medium: Dp = Space200
    val Large: Dp = Space300
    val ExtraLarge: Dp = Space400
    val Huge: Dp = Space600
    val Massive: Dp = Space800
}

/**
 * Semantic layout spacing aliases following AOSP Settings and Pixel System UI standards.
 */
object SystemLayoutTokens {
    /** Standard margin from screen edges on compact window sizes (phones) */
    val ScreenHorizontalPadding: Dp = SpacingTokens.Medium

    /** Vertical spacing between clustered preference / section cards */
    val CardGroupVerticalSpacing: Dp = SpacingTokens.MediumSmall

    /** Internal padding inside a card / preference tile */
    val CardInternalPadding: Dp = SpacingTokens.Medium

    /** Spacing between leading icon container and title / content */
    val IconToContentSpacing: Dp = SpacingTokens.Medium

    /** Standard container size for tile icons (40x40 dp) */
    val IconContainerSize: Dp = 40.dp

    /** Standard icon glyph size (24x24 dp) */
    val IconSize: Dp = 24.dp

    /**
     * Start indent for list dividers when a leading icon is present.
     * (16dp padding + 40dp icon container = 56dp).
     */
    val DividerIndentWithIcon: Dp = 56.dp

    /** Top padding before a section header title */
    val SectionHeaderPaddingTop: Dp = SpacingTokens.Large

    /** Bottom padding after a section header title before the first card */
    val SectionHeaderPaddingBottom: Dp = SpacingTokens.Small
}

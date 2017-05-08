package klay.core

import react.RFuture

/**
 * Provides a [Tile], potentially asynchronously. This provides a uniform API for obtaining a
 * [Texture] or [Tile] directly from an instance thereof, or from an [Image]
 * which provides the [Texture] or an `Image.Region (TODO)` which provides the
 * [Tile].
 */
abstract class TileSource {

    /** Returns whether this tile source is loaded and ready to provide its tile.  */
    abstract val isLoaded: Boolean

    /** Returns the tile provided by this source.
     * @throws IllegalStateException if `!isLoaded()`.
     */
    abstract fun tile(): Tile

    /** Delivers the tile provided by this source once the source is loaded.  */
    abstract fun tileAsync(): RFuture<Tile>
}

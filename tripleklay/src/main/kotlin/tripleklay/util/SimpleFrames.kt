package tripleklay.util

import klay.core.Tile
import klay.core.TileSource
import klay.scene.ImageLayer
import pythagoras.f.IPoint
import pythagoras.f.IRectangle
import pythagoras.f.Points
import pythagoras.f.Rectangle
import react.Slot

/**
 * A simple implementation of [Frames] that uses an untrimmed horizontal strip image.
 */
class SimpleFrames
/**
 * Creates an instance with the supplied tile source. The tile is assumed to contain `count` frames, each `width x height` in size, in row major order (any missing frames
 * are on the right side of the bottom row).
 * @param width the width of each frame.
 * *
 * @param height the width of each frame.
 */
(source: TileSource, protected val _width: Float, protected val _height: Float, protected val _count: Int) : Frames {

    private var _tile: Tile? = null

    /**
     * Creates an instance with the supplied source texture. The image is assumed to contain a
     * complete sheet of frames, each `width x height` in size.
     * @param width the width of each frame.
     * *
     * @param height the width of each frame.
     */
    constructor(source: Tile, width: Float, height: Float = source.height) : this(source, width, height, (source.height / height) as Int * (source.height / width) as Int) {}

    init {
        if (source.isLoaded)
            _tile = source.tile()
        else
            source.tileAsync().onSuccess(object : Slot<Tile> {
                override fun invoke(tile: Tile) {
                    _tile = tile
                }
            })
    }

    override fun width(): Float {
        return _width
    }

    override fun height(): Float {
        return _height
    }

    override fun count(): Int {
        return _count
    }

    override fun bounds(index: Int): IRectangle {
        return bounds(index, Rectangle())
    }

    override fun offset(index: Int): IPoint {
        return Points.ZERO
    } // we have no offsets

    override fun apply(index: Int, layer: ImageLayer) {
        if (_tile != null) {
            layer.setTile(_tile)
            layer.setTranslation(0f, 0f)
            var r = layer.region
            if (r == null) {
                layer.region = Rectangle()
                r = layer.region
            }
            bounds(index, r!!)
        }
    }

    protected fun cols(): Int {
        return (_tile!!.width / _width).toInt()
    }

    protected fun bounds(index: Int, r: Rectangle): Rectangle {
        val cols = cols()
        val row = index % cols
        val col = index / cols
        r.x = _width * row
        r.y = _height * col
        r.width = _width
        r.height = _height
        return r
    }
}
/**
 * Creates an instance with the supplied source texture. The frames are assumed to be all in a
 * single row, thus the height of the image defines the height of the frame.
 * @param width the width of each frame.
 */

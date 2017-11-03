package tripleklay.util

import klay.core.*
import react.Slot

/**
 * A runtime texture packer.
 */
class TexturePacker {
    interface Renderer {
        fun render(surface: Surface, x: Float, y: Float, width: Float, height: Float)
    }

    /** Add an image to the packer.  */
    fun add(id: String, tile: Tile): TexturePacker {
        return addItem(TileItem(id, tile))
    }

    /** Add a lazily rendered region to the packer.
     * The renderer will be used to draw the region each time pack() is called.  */
    fun add(id: String, width: Float, height: Float, renderer: Renderer): TexturePacker {
        return addItem(RenderedItem(id, width, height, renderer))
    }

    /**
     * Pack all images into as few atlases as possible.
     * @return A map containing the new images, keyed by the id they were added with.
     */
    fun pack(gfx: Graphics, batch: QuadBatch): Map<String, Tile> {
        val unpacked = ArrayList(_items.values)
        // TODO(bruno): Experiment with different heuristics. Brute force calculate using multiple
        // different heuristics and use the best one?

        // Sort by perimeter (instead of area). It can be harder to fit long skinny
        // textures after the large square ones
        unpacked.sortedBy { o1 -> o1.width + o1.height }

        val atlases = ArrayList<Atlas>()
        while (!unpacked.isEmpty()) {
            atlases.add(createAtlas())

            // Try to pack each item into any atlas
            val it = unpacked.iterator()
            while (it.hasNext()) {
                val item = it.next()

                for (atlas in atlases) {
                    if (atlas.place(item)) {
                        it.remove()
                    }
                }
            }
        }

        val packed = HashMap<String, Tile>()
        for (atlas in atlases) {
            val root = atlas.root
            val atlasTex = TextureSurface(gfx, batch, root.width, root.height)
            atlasTex.begin()
            root.visitItems(object : Slot<Node> {
                override fun invoke(n: Node) {
                    // Draw the item to the atlas
                    n.item!!.draw(atlasTex, n.x, n.y)
                    // Record its region
                    packed.put(n.item!!.id, atlasTex.texture.tile(n.x, n.y, n.width, n.height))
                }
            })
            atlasTex.end()
        }
        return packed
    }

    protected fun createAtlas(): Atlas {
        // TODO(bruno): Be smarter about sizing
        return Atlas(MAX_SIZE, MAX_SIZE)
    }

    protected fun addItem(item: Item): TexturePacker {
        if (item.width + PADDING > MAX_SIZE || item.height + PADDING > MAX_SIZE) {
            throw RuntimeException("Item is too big to pack " + item)
        }
        _items.put(item.id, item)
        return this
    }

    protected abstract class Item(val id: String, val width: Float, val height: Float) {

        abstract fun draw(surface: Surface, x: Float, y: Float)

        override fun toString(): String {
            return "[id=" + id + ", size=" + width + "x" + height + "]"
        }
    }

    protected class TileItem(id: String, val tile: Tile) : Item(id, tile.width, tile.height) {

        override fun draw(surface: Surface, x: Float, y: Float) {
            surface.draw(tile, x, y)
        }
    }

    protected class RenderedItem(id: String, width: Float, height: Float, val renderer: Renderer) : Item(id, width, height) {

        override fun draw(surface: Surface, x: Float, y: Float) {
            renderer.render(surface, x, y, width, height)
        }
    }

    protected class Atlas(width: Int, height: Int) {
        val root: Node

        init {
            root = Node(0f, 0f, width.toFloat(), height.toFloat())
        }

        fun place(item: Item): Boolean {
            val node = root.search(item.width + PADDING, item.height + PADDING) ?: return false
            node.item = item
            return true
        }
    }

    protected class Node(
            /** The bounds of this node (and its children).  */
            val x: Float, val y: Float, val width: Float, val height: Float) {

        /** This node's two children, if any.  */
        var children: Pair<Node, Node>? = null

        /** The texture that is placed here, if any. Implies that this is a leaf node.  */
        var item: Item? = null

        /** Find a free node in this tree big enough to fit an area, or null.  */
        fun search(w: Float, h: Float): Node? {
            // There's already an item here, terminate
            if (item != null) return null

            // That'll never fit, terminate
            if (width < w || height < h) return null

            if (children != null) {
                var descendent = children!!.first.search(w, h)
                if (descendent == null) descendent = children!!.second.search(w, h)
                return descendent

            } else {
                // This node is a perfect size, no need to subdivide
                if (width == w && height == h) return this

                // Split into two children
                val dw = width - w
                val dh = height - h
                if (dw > dh) {
                    children = Pair(Node(x, y, w, height), Node(x + w, y, dw, height))
                } else {
                    children = Pair(Node(x, y, width, h), Node(x, y + h, width, dh))
                }
                return children!!.first.search(w, h)
            }
        }

        /** Iterate over all nodes with items in this tree.  */
        fun visitItems(slot: Slot<Node>) {
            if (item != null) slot.invoke(this)
            if (children != null) {
                children!!.first.visitItems(slot)
                children!!.second.visitItems(slot)
            }
        }
    }

    protected var _items: MutableMap<String, Item> = HashMap()

    companion object {

        protected val PADDING = 1
        protected val MAX_SIZE = 2048
    }
}

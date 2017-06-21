package tripleklay.ui

import pythagoras.f.IDimension
import tripleklay.util.DimensionValue

/**
 * A group that allows configuring its preferred size. The size is always returned when the size
 * of the group is calculated, but the group may end up being stretched when contained in a
 * layout that does so.
 */
class SizableGroup
/** Creates the sizable group with preferred width and height.  */
@JvmOverloads constructor(layout: Layout, wid: Float = 0f, hei: Float = 0f) : Group(layout) {
    /** The preferred size of this widget. Update at will.  */
    val preferredSize = DimensionValue(0f, 0f)

    /** Creates the sizable group with the given preferred size.  */
    constructor(layout: Layout, size: IDimension) : this(layout, size.width, size.height) {}

    init {
        preferredSize.update(wid, hei)
        preferredSize.connect(invalidateSlot())
    }

    override fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
        // use a sizable layout data with the usual layout and hybrid size
        return SizableLayoutData(super.createLayoutData(hintX, hintY), preferredSize.get())
    }
}
/** Creates the sizable group with preferred width and height of 0. Note that this will
 * cause the base layout preferred size to be used, if overridden.  */

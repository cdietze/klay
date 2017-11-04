package tripleklay.ui

import kotlin.reflect.KClass

/**
 * A grouping element that contains other elements and lays them out according to a layout policy.
 */
open class Group : Elements<Group> {
    /**
     * Creates a group with the specified layout and styles.
     */
    constructor(layout: Layout, styles: Styles) : super(layout) {
        setStyles(styles)
    }

    /**
     * Creates a group with the specified layout and styles (in the DEFAULT mode).
     */
    constructor(layout: Layout, vararg styles: Style.Binding<*>) : super(layout) {
        setStyles(*styles)
    }

    /**
     * Creates a group with the specified layout.
     */
    constructor(layout: Layout) : super(layout)

    override val styleClass: KClass<*>
        get() = Group::class
}

package tripleklay.ui

import kotlin.reflect.KClass

/**
 * Provides style defaults per element type for a sub-tree of the interface hierarchy.
 */
class Stylesheet private constructor(private val _styles: Map<KClass<*>, Styles>) {
    /** Builds stylesheets, obtain via [.builder].  */
    class Builder {
        /** Adds styles for the supplied element class. If styles exist for said class, the
         * supplied styles will be merged with the existing styles (with the new styles taking
         * precedence).
         * @throws NullPointerException if styles are added after [.create] is called.
         */
        fun add(eclass: KClass<*>, styles: Styles): Builder {
            val ostyles = _styles!![eclass]
            _styles!!.put(eclass, ostyles?.merge(styles) ?: styles)
            return this
        }

        /** Adds styles for the supplied element class (in the DEFAULT mode).
         * @throws NullPointerException if styles are added after [.create] is called.
         */
        fun add(eclass: KClass<*>, vararg styles: Style.Binding<*>): Builder {
            return add(eclass, Styles.make(*styles))
        }

        /** Adds styles for the supplied element class (in the specified mode).
         * @throws NullPointerException if styles are added after [.create] is called.
         */
        fun add(eclass: KClass<*>, mode: Style.Mode, vararg styles: Style.Binding<*>): Builder {
            return add(eclass, Styles.none().add(mode, *styles))
        }

        /** Creates a stylesheet with the previously configured style mappings.  */
        fun create(): Stylesheet {
            val sheet = Stylesheet(_styles!!)
            _styles = null // prevent further modification
            return sheet
        }

        private var _styles: MutableMap<KClass<*>, Styles>? = HashMap()
    }

    /**
     * Looks up the style for the supplied key and (concrete) element class. If the style is
     * inherited, the style may be fetched from the configuration for a supertype of the supplied
     * element type. Returns null if no configuration can be found.
     */
    internal fun <V> get(style: Style<V>, eclass: KClass<*>, elem: Element<*>): V? {
        val styles = _styles[eclass]
        val value = styles?.get(style, elem)
        if (value != null) return value

        // if the style is not inherited, or we're already checking for Element.class, then we've
        // done all the searching we can
        if (!style.inherited || eclass == Element::class) return null

        // otherwise check our parent class
        val parent = eclass.superclass ?: // TEMP: avoid confusion while PlayN POM disables class metadata by default
                throw RuntimeException(
                        "Your Klay application must not be compiled with -XdisableClassMetadata. " + "It breaks TripleKlay stylesheets.")
        return get(style, parent, elem)
    }

    companion object {

        /**
         * Returns a stylesheet builder.
         */
        fun builder(): Builder {
            return Builder()
        }
    }
}

expect val KClass<*>.superclass: KClass<*>?

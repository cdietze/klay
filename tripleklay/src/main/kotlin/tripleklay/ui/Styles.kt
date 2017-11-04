package tripleklay.ui

import klay.core.assert

/**
 * An immutable collection of styles. Used in builder-style to add, replace or remove styles.
 * Configure a group of styles and then apply them to an element via [Element.setStyles] or
 * [Element.addStyles].
 */
class Styles private constructor(private val _bindings: List<Binding<*>>) {

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the default mode. The receiver is not modified.
     */
    fun add(vararg bindings: Style.Binding<*>): Styles {
        return add(Style.Mode.DEFAULT, *bindings)
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the disabled mode. The receiver is not modified.
     */
    fun addDisabled(vararg bindings: Style.Binding<*>): Styles {
        return add(Style.Mode.DISABLED, *bindings)
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the selected mode. The receiver is not modified.
     */
    fun addSelected(vararg bindings: Style.Binding<*>): Styles {
        return add(Style.Mode.SELECTED, *bindings)
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the disabled selected mode. The receiver is not modified.
     */
    fun addDisabledSelected(vararg bindings: Style.Binding<*>): Styles {
        return add(Style.Mode.DISABLED_SELECTED, *bindings)
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles (in the specified mode). The receiver is not modified.
     */
    fun add(mode: Style.Mode, vararg bindings: Style.Binding<*>): Styles {
        if (bindings.isEmpty()) return this // optimization
        val nbindings = List(bindings.size, { i -> newBinding(bindings[i], mode) })
        // note that we take advantage of the fact that merge can handle unsorted bindings
        return merge(nbindings)
    }

    /**
     * Returns a new instance where no binding exists for the specified style in the specified
     * state. The receiver is not modified.
     */
    fun <V> clear(mode: Style.Mode, style: Style<V>): Styles {
        val index = _bindings.binarySearch(Binding(style), bindingComparator)
        if (index < 0) return this
        val binding = _bindings[index] as Binding<V>
        val nbindings = _bindings.toMutableList()
        nbindings[index] = binding.clear(mode)
        return Styles(nbindings)
    }

    /**
     * Returns a new styles instance which merges these styles with the supplied styles. Where both
     * instances define a particular style, the supplied `styles` will take precedence.
     */
    fun merge(styles: Styles): Styles {
        if (_bindings.isEmpty()) return styles
        return merge(styles._bindings)
    }

    internal fun <V> get(key: Style<V>, elem: Element<*>): V? {
        // we replicate Arrays.binarySearch here because we want to find the Binding with the
        // specified Style without creating a temporary garbage instance of Style.Binding
        var low = 0
        var high = _bindings.size - 1
        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midVal = _bindings[mid] as Binding<V>
            val cmp = midVal.compareToStyle(key)
            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return midVal[elem] // key found
        }
        return null
    }

    private fun merge(obindings: List<Binding<*>>): Styles {
        if (obindings.isEmpty()) return this // optimization

        // determine which of the to-be-merged styles also exist in our styles
        val dupidx = IntArray(obindings.size)
        var dups = 0
        for (ii in obindings.indices) {
            val idx = _bindings.binarySearch(obindings[ii], bindingComparator)
            if (idx >= 0) dups++
            dupidx[ii] = idx
        }

        // copy the old bindings, merge any duplicated bindings, tack the rest on the end
        val nbindings = _bindings.toMutableList()
        for (ii in obindings.indices) {
            val didx = dupidx[ii]
            if (didx >= 0) {
                val nb = nbindings[didx] as Binding<Any>
                val ob = obindings[ii] as Binding<Any>
                nbindings[didx] = nb.merge(ob)
            } else
                nbindings.add(obindings[ii])
        }
        nbindings.sortWith(bindingComparator)
        return Styles(nbindings)
    }

    data class Binding<V>(
            val style: Style<V>,
            private val defaultV: V? = null,
            private val disabledV: V? = null,
            private val selectedV: V? = null,
            private val disSelectedV: V? = null) : Comparable<Binding<V>> {

        constructor(binding: Style.Binding<V>, mode: Style.Mode) :
                this(
                        binding.style,
                        if (mode == Style.Mode.DEFAULT) binding.value else null,
                        if (mode == Style.Mode.DISABLED) binding.value else null,
                        if (mode == Style.Mode.SELECTED) binding.value else null,
                        if (mode == Style.Mode.DISABLED_SELECTED) binding.value else null
                )

        operator fun get(elem: Element<*>): V? {
            // prioritize as: disabled_selected, disabled, selected, default
            if (elem.isEnabled) {
                if (elem.isSelected && selectedV != null) return selectedV
            } else {
                if (elem.isSelected && disSelectedV != null) return disSelectedV
                if (disabledV != null) return disabledV
            }
            return defaultV
        }

        fun merge(other: Binding<V>): Binding<V> {
            return Binding(style,
                    merge(defaultV, other.defaultV),
                    merge(disabledV, other.disabledV),
                    merge(selectedV, other.selectedV),
                    merge(disSelectedV, other.disSelectedV))
        }

        fun clear(mode: Style.Mode): Binding<V> {
            when (mode) {
                Style.Mode.DEFAULT -> return this.copy(defaultV = null)
                Style.Mode.DISABLED -> return this.copy(disabledV = null)
                Style.Mode.SELECTED -> return this.copy(selectedV = null)
                Style.Mode.DISABLED_SELECTED -> return this.copy(disSelectedV = null)
                else -> return this
            }
        }

        fun compareToStyle(style: Style<V>): Int {
            if (this.style === style) return 0
            val hc = this.style.hashCode()
            val ohc = style.hashCode()
            assert(hc != ohc)
            return if (hc < ohc) -1 else 1
        }

        private fun merge(ours: V?, theirs: V?): V? {
            return theirs ?: ours
        }

        override fun compareTo(other: Binding<V>): Int {
            if (this.style === other.style) return 0
            val hc = this.style.hashCode()
            val ohc = other.style.hashCode()
            assert(hc != ohc)
            return if (hc < ohc) -1 else 1
        }
    }

    companion object {

        val bindingComparator2: Comparator<Binding<*>> = Comparator({ a: Binding<*>, b: Binding<*> -> 2 })
        val bindingComparator3: Comparator<Int> = Comparator({ a: Int, b: Int -> 2 })
        //
        val bindingComparator: Comparator<Binding<*>> = object : Comparator<Binding<*>> {
            override fun compare(o1: Binding<*>, o2: Binding<*>): Int {
                if (o1.style === o2.style) return 0
                val hc = o1.style.hashCode()
                val ohc = o2.style.hashCode()
                assert(hc != ohc)
                return if (hc < ohc) -1 else 1
            }
        }

        /**
         * Returns the empty styles instance.
         */
        fun none(): Styles {
            return _noneSingleton
        }

        /** Creates a styles instance with the supplied style bindings in the DEFAULT mode.  */
        fun make(vararg bindings: Style.Binding<*>): Styles {
            return none().add(Style.Mode.DEFAULT, *bindings)
        }

        /** Resolves the current value of `style` on `element`.  */
        fun <V> resolveStyle(element: Element<*>, style: Style<V>): V {
            // first check for the style configured directly on the element
            var value = element.styles().get(style, element)
            if (value != null) return value

            // now check for the style in the appropriate stylesheets
            var group: Container<*>? = element as? Container<*> ?: element.parent()
            while (group != null) {
                val sheet = group.stylesheet()
                if (sheet == null) {
                    group = group.parent()
                    continue
                }
                value = sheet.get(style, element.styleClass, element)
                if (value != null) return value
                group = group.parent()
            }

            // if we haven't found the style anywhere, return the global default
            return style.getDefault(element)
        }

        internal fun <V> newBinding(binding: Style.Binding<V>, mode: Style.Mode): Binding<V> {
            return Binding(binding, mode)
        }

        private val _noneSingleton = Styles(listOf())
    }
}

package tripleklay.ui

import java.util.Arrays

/**
 * An immutable collection of styles. Used in builder-style to add, replace or remove styles.
 * Configure a group of styles and then apply them to an element via [Element.setStyles] or
 * [Element.addStyles].
 */
class Styles private constructor(protected var _bindings: Array<Binding<*>>) {

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
        if (bindings.size == 0) return this // optimization
        val nbindings = arrayOfNulls<Binding<*>>(bindings.size)
        for (ii in bindings.indices) {
            nbindings[ii] = newBinding<*>(bindings[ii], mode)
        }
        // note that we take advantage of the fact that merge can handle unsorted bindings
        return merge(nbindings)
    }

    /**
     * Returns a new instance where no binding exists for the specified style in the specified
     * state. The receiver is not modified.
     */
    fun <V> clear(mode: Style.Mode, style: Style<V>): Styles {
        val index = Arrays.binarySearch(_bindings, Binding(style))
        if (index < 0) return this
        val binding = _bindings[index] as Binding<V>
        val nbindings = arrayOfNulls<Binding<*>>(_bindings.size)
        System.arraycopy(_bindings, 0, nbindings, 0, nbindings.size)
        nbindings[index] = binding.clear(mode)
        return Styles(nbindings)
    }

    /**
     * Returns a new styles instance which merges these styles with the supplied styles. Where both
     * instances define a particular style, the supplied `styles` will take precedence.
     */
    fun merge(styles: Styles): Styles {
        if (_bindings.size == 0) return styles
        return merge(styles._bindings)
    }

    internal operator fun <V> get(key: Style<V>, elem: Element<*>): V? {
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

    private fun merge(obindings: Array<Binding<*>>): Styles {
        if (obindings.size == 0) return this // optimization

        // determine which of the to-be-merged styles also exist in our styles
        val dupidx = IntArray(obindings.size)
        var dups = 0
        for (ii in obindings.indices) {
            val idx = Arrays.binarySearch(_bindings, obindings[ii])
            if (idx >= 0) dups++
            dupidx[ii] = idx
        }

        // copy the old bindings, merge any duplicated bindings, tack the rest on the end
        val nbindings = arrayOfNulls<Binding<*>>(_bindings.size + obindings.size - dups)
        System.arraycopy(_bindings, 0, nbindings, 0, _bindings.size)
        var idx = _bindings.size
        for (ii in obindings.indices) {
            val didx = dupidx[ii]
            if (didx >= 0) {
                val nb = nbindings[didx] as Binding<Any>
                val ob = obindings[ii] as Binding<Any>
                nbindings[didx] = nb.merge(ob)
            } else
                nbindings[idx++] = obindings[ii]
        }
        Arrays.sort(nbindings)

        return Styles(nbindings)
    }

    internal class Binding<V>(val style: Style<V>) : Comparable<Binding<V>> {

        constructor(binding: Style.Binding<V>, mode: Style.Mode) : this(binding.style) {
            when (mode) {
                Style.Mode.DEFAULT -> _defaultV = binding.value
                Style.Mode.DISABLED -> _disabledV = binding.value
                Style.Mode.SELECTED -> _selectedV = binding.value
                Style.Mode.DISABLED_SELECTED -> _disSelectedV = binding.value
            }
        }

        constructor(style: Style<V>, defaultV: V, disabledV: V, selectedV: V, disSelectedV: V) : this(style) {
            _defaultV = defaultV
            _disabledV = disabledV
            _selectedV = selectedV
            _disSelectedV = disSelectedV
        }

        operator fun get(elem: Element<*>): V {
            // prioritize as: disabled_selected, disabled, selected, default
            if (elem.isEnabled) {
                if (elem.isSelected && _selectedV != null) return _selectedV
            } else {
                if (elem.isSelected && _disSelectedV != null) return _disSelectedV
                if (_disabledV != null) return _disabledV
            }
            return _defaultV
        }

        fun merge(other: Binding<V>): Binding<V> {
            return Binding(style,
                    merge(_defaultV, other._defaultV),
                    merge(_disabledV, other._disabledV),
                    merge(_selectedV, other._selectedV),
                    merge(_disSelectedV, other._disSelectedV))
        }

        fun clear(mode: Style.Mode): Binding<V> {
            when (mode) {
                Style.Mode.DEFAULT -> return Binding<V>(style, null, _disabledV, _selectedV, _disSelectedV)
                Style.Mode.DISABLED -> return Binding<V>(style, _defaultV, null, _selectedV, _disSelectedV)
                Style.Mode.SELECTED -> return Binding<V>(style, _defaultV, _disabledV, null, _disSelectedV)
                Style.Mode.DISABLED_SELECTED -> return Binding<V>(style, _defaultV, _disabledV, _selectedV, null)
                else -> return this
            }
        }

        override fun compareTo(other: Binding<V>): Int {
            if (this.style === other.style) return 0
            val hc = this.style.hashCode()
            val ohc = other.style.hashCode()
            assert(hc != ohc)
            return if (hc < ohc) -1 else 1
        }

        fun compareToStyle(style: Style<V>): Int {
            if (this.style === style) return 0
            val hc = this.style.hashCode()
            val ohc = style.hashCode()
            assert(hc != ohc)
            return if (hc < ohc) -1 else 1
        }

        private fun merge(ours: V, theirs: V?): V {
            return theirs ?: ours
        }

        protected var _defaultV: V
        protected var _disabledV: V? = null
        protected var _selectedV: V? = null
        protected var _disSelectedV: V? = null
    }

    companion object {
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

        protected val _noneSingleton = Styles(arrayOfNulls<Binding<*>>(0))
    }
}

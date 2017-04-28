//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * An exception thrown if an operation is performed on a [Path] that is in an illegal state
 * with respect to the particular operation being performed. For example, appending a segment to a
 * path without an initial moveto.
 */
class IllegalPathStateException : RuntimeException {

    constructor() {}

    constructor(s: String) : super(s) {}

    companion object {
        private val serialVersionUID = 5494939619370624441L
    }
}

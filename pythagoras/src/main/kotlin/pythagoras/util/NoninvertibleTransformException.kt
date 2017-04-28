//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.util

/**
 * An exception thrown by `Transform` when a request for an inverse transform cannot be
 * satisfied.
 */
class NoninvertibleTransformException(s: String) : RuntimeException(s) {
    companion object {
        private val serialVersionUID = 5208863644264280750L
    }
}

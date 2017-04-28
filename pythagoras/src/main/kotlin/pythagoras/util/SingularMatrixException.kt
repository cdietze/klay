//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.util

/**
 * Thrown when inversion is attempted on a singular (non-invertible) matrix.
 */
class SingularMatrixException : RuntimeException {

    /**
     * Creates a new exception.
     */
    constructor() {}

    /**
     * Creates a new exception with the provided message.
     */
    constructor(message: String) : super(message) {}

    companion object {
        private val serialVersionUID = -4744745375693073952L
    }
}

package klay.scene

/**
 * Serves as the root of the scene graph. This is just a [GroupLayer] with minor tweaks to
 * ensure that when layers are added to it, they transition properly to the "added to scene graph"
 * state.
 */
class RootLayer : GroupLayer {

    /** Creates an unclipped root layer. This is almost always what you want.  */
    constructor() {
        setState(State.ADDED)
    }

    /** Creates a root layer clipped to the specified dimensions. This is rarely what you want.  */
    constructor(width: Float, height: Float) : super(width, height) {
        setState(State.ADDED)
    }
}

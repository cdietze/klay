package klay.scene

import klay.core.*

/**
 * A simple class for games which wish to use a single scene graph.
 */
abstract class SceneGame(plat: Platform, updateRate: Int) : Game(plat, updateRate) {

    private var cred: Float = 0.toFloat()
    private var cgreen: Float = 0.toFloat()
    private var cblue: Float = 0.toFloat()
    private var calpha: Float = 0.toFloat() // default to zero

    val defaultBatch: QuadBatch
    val viewSurf: Surface
    val rootLayer: RootLayer

    init {

        val gl = plat.graphics.gl
        gl.glDisable(GL20.GL_CULL_FACE)
        gl.glEnable(GL20.GL_BLEND)
        gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)

        defaultBatch = createDefaultBatch(gl)
        viewSurf = Surface(plat.graphics, plat.graphics.defaultRenderTarget, defaultBatch)
        rootLayer = RootLayer()

        paint.connect { this::paintScene }.atPrio(scenePaintPrio())
    }

    /**
     * Configures the color to which the frame buffer is cleared prior to painting the scene graph.
     */
    fun setClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        cred = red
        cgreen = green
        cblue = blue
        calpha = alpha
    }

    /**
     * Configures the color to which the frame buffer is cleared prior to painting the scene graph.
     */
    fun setClearColor(argb: Int) {
        val red = (argb shr 16 and 0xFF) / 255f
        val green = (argb shr 8 and 0xFF) / 255f
        val blue = (argb shr 0 and 0xFF) / 255f
        val alpha = (argb shr 24 and 0xFF) / 255f
        setClearColor(red, green, blue, alpha)
    }

    /**
     * Renders the main scene graph into the OpenGL frame buffer.
     */
    protected fun paintScene() {
        viewSurf.saveTx()
        viewSurf.begin()
        viewSurf.clear(cred, cgreen, cblue, calpha)
        try {
            rootLayer.paint(viewSurf)
        } finally {
            viewSurf.end()
            viewSurf.restoreTx()
        }
    }

    /** Defines the priority at which the scene graph is painted. By default this is -1 which causes
     * the scene graph to be painted *after* any slots listening to the paint tick at the
     * default priority (0).  */
    protected fun scenePaintPrio(): Int {
        return -1
    }

    /** Creates the [QuadBatch] used as the default top-level batch when rendering the scene
     * graph. This uses [UniformQuadBatch] if possible, [TriangleBatch] otherwise.  */
    protected fun createDefaultBatch(gl: GL20): QuadBatch {
        try {
            if (UniformQuadBatch.isLikelyToPerform(gl)) return UniformQuadBatch(gl)
        } catch (e: Exception) {
            // oops, fall through and use a TriangleBatch
        }

        return TriangleBatch(gl)
    }
}

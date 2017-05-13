package klay.jvm

import klay.core.Key
import klay.core.Keyboard.TextType
import klay.core.Mouse.ButtonEvent
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import pythagoras.f.Point
import react.RFuture
import javax.swing.JOptionPane

class GLFWInput(override val plat: LWJGLPlatform, private val window: Long) : JavaInput(plat) {
    private var lastMouseX = -1f
    private var lastMouseY = -1f

    // we have to keep strong references to GLFW callbacks
    private val charCallback = object : GLFWCharCallback() {
        override fun invoke(window: Long, codepoint: Int) {
            emitKeyTyped(System.currentTimeMillis().toDouble(), codepoint.toChar())
        }
    }
    private val keyCallback = object : GLFWKeyCallback() {
        override fun invoke(window: Long, keyCode: Int, scancode: Int, action: Int, mods: Int) {
            val time = System.currentTimeMillis().toDouble()
            val key = translateKey(keyCode)
            val pressed = action == GLFW_PRESS || action == GLFW_REPEAT
            if (key != null)
                emitKeyPress(time, key, pressed, toModifierFlags(mods))
            else
                plat.log().warn("Unknown keyCode:" + keyCode)
        }
    }
    private val mouseBtnCallback = object : GLFWMouseButtonCallback() {
        override fun invoke(handle: Long, btnIdx: Int, action: Int, mods: Int) {
            val time = System.currentTimeMillis().toDouble()
            val m = queryCursorPosition()
            val btn = getButton(btnIdx) ?: return
            emitMouseButton(time, m.x, m.y, btn, action == GLFW_PRESS, toModifierFlags(mods))
        }
    }
    private val cursorPosCallback = object : GLFWCursorPosCallback() {
        override fun invoke(handle: Long, xpos: Double, ypos: Double) {
            val time = System.currentTimeMillis().toDouble()
            val x = xpos.toFloat()
            val y = ypos.toFloat()
            if (lastMouseX == -1f) {
                lastMouseX = x
                lastMouseY = y
            }
            val dx = x - lastMouseX
            val dy = y - lastMouseY
            emitMouseMotion(time, x, y, dx, dy, pollModifierFlags())
            lastMouseX = x
            lastMouseY = y
        }
    }
    private val scrollCallback = object : GLFWScrollCallback() {
        override fun invoke(handle: Long, xoffset: Double, yoffset: Double) {
            val m = queryCursorPosition()
            val time = System.currentTimeMillis().toDouble()
            //TODO: is it correct that just simply sets the flag as 0?
            if (GLFW_CURSOR_DISABLED == glfwGetInputMode(window, GLFW_CURSOR))
                emitMouseMotion(time, m.x, m.y, xoffset.toFloat(), -yoffset.toFloat(), 0)
            else
                emitMouseWheel(time, m.x, m.y, if (yoffset > 0) -1 else 1, 0)
        }
    }

    init {
        glfwSetCharCallback(window, charCallback)
        glfwSetKeyCallback(window, keyCallback)
        glfwSetMouseButtonCallback(window, mouseBtnCallback)
        glfwSetCursorPosCallback(window, cursorPosCallback)
        glfwSetScrollCallback(window, scrollCallback)
    }

    override fun update() {
        glfwPollEvents()
        super.update()
    }

    internal fun shutdown() {
        charCallback.close()
        keyCallback.close()
        mouseBtnCallback.close()
        cursorPosCallback.close()
        scrollCallback.close()
    }

    override fun getText(textType: TextType, label: String, initVal: String): RFuture<String> {
        if (plat.needsHeadless()) throw UnsupportedOperationException(NO_UI_ERROR)

        val result = JOptionPane.showInputDialog(null, label, "", JOptionPane.QUESTION_MESSAGE, null, null, initVal)
        return RFuture.success(result as String)
    }

    override fun sysDialog(title: String, text: String,
                           ok: String, cancel: String?): RFuture<Boolean> {
        if (plat.needsHeadless()) throw UnsupportedOperationException(NO_UI_ERROR)

        val optType = JOptionPane.OK_CANCEL_OPTION
        val msgType = if (cancel == null) JOptionPane.INFORMATION_MESSAGE else JOptionPane.QUESTION_MESSAGE
        val options = if (cancel == null) arrayOf<Any>(ok) else arrayOf<Any>(ok, cancel)
        val defOption = cancel ?: ok
        val result = JOptionPane.showOptionDialog(null, text, title, optType, msgType, null, options, defOption)
        return RFuture.success(result == 0)
    }

    override val hasMouseLock: Boolean = true

    override var isMouseLocked: Boolean
        get() = glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
        set(locked) = glfwSetInputMode(window, GLFW_CURSOR, if (locked) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL)

    private val xpos = BufferUtils.createByteBuffer(8).asDoubleBuffer()
    private val ypos = BufferUtils.createByteBuffer(8).asDoubleBuffer()
    private val cpos = Point()
    private fun queryCursorPosition(): Point {
        xpos.rewind()
        ypos.rewind()
        glfwGetCursorPos(window, xpos, ypos)
        cpos[xpos.get().toFloat()] = ypos.get().toFloat()
        return cpos
    }

    /** Returns the current state of the modifier keys. Note: the code assumes the current state of
     * the modifier keys is "correct" for all events that have arrived since the last call to
     * update; since that happens pretty frequently, 60fps, that's probably good enough.  */
    private fun pollModifierFlags(): Int {
        return modifierFlags(
                isKeyDown(GLFW_KEY_LEFT_ALT) || isKeyDown(GLFW_KEY_LEFT_ALT),
                isKeyDown(GLFW_KEY_LEFT_CONTROL) || isKeyDown(GLFW_KEY_RIGHT_CONTROL),
                isKeyDown(GLFW_KEY_LEFT_SUPER) || isKeyDown(GLFW_KEY_RIGHT_SUPER),
                isKeyDown(GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW_KEY_RIGHT_SHIFT))
    }

    /** Converts GLFW modifier key flags into Klay modifier key flags.  */
    private fun toModifierFlags(mods: Int): Int {
        return modifierFlags(mods and GLFW_MOD_ALT != 0,
                mods and GLFW_MOD_CONTROL != 0,
                mods and GLFW_MOD_SUPER != 0,
                mods and GLFW_MOD_SHIFT != 0)
    }

    private fun isKeyDown(key: Int): Boolean {
        return glfwGetKey(window, key) == GLFW_PRESS
    }

    private fun translateKey(keyCode: Int): Key? {
        when (keyCode) {
            GLFW_KEY_ESCAPE -> return Key.ESCAPE
            GLFW_KEY_1 -> return Key.K1
            GLFW_KEY_2 -> return Key.K2
            GLFW_KEY_3 -> return Key.K3
            GLFW_KEY_4 -> return Key.K4
            GLFW_KEY_5 -> return Key.K5
            GLFW_KEY_6 -> return Key.K6
            GLFW_KEY_7 -> return Key.K7
            GLFW_KEY_8 -> return Key.K8
            GLFW_KEY_9 -> return Key.K9
            GLFW_KEY_0 -> return Key.K0
            GLFW_KEY_MINUS -> return Key.MINUS
            GLFW_KEY_EQUAL -> return Key.EQUALS
            GLFW_KEY_BACKSPACE -> return Key.BACK
            GLFW_KEY_TAB -> return Key.TAB
            GLFW_KEY_Q -> return Key.Q
            GLFW_KEY_W -> return Key.W
            GLFW_KEY_E -> return Key.E
            GLFW_KEY_R -> return Key.R
            GLFW_KEY_T -> return Key.T
            GLFW_KEY_Y -> return Key.Y
            GLFW_KEY_U -> return Key.U
            GLFW_KEY_I -> return Key.I
            GLFW_KEY_O -> return Key.O
            GLFW_KEY_P -> return Key.P
            GLFW_KEY_LEFT_BRACKET -> return Key.LEFT_BRACKET
            GLFW_KEY_RIGHT_BRACKET -> return Key.RIGHT_BRACKET
            GLFW_KEY_ENTER -> return Key.ENTER
            GLFW_KEY_RIGHT_CONTROL -> return Key.CONTROL
            GLFW_KEY_LEFT_CONTROL -> return Key.CONTROL
            GLFW_KEY_A -> return Key.A
            GLFW_KEY_S -> return Key.S
            GLFW_KEY_D -> return Key.D
            GLFW_KEY_F -> return Key.F
            GLFW_KEY_G -> return Key.G
            GLFW_KEY_H -> return Key.H
            GLFW_KEY_J -> return Key.J
            GLFW_KEY_K -> return Key.K
            GLFW_KEY_L -> return Key.L
            GLFW_KEY_SEMICOLON -> return Key.SEMICOLON
            GLFW_KEY_APOSTROPHE -> return Key.QUOTE
            GLFW_KEY_GRAVE_ACCENT -> return Key.BACKQUOTE
            GLFW_KEY_LEFT_SHIFT -> return Key.SHIFT // Klay doesn't know left v. right
            GLFW_KEY_BACKSLASH -> return Key.BACKSLASH
            GLFW_KEY_Z -> return Key.Z
            GLFW_KEY_X -> return Key.X
            GLFW_KEY_C -> return Key.C
            GLFW_KEY_V -> return Key.V
            GLFW_KEY_B -> return Key.B
            GLFW_KEY_N -> return Key.N
            GLFW_KEY_M -> return Key.M
            GLFW_KEY_COMMA -> return Key.COMMA
            GLFW_KEY_PERIOD -> return Key.PERIOD
            GLFW_KEY_SLASH -> return Key.SLASH
            GLFW_KEY_RIGHT_SHIFT -> return Key.SHIFT // Klay doesn't know left v. right
            GLFW_KEY_KP_MULTIPLY -> return Key.MULTIPLY
            GLFW_KEY_SPACE -> return Key.SPACE
            GLFW_KEY_CAPS_LOCK -> return Key.CAPS_LOCK
            GLFW_KEY_F1 -> return Key.F1
            GLFW_KEY_F2 -> return Key.F2
            GLFW_KEY_F3 -> return Key.F3
            GLFW_KEY_F4 -> return Key.F4
            GLFW_KEY_F5 -> return Key.F5
            GLFW_KEY_F6 -> return Key.F6
            GLFW_KEY_F7 -> return Key.F7
            GLFW_KEY_F8 -> return Key.F8
            GLFW_KEY_F9 -> return Key.F9
            GLFW_KEY_F10 -> return Key.F10
            GLFW_KEY_NUM_LOCK -> return Key.NP_NUM_LOCK
            GLFW_KEY_SCROLL_LOCK -> return Key.SCROLL_LOCK
            GLFW_KEY_KP_7 -> return Key.NP7
            GLFW_KEY_KP_8 -> return Key.NP8
            GLFW_KEY_KP_9 -> return Key.NP9
            GLFW_KEY_KP_SUBTRACT -> return Key.NP_SUBTRACT
            GLFW_KEY_KP_4 -> return Key.NP4
            GLFW_KEY_KP_5 -> return Key.NP5
            GLFW_KEY_KP_6 -> return Key.NP6
            GLFW_KEY_KP_ADD -> return Key.NP_ADD
            GLFW_KEY_KP_1 -> return Key.NP1
            GLFW_KEY_KP_2 -> return Key.NP2
            GLFW_KEY_KP_3 -> return Key.NP3
            GLFW_KEY_KP_0 -> return Key.NP0
            GLFW_KEY_KP_DECIMAL -> return Key.NP_DECIMAL
            GLFW_KEY_F11 -> return Key.F11
            GLFW_KEY_F12 -> return Key.F12
        //case GLFW_KEY_F13          : return Key.F13;
        //case GLFW_KEY_F14          : return Key.F14;
        //case GLFW_KEY_F15          : return Key.F15;
        //case GLFW_KEY_F16          : return Key.F16;
        //case GLFW_KEY_F17          : return Key.F17;
        //case GLFW_KEY_F18          : return Key.F18;
        //case GLFW_KEY_KANA         : return Key.
        //case GLFW_KEY_F19          : return Key.F19;
        //case GLFW_KEY_CONVERT      : return Key.
        //case GLFW_KEY_NOCONVERT    : return Key.
        //case GLFW_KEY_YEN          : return Key.
        //case GLFW_KEY_NUMPADEQUALS : return Key.
        //TODO: case GLFW_KEY_CIRCUMFLEX   : return Key.CIRCUMFLEX;
        //TODO: case GLFW_KEY_AT           : return Key.AT;
        //TODO: case GLFW_KEY_COLON        : return Key.COLON;
        //TODO: case GLFW_KEY_UNDERLINE    : return Key.UNDERSCORE;
        //case GLFW_KEY_KANJI        : return Key.
        //case GLFW_KEY_STOP         : return Key.
        //case GLFW_KEY_AX           : return Key.
        //case GLFW_KEY_UNLABELED    : return Key.
        //case GLFW_KEY_NUMPADENTER  : return Key.
        //case GLFW_KEY_SECTION      : return Key.
        //case GLFW_KEY_NUMPADCOMMA  : return Key.
        //case GLFW_KEY_DIVIDE       :
        //TODO: case GLFW_KEY_SYSRQ        : return Key.SYSRQ;
            GLFW_KEY_RIGHT_ALT -> return Key.ALT // Klay doesn't know left v. right
            GLFW_KEY_LEFT_ALT -> return Key.ALT // Klay doesn't know left v. right
            GLFW_KEY_MENU -> return Key.FUNCTION
            GLFW_KEY_PAUSE -> return Key.PAUSE
            GLFW_KEY_HOME -> return Key.HOME
            GLFW_KEY_UP -> return Key.UP
            GLFW_KEY_PAGE_UP -> return Key.PAGE_UP
            GLFW_KEY_LEFT -> return Key.LEFT
            GLFW_KEY_RIGHT -> return Key.RIGHT
            GLFW_KEY_END -> return Key.END
            GLFW_KEY_DOWN -> return Key.DOWN
            GLFW_KEY_PAGE_DOWN -> return Key.PAGE_DOWN
            GLFW_KEY_INSERT -> return Key.INSERT
            GLFW_KEY_DELETE -> return Key.DELETE
        //TODO: case GLFW_KEY_CLEAR        : return Key.CLEAR;
            GLFW_KEY_RIGHT_SUPER -> return Key.META // Klay doesn't know left v. right
            GLFW_KEY_LEFT_SUPER -> return Key.META // Klay doesn't know left v. right
        //case GLFW_KEY_LWIN         : return Key.WINDOWS; // Duplicate with KEY_LMETA
        //case GLFW_KEY_RWIN         : return Key.WINDOWS; // Duplicate with KEY_RMETA
        //case GLFW_KEY_APPS         : return Key.
        //TODO: case GLFW_KEY_POWER  : return Key.POWER;
        //case Keyboard.KEY_SLEEP    : return Key.
            else -> return null
        }
    }

    companion object {

        private val NO_UI_ERROR = "The java-lwjgl backend does not allow interop with AWT on Mac OS X. " + "Use the java-swt backend if you need native dialogs."

        private fun getButton(lwjglButton: Int): ButtonEvent.Id? {
            when (lwjglButton) {
                GLFW_MOUSE_BUTTON_LEFT -> return ButtonEvent.Id.LEFT
                GLFW_MOUSE_BUTTON_MIDDLE -> return ButtonEvent.Id.MIDDLE
                GLFW_MOUSE_BUTTON_RIGHT -> return ButtonEvent.Id.RIGHT
                else -> return null
            }
        }
    }
}

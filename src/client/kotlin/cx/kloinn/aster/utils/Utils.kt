package cx.kloinn.aster.utils

import org.lwjgl.glfw.GLFW.*

object Utils {
    fun charToGlfwKey(char: String): Int {
        val key = char.trim().uppercase()

        if (key.length == 1) {
            return when (val c = key[0]) {
                in 'A'..'Z' -> c.code
                in '0'..'9' -> c.code

                ' '  -> GLFW_KEY_SPACE
                '\'' -> GLFW_KEY_APOSTROPHE
                ','  -> GLFW_KEY_COMMA
                '-'  -> GLFW_KEY_MINUS
                '.'  -> GLFW_KEY_PERIOD
                '/'  -> GLFW_KEY_SLASH
                ';'  -> GLFW_KEY_SEMICOLON
                '='  -> GLFW_KEY_EQUAL
                '['  -> GLFW_KEY_LEFT_BRACKET
                '\\' -> GLFW_KEY_BACKSLASH
                ']'  -> GLFW_KEY_RIGHT_BRACKET
                '`'  -> GLFW_KEY_GRAVE_ACCENT

                else -> throw IllegalArgumentException("Unsupported GLFW key: $char")
            }
        }

        return when (key) {
            "SPACE" -> GLFW_KEY_SPACE

            "ESC", "ESCAPE" -> GLFW_KEY_ESCAPE
            "ENTER", "RETURN" -> GLFW_KEY_ENTER
            "TAB" -> GLFW_KEY_TAB
            "BACKSPACE" -> GLFW_KEY_BACKSPACE
            "INSERT" -> GLFW_KEY_INSERT
            "DELETE", "DEL" -> GLFW_KEY_DELETE

            "RIGHT" -> GLFW_KEY_RIGHT
            "LEFT" -> GLFW_KEY_LEFT
            "DOWN" -> GLFW_KEY_DOWN
            "UP" -> GLFW_KEY_UP

            "PAGE_UP", "PAGEUP" -> GLFW_KEY_PAGE_UP
            "PAGE_DOWN", "PAGEDOWN" -> GLFW_KEY_PAGE_DOWN
            "HOME" -> GLFW_KEY_HOME
            "END" -> GLFW_KEY_END

            "CAPS_LOCK", "CAPSLOCK" -> GLFW_KEY_CAPS_LOCK
            "SCROLL_LOCK", "SCROLLLOCK" -> GLFW_KEY_SCROLL_LOCK
            "NUM_LOCK", "NUMLOCK" -> GLFW_KEY_NUM_LOCK
            "PRINT_SCREEN", "PRINTSCREEN" -> GLFW_KEY_PRINT_SCREEN
            "PAUSE" -> GLFW_KEY_PAUSE

            "F1" -> GLFW_KEY_F1
            "F2" -> GLFW_KEY_F2
            "F3" -> GLFW_KEY_F3
            "F4" -> GLFW_KEY_F4
            "F5" -> GLFW_KEY_F5
            "F6" -> GLFW_KEY_F6
            "F7" -> GLFW_KEY_F7
            "F8" -> GLFW_KEY_F8
            "F9" -> GLFW_KEY_F9
            "F10" -> GLFW_KEY_F10
            "F11" -> GLFW_KEY_F11
            "F12" -> GLFW_KEY_F12
            "F13" -> GLFW_KEY_F13
            "F14" -> GLFW_KEY_F14
            "F15" -> GLFW_KEY_F15
            "F16" -> GLFW_KEY_F16
            "F17" -> GLFW_KEY_F17
            "F18" -> GLFW_KEY_F18
            "F19" -> GLFW_KEY_F19
            "F20" -> GLFW_KEY_F20
            "F21" -> GLFW_KEY_F21
            "F22" -> GLFW_KEY_F22
            "F23" -> GLFW_KEY_F23
            "F24" -> GLFW_KEY_F24
            "F25" -> GLFW_KEY_F25

            "KP_0", "NUMPAD_0" -> GLFW_KEY_KP_0
            "KP_1", "NUMPAD_1" -> GLFW_KEY_KP_1
            "KP_2", "NUMPAD_2" -> GLFW_KEY_KP_2
            "KP_3", "NUMPAD_3" -> GLFW_KEY_KP_3
            "KP_4", "NUMPAD_4" -> GLFW_KEY_KP_4
            "KP_5", "NUMPAD_5" -> GLFW_KEY_KP_5
            "KP_6", "NUMPAD_6" -> GLFW_KEY_KP_6
            "KP_7", "NUMPAD_7" -> GLFW_KEY_KP_7
            "KP_8", "NUMPAD_8" -> GLFW_KEY_KP_8
            "KP_9", "NUMPAD_9" -> GLFW_KEY_KP_9

            "KP_DECIMAL", "NUMPAD_DECIMAL" -> GLFW_KEY_KP_DECIMAL
            "KP_DIVIDE", "NUMPAD_DIVIDE" -> GLFW_KEY_KP_DIVIDE
            "KP_MULTIPLY", "NUMPAD_MULTIPLY" -> GLFW_KEY_KP_MULTIPLY
            "KP_SUBTRACT", "NUMPAD_SUBTRACT" -> GLFW_KEY_KP_SUBTRACT
            "KP_ADD", "NUMPAD_ADD" -> GLFW_KEY_KP_ADD
            "KP_ENTER", "NUMPAD_ENTER" -> GLFW_KEY_KP_ENTER
            "KP_EQUAL", "NUMPAD_EQUAL" -> GLFW_KEY_KP_EQUAL

            "LEFT_SHIFT", "LSHIFT" -> GLFW_KEY_LEFT_SHIFT
            "LEFT_CONTROL", "LEFT_CTRL", "LCTRL" -> GLFW_KEY_LEFT_CONTROL
            "LEFT_ALT", "LALT" -> GLFW_KEY_LEFT_ALT
            "LEFT_SUPER", "LSUPER" -> GLFW_KEY_LEFT_SUPER

            "RIGHT_SHIFT", "RSHIFT" -> GLFW_KEY_RIGHT_SHIFT
            "RIGHT_CONTROL", "RIGHT_CTRL", "RCTRL" -> GLFW_KEY_RIGHT_CONTROL
            "RIGHT_ALT", "RALT" -> GLFW_KEY_RIGHT_ALT
            "RIGHT_SUPER", "RSUPER" -> GLFW_KEY_RIGHT_SUPER

            "MENU" -> GLFW_KEY_MENU

            "APOSTROPHE" -> GLFW_KEY_APOSTROPHE
            "COMMA" -> GLFW_KEY_COMMA
            "MINUS" -> GLFW_KEY_MINUS
            "PERIOD", "DOT" -> GLFW_KEY_PERIOD
            "SLASH" -> GLFW_KEY_SLASH
            "SEMICOLON" -> GLFW_KEY_SEMICOLON
            "EQUAL", "EQUALS" -> GLFW_KEY_EQUAL
            "LEFT_BRACKET" -> GLFW_KEY_LEFT_BRACKET
            "BACKSLASH" -> GLFW_KEY_BACKSLASH
            "RIGHT_BRACKET" -> GLFW_KEY_RIGHT_BRACKET
            "GRAVE_ACCENT", "BACKTICK" -> GLFW_KEY_GRAVE_ACCENT

            else -> throw IllegalArgumentException("Unsupported GLFW key")
        }
    }
}
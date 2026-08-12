package cx.kloinn.aster.utils

import kotlin.math.abs
import kotlin.math.roundToInt

data class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 255
) {
    fun toRgbInt(): Int {
        return (red shl 16) or
                (green shl 8) or
                blue
    }

    fun toArgb(opacity: Float = 1.0f): Int {
        return toArgb(this.toRgbInt(), opacity)
    }

    companion object {
        fun toArgb(rgb: Int, opacity: Float = 1.0f): Int {
            val alpha = (opacity.coerceIn(0.0f, 1.0f) * 255).toInt()
            return (alpha shl 24) or (rgb and 0xFFFFFF)
        }

        fun fromHsv(
            hue: Float,
            saturation: Float,
            value: Float,
            alpha: Int = 255
        ): Color {
            val normalizedHue = ((hue % 360f) + 360f) % 360f

            val chroma = value * saturation
            val hueSection = normalizedHue / 60f
            val x = chroma * (1f - abs(hueSection % 2f - 1f))
            val match = value - chroma

            val (redPrime, greenPrime, bluePrime) = when {
                hueSection < 1f -> Triple(chroma, x, 0f)
                hueSection < 2f -> Triple(x, chroma, 0f)
                hueSection < 3f -> Triple(0f, chroma, x)
                hueSection < 4f -> Triple(0f, x, chroma)
                hueSection < 5f -> Triple(x, 0f, chroma)
                else -> Triple(chroma, 0f, x)
            }

            return Color(
                red = ((redPrime + match) * 255f).roundToInt()
                    .coerceIn(0, 255),
                green = ((greenPrime + match) * 255f).roundToInt()
                    .coerceIn(0, 255),
                blue = ((bluePrime + match) * 255f).roundToInt()
                    .coerceIn(0, 255),
                alpha = alpha
            )
        }

        private var hue = 0f

        fun nextColor(speed: Float): Color {
            val color = fromHsv(hue, 1f, 1f)
            hue = (hue + 60f * speed) % 360f
            return color
        }
    }
}
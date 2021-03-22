package galaxy.app.destressify

class HanningWindow {
    fun value(i: Int, length: Int): Double {
        return if (i >= 0 && i <= length - 1) {
            0.5 - 0.5 * Math.cos(2 * Math.PI * i / (length - 1))
        } else {
            0.0
        }
    }

    fun normalization(length: Int): Double {
        var normal = 0.0
        for (i in 0..length) {
            normal += value(i, length)
        }
        return normal / length
    }
}
package com.cartoniq.lexiforge.domain

import com.cartoniq.lexiforge.data.CardEntity
import kotlin.math.roundToInt

/**
 * Strict SM-2 implementation.
 *  - quality < 3  -> repetition = 0, interval = 1
 *  - quality >= 3 -> interval = 1 / 6 / round(interval * EF), repetition += 1
 *  - EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)), min 1.3
 */
object SM2Algorithm {
    const val AGAIN = 1
    const val HARD = 3
    const val GOOD = 4
    const val EASY = 5

    const val PASS_THRESHOLD = 3
    const val MIN_EASE_FACTOR = 1.3
    const val INITIAL_EASE_FACTOR = 2.5

    data class Result(val interval: Int, val repetition: Int, val easeFactor: Double)

    fun calculate(interval: Int, repetition: Int, easeFactor: Double, quality: Int): Result {
        val newInterval: Int
        val newRepetition: Int

        if (quality < PASS_THRESHOLD) {
            newRepetition = 0
            newInterval = 1
        } else {
            newInterval = when (repetition) {
                0 -> 1
                1 -> 6
                else -> (interval * easeFactor).roundToInt().coerceAtLeast(1)
            }
            newRepetition = repetition + 1
        }

        val q = (5 - quality).toDouble()
        var newEase = easeFactor + (0.1 - q * (0.08 + q * 0.02))
        if (newEase < MIN_EASE_FACTOR) newEase = MIN_EASE_FACTOR

        return Result(newInterval, newRepetition, newEase)
    }

    fun applyReview(card: CardEntity, quality: Int, today: Long, nowMillis: Long): CardEntity {
        val r = calculate(card.interval, card.repetition, card.easeFactor, quality)
        return card.copy(
            interval = r.interval,
            repetition = r.repetition,
            easeFactor = r.easeFactor,
            nextReviewDate = today + r.interval,
            lastReviewedAt = nowMillis
        )
    }

    fun previewInterval(card: CardEntity, quality: Int): Int =
        calculate(card.interval, card.repetition, card.easeFactor, quality).interval
}

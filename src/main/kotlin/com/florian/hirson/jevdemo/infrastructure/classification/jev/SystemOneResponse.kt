package com.florian.hirson.jevdemo.infrastructure.classification.jev

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/** The jev API response body: one answer per requested question, keyed identically. */
data class SystemOneResponse(
    val model: String,
    val answers: Map<String, Answer>,
    val usage: Usage,
)

data class Usage(
    @JsonProperty("input_tokens") val inputTokens: Int,
    @JsonProperty("output_tokens") val outputTokens: Int,
)

/**
 * A jev answer, discriminated on the wire by [type] — see
 * https://docs.typesafe.ai/primitives.md and https://docs.typesafe.ai/confidence.md.
 * Per jev-1.13's documented jaggedness, `confidence` is not comparable across
 * answer types: a `noul` carries none at all.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(Answer.Choice::class, name = "choice"),
    JsonSubTypes.Type(Answer.Score::class, name = "score"),
    JsonSubTypes.Type(Answer.Noul::class, name = "noul"),
)
sealed interface Answer {
    val type: String

    data class Choice(
        val choice: String,
        val confidence: Double,
        val probabilities: Map<String, Double>,
        override val type: String = "choice",
    ) : Answer

    data class Score(
        val score: Double,
        val legend: Map<String, String>,
        val confidence: Double,
        val probabilities: Map<String, Double>,
        override val type: String = "score",
    ) : Answer

    data class Noul(
        val noul: Double,
        override val type: String = "noul",
    ) : Answer
}

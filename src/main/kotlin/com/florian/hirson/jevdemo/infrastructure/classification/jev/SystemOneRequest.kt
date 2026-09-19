package com.florian.hirson.jevdemo.infrastructure.classification.jev

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/** The jev API request body: one state, evaluated against every question in parallel. */
data class SystemOneRequest(
    val state: String,
    val model: String,
    val questions: Map<String, Question>,
)

/**
 * A jev question, discriminated on the wire by [type]. Each variant mirrors
 * one jev primitive (noul/choice/score) exactly as documented — see
 * https://docs.typesafe.ai/primitives.md.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(Question.Choice::class, name = "choice"),
    JsonSubTypes.Type(Question.Score::class, name = "score"),
    JsonSubTypes.Type(Question.Noul::class, name = "noul"),
)
sealed interface Question {
    val type: String
    val instructions: String

    /** Pick one option from a fixed list of up to 255. */
    data class Choice(
        override val instructions: String,
        val criteria: Map<String, String?>,
        override val type: String = "choice",
    ) : Question

    /** Rate the state along 2 to 10 ordered levels. */
    data class Score(
        override val instructions: String,
        val criteria: List<String>,
        override val type: String = "score",
    ) : Question

    /** Assess how true a statement is, as a 0..1 probability. */
    data class Noul(
        override val instructions: String,
        val criteria: NoulCriteria? = null,
        override val type: String = "noul",
    ) : Question
}

/** Optional clarification of what "true" and "false" mean for a [Question.Noul]. */
data class NoulCriteria(
    @JsonProperty("true") val whenTrue: String? = null,
    @JsonProperty("false") val whenFalse: String? = null,
)

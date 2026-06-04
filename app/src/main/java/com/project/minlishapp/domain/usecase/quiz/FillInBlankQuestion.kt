package com.project.minlishapp.domain.usecase.quiz

data class FillInBlankQuestion(
    val cardId: String,
    val sentence: String,
    val correctAnswer: String,
    val meaning: String,
    val phonetic: String? = null
)

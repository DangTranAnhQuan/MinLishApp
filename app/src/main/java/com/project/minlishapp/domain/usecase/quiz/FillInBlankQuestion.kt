package com.project.minlishapp.domain.usecase.quiz

data class FillInBlankQuestion(
    val cardId: String,
    val sentenceWithBlank: String,
    val correctAnswer: String,
    val phonetic: String? = null,
    val meaning: String? = null
)

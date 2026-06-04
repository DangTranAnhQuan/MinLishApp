package com.project.minlishapp.domain.usecase.quiz

data class MultipleChoiceQuestion(
    val cardId: String,
    val word: String,
    val options: List<String>,
    val correctAnswer: String,
    val imageUrl: String? = null
)

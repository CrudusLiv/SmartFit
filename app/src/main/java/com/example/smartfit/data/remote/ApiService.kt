package com.example.smartfit.data.remote

data class ExerciseInfoResponse(
    val count: Int? = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<ExerciseInfo> = emptyList()
)

data class ExerciseInfo(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val category: ExerciseCategory? = null,
    val muscles: List<Muscle> = emptyList(),
    val muscles_secondary: List<Muscle> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val images: List<ExerciseImage> = emptyList()
)

data class ExerciseCategory(
    val id: Int? = null,
    val name: String? = null
)

data class Muscle(
    val id: Int? = null,
    val name: String? = null,
    val name_en: String? = null
)

data class Equipment(
    val id: Int? = null,
    val name: String? = null
)

data class ExerciseImage(
    val id: Int? = null,
    val image: String? = null,
    val is_main: Boolean? = null
)


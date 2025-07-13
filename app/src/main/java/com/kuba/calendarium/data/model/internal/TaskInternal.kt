package com.kuba.calendarium.data.model.internal

import java.util.UUID

data class TaskInternal(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val done: Boolean = false
)

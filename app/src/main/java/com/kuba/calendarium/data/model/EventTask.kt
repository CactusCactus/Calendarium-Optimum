package com.kuba.calendarium.data.model

import java.util.UUID

data class EventTask(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val done: Boolean = false
)
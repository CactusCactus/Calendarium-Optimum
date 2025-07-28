package com.kuba.calendarium.data.model.internal

import com.kuba.calendarium.data.model.Task
import kotlin.random.Random

data class TaskCreationData(
    val id: Long = Random.nextLong(),
    val title: String,
    val done: Boolean = false
) {
    fun toTask(position : Int) = Task(id = id, title = title, position = position, done = done)
}

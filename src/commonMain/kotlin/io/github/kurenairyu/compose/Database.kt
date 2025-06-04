package io.github.kurenairyu.compose

interface Database {
    fun getAll(): List<Item>
    fun getById(id: Long): Item
}
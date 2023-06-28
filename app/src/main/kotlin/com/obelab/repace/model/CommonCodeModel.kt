package com.obelab.repace.model

data class CommonCodeModel(
    val code: String,
    val createdAt: String,
    val deleteAt: Any,
    val ext1: Any,
    val ext2: Any,
    val id: Int,
    val isActive: Boolean,
    val name: String,
    val order: Int,
    val parentId: Int,
    val updatedAt: String,
    val value: String
)
package com.obelab.repace.model

import com.obelab.repace.core.extension.empty

data class UserModel(val id: Int, val name: String) {

    companion object {
        val empty = UserModel(0, String.empty())
    }

    fun toToUser() = UserModel(id, name)
}
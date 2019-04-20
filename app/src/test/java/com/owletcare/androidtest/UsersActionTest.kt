package com.owletcare.androidtest

import org.junit.Test

class UsersActionTest {
    @Test
    fun actionAddUser() {
        val state = UsersAction.AddUser("test", "profilePicture")
            .reduce(arrayListOf())
        assert(state.find { it.name == "test" && it.profilePicture == "profilePicture" } !== null)
    }

    @Test
    fun actionRemoveUser() {
        val user = User("name", "profilePicture")
        val state = arrayListOf(user)
        assert(UsersAction.RemoveUser(user).reduce(state).size == 0)
    }
}
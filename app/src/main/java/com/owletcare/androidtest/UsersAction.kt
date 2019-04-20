package com.owletcare.androidtest

import android.util.Log
import com.owletcare.androidtest.redux.Action

/**
 * StorageAction.kt
 * OwletBuggyAndroid
 *
 * Created by kvanry on 4/15/19.
 * Copyright (c) 2019. Owlet Care. All rights reserved worldwide.
 */
sealed class UsersAction: Action() {

    abstract fun reduce(state: ArrayList<User>): ArrayList<User>

    class AddUser(val name: String, val profilePicture: String): UsersAction() {
        override fun reduce(state: ArrayList<User>): ArrayList<User> {
            state.add(User(name, profilePicture))
            return state
        }
    }

    class RemoveUser(val user: User): UsersAction() {
        override fun reduce(state: ArrayList<User>): ArrayList<User> {
            state.remove(user)
            return state
        }
    }

}

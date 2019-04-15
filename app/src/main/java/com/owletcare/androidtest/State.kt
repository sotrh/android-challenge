package com.owletcare.androidtest

/**
 * State.kt
 * OwletCore-Android
 *
 * Created by Kody Van Ry on 3/12/18.
 * Copyright (c) 2018. Owlet Baby Care. All rights reserved worldwide.
 */

data class State(
    val users: ArrayList<User>
)

data class User(val name: String, val profilePicture: String)

package com.owletcare.androidtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.owletcare.androidtest.redux.Store

/**
 * UserRecyclerAdapter.kt
 * OwletBuggyAndroid
 *
 * Created by kvanry on 4/15/19.
 * Copyright (c) 2019. Owlet Care. All rights reserved worldwide.
 */
class UserRecyclerAdapter(private val store: Store<State>): RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {

    private val usersDisplayed: ArrayList<User> = arrayListOf()

    val subscriber: (ArrayList<User>) -> Unit = { users ->
        // TODO update the displayed users
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        store.subscribe(subscriber) { it.users }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_list_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usersDisplayed.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(usersDisplayed[position])
    }

    inner class UserViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(user: User) {
            // TODO Bind the user to the view and allow the user to be deleted
        }
    }
}
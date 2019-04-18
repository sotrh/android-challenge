package com.owletcare.androidtest

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.owletcare.androidtest.redux.Store
import kotlinx.android.synthetic.main.user_list_item.view.*
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * UserRecyclerAdapter.kt
 * OwletBuggyAndroid
 *
 * Created by kvanry on 4/15/19.
 * Copyright (c) 2019. Owlet Care. All rights reserved worldwide.
 */
class UserRecyclerAdapter(private val store: Store<State>): RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>() {

    private val usersDisplayed: ArrayList<User> = arrayListOf()
    private val positionsChanged: ArrayList<Int> = arrayListOf()

    val subscriber: (ArrayList<User>) -> Unit = { users ->
        positionsChanged.clear()
        (users zip usersDisplayed).forEachIndexed { index, (newUser, oldUser) ->
            if (newUser != oldUser)
                positionsChanged.add(index)
        }

        val oldSize = usersDisplayed.size
        val numUsersAdded = users.size - oldSize

        usersDisplayed.clear()
        usersDisplayed.addAll(users)

        positionsChanged.forEach { notifyItemChanged(it) }

        if (numUsersAdded > 0) {
            notifyItemRangeInserted(oldSize, numUsersAdded)
        } else if (numUsersAdded < 0) {
            notifyItemRangeRemoved(oldSize, abs(numUsersAdded))
        }
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

            view.user_name.text = user.name
            view.user_profilePicture.setImageURI(Uri.parse(user.profilePicture))
        }
    }
}
package com.owletcare.androidtest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.owletcare.androidtest.redux.Store
import kotlinx.android.synthetic.main.user_list_item.view.*
import kotlinx.coroutines.*
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

/**
 * UserRecyclerAdapter.kt
 * OwletBuggyAndroid
 *
 * Created by kvanry on 4/15/19.
 * Copyright (c) 2019. Owlet Care. All rights reserved worldwide.
 */
class UserRecyclerAdapter(private val store: Store<State>):
    RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>(),
    CoroutineScope {

    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job

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
            launch {
                view.user_name.text = user.name
                view.user_profilePicture.setImageResource(R.drawable.ic_person_black_24dp)
                view.user_profilePicture_progressBar.visibility = View.VISIBLE

                val bitmapResult = withContext(Dispatchers.Default) {
                    try {
                        val url = URL(user.profilePicture)
                        val connection = url.openConnection().apply {
                            doInput = true
                            connect()
                        }
                        val input = connection.getInputStream()

                        Result.success(BitmapFactory.decodeStream(input))
                    } catch(e: Throwable) {
                        Result.failure<Bitmap>(e)
                    }
                }

                if (bitmapResult.isFailure) {
                    Toast.makeText(
                        view.context,
                        "Unable to process profile image url ${user.profilePicture}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    bitmapResult.getOrNull()?.let { view.user_profilePicture.setImageBitmap(it) }
                }

                view.user_profilePicture_progressBar.visibility = View.INVISIBLE
            }
        }
    }
}
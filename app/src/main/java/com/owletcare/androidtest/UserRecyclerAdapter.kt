package com.owletcare.androidtest

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.owletcare.androidtest.redux.Store
import kotlinx.android.synthetic.main.user_list_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * UserRecyclerAdapter.kt
 * OwletBuggyAndroid
 *
 * Created by kvanry on 4/15/19.
 * Copyright (c) 2019. Owlet Care. All rights reserved worldwide.
 */
class UserRecyclerAdapter(private val store: Store<State>, private val bitmapCache: BitmapCache):
    RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder>(), CoroutineScope {

    override val coroutineContext = Dispatchers.Main

    private val usersDisplayed: ArrayList<User> = arrayListOf()
    private val userPositionsChanged: ArrayList<Int> = arrayListOf()

    val subscriber: (ArrayList<User>) -> Unit = { users ->

        userPositionsChanged.clear()
        (users zip usersDisplayed).forEachIndexed { index, (new, old) ->
            if (new != old) userPositionsChanged.add(index)
        }

        val oldSize = usersDisplayed.size
        val numUsersInserted = users.size - oldSize

        usersDisplayed.clear()
        usersDisplayed.addAll(users)

        userPositionsChanged.forEach { notifyItemChanged(it) }
        if (numUsersInserted > 0) notifyItemRangeInserted(oldSize, numUsersInserted)
        if (numUsersInserted < 0) notifyItemRangeRemoved(oldSize, abs(numUsersInserted))
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

    inner class UserViewHolder(val view: View):
        RecyclerView.ViewHolder(view) {

        fun bind(user: User) {
            launch {
                view.user_name.text = user.name
                view.user_profilePicture.setImageResource(R.drawable.ic_person_black_24dp)
                view.user_profilePicture_progressBar.visibility = View.VISIBLE
                view.user_delete.setOnClickListener {
                    store.dispatch(UsersAction.RemoveUser(user))
                }

                val bitmapResult = try {
                    Result.success(bitmapCache.getBitmapAsync(user.profilePicture).await())
                } catch (t: Throwable) {
                    Result.failure<Bitmap>(t)
                }

                if (bitmapResult.isFailure) {
                    Toast.makeText(
                        view.context,
                        view.resources.getString(R.string.error_profile_picture, user.profilePicture),
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
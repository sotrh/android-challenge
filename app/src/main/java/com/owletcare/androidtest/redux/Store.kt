package com.owletcare.androidtest.redux

/**
 * Store.java
 * OwletCore-Android
 *
 * Created by David N. Junod on 2/23/18.
 * Copyright (c) 2018. Owlet Baby Care. All rights reserved worldwide.
 */

import java.util.*

@Suppress("UNCHECKED_CAST")
/**
 * Store is the single source of truth for the application
 *
 * @param State This is the application implementation of State, should have a reference to the
 * Core [State]
 * @param Action This is the application implementation of Action, this should be a a sealed class
 * that has a subclass referencing [Action]
 * @property reducer This is the main reducer. This should have sub-reducers that are pure
 * functions.
 * @property state This is the application implementation of State
 * @property middlewares This is for all middleware (Logging, Network Requests, etc.)
 */
class Store<State : Any?>(
    private val reducer: (Action, State) -> State,
    state: State,
    vararg middlewares: (Store<State>, Action, (Action) -> Unit) -> Unit
) {

    /**
     * The `State` a plain object that describes the entire state of the application
     */
    var state: State = state
        private set

    private val subscriptions = ArrayList<Subscription<State, Any?>>()

    private val next = ArrayList<(Action) -> Unit>()

    /**
     * This is will always be the last [Middleware] to be dispatched and it will update the [Store] state
     */
    private val dispatcher: (Store<State>, Action, (Action) -> Unit) -> Unit = { store, action, _ ->
        synchronized(this)
        {
            this.state = store.reducer(action, this.state)
        }
        val subscriptionsArrayList = ArrayList<Subscription<State, Any?>>()
        subscriptionsArrayList.addAll(subscriptions)
        subscriptionsArrayList.forEach { subscription ->
            subscription.subscriber(subscription.map(this.state))
        }
    }

    init {
        this.next.add { action ->
            dispatcher(this, action) {}
        }

        middlewares.reversed().forEach { mw ->
            val n = next[0]
            next.add(0) { action ->
                mw(this, action, n)
            }
        }
    }

    /**
     * Dispatch an [Action] to the [Store]
     *
     * @param action the [Action] to be dispatched
     */
    fun dispatch(action: Action): State? {
        this.next[0](action)
        return this.state
    }

    /**
     * Subscribe for to the [Store] to receive [State] updates
     *
     * @param subscriber The subscriber that will be called when the [State] updates
     */
    fun subscribe(subscriber: (State) -> Unit) {
        val subscription = Subscription<State, State>(subscriber) { return@Subscription it }
        subscription.subscriber(subscription.map(this.state))
        this.subscriptions.add(subscription as Subscription<State, Any?>)

    }

    /**
     * Subscribe for to the [Store] to receive [State] updates
     *
     * @param SubscribedState The subscriber that will be called when the MappedState] updates
     * @param subscriber The subscriber that will be called when the MappedState] updates
     */
    fun <SubscribedState : Any?> subscribe(subscriber: (SubscribedState) -> Unit, map: (State) -> SubscribedState) {
        val subscription = Subscription(subscriber, map)
        subscription.subscriber(subscription.map(this.state))
        this.subscriptions.add(subscription as Subscription<State, Any?>)
    }

    /**
     * Unsubscribe from the [Store] to stop receiving [State] updates
     *
     * @param subscriber The subscriber that will stop receiving [State] updates
     */
    fun <SubscribedState> unsubscribe(subscriber: (SubscribedState) -> Unit) {
        this.subscriptions.removeAll { it.subscriber == subscriber }
    }

    data class Subscription<in State, MappedState : Any?> constructor(
        val subscriber: (MappedState) -> Unit,
        val map: (State) -> MappedState
    )
}

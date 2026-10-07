package com.xtine.habbitrabbit.data.repo

fun interface StoreChangeNotifier {
    suspend fun notifyChanged()

    companion object {
        val NoOp: StoreChangeNotifier = StoreChangeNotifier { /* no-op */ }
    }
}

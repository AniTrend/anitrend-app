package com.mxt.anitrend.extension

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import java.io.Serializable

/**
 * Typed bundle reads must use the app class loader after parcel recreation.
 * API 33+ otherwise attempts to resolve app Parcelables with the boot loader.
 */
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? {
    T::class.java.classLoader?.let(::setClassLoader)
    return BundleCompat.getParcelable(this, key, T::class.java)
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? {
    T::class.java.classLoader?.let(::setClassLoader)
    return BundleCompat.getParcelableArrayList(this, key, T::class.java)
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? {
    T::class.java.classLoader?.let(::setClassLoader)
    return BundleCompat.getSerializable(this, key, T::class.java)
}

inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? = IntentCompat.getSerializableExtra(this, key, T::class.java)

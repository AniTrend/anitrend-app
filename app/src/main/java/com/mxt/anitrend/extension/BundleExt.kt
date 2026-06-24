package com.mxt.anitrend.extension

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import java.io.Serializable

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = BundleCompat.getParcelable(this, key, T::class.java)

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = BundleCompat.getParcelableArrayList(this, key, T::class.java)

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = BundleCompat.getSerializable(this, key, T::class.java)

inline fun <reified T : Serializable> Intent.serializableExtra(key: String): T? = IntentCompat.getSerializableExtra(this, key, T::class.java)

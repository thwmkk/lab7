package com.example.myapplication

import android.os.Parcel
import android.os.Parcelable

data class Workout(
    var id: Int,
    var title: String,
    var description: String,
    var rating: Float = 0f
) : Parcelable { // Удалили Serializable
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "", // Читаем название
        parcel.readString() ?: "", // Читаем описание
        parcel.readFloat() // Читаем рейтинг
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title) // Записываем название
        parcel.writeString(description) // Записываем описание
        parcel.writeFloat(rating) // Записываем рейтинг
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Workout) return false

        return title == other.title && description == other.description && rating == other.rating
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + rating.hashCode() // Включаем рейтинг в hashCode
        return result
    }

    companion object CREATOR : Parcelable.Creator<Workout> {
        override fun createFromParcel(parcel: Parcel): Workout {
            return Workout(parcel)
        }

        override fun newArray(size: Int): Array<Workout?> {
            return arrayOfNulls(size)
        }
    }
}

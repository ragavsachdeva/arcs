/*
 * Copyright 2019 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */

package arcs.type.parcelables

import android.os.Parcel
import android.os.Parcelable
import arcs.data.CollectionType
import arcs.data.CountType
import arcs.data.EntityType
import arcs.data.ReferenceType
import arcs.data.SingletonType
import arcs.type.Tag
import arcs.type.Type

/** Wrappers for [Type] classes which implements [Parcelable]. */
sealed class ParcelableType(open val actual: Type) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actual.tag.ordinal)
        // Subclasses will write the remainder.
    }

    override fun describeContents(): Int = 0

    /** [Parcelable] variant of [arcs.data.CollectionType]. */
    data class CollectionType(
        override val actual: arcs.data.CollectionType<*>
    ) : ParcelableType(actual) {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeType(actual.collectionType, flags)
        }

        companion object CREATOR : Parcelable.Creator<CollectionType> {
            override fun createFromParcel(parcel: Parcel): CollectionType =
                CollectionType(actual = CollectionType(requireNotNull(parcel.readType())))

            override fun newArray(size: Int): Array<CollectionType?> = arrayOfNulls(size)
        }
    }

    /** [Parcelable] variant of [arcs.data.CollectionType]. */
    data class CountType(
        override val actual: arcs.data.CountType = arcs.data.CountType()
    ) : ParcelableType(actual) {
        // No need to override writeToParcel.

        companion object CREATOR : Parcelable.Creator<CountType> {
            override fun createFromParcel(parcel: Parcel): CountType = CountType()
            override fun newArray(size: Int): Array<CountType?> = arrayOfNulls(size)
        }
    }

    /** [Parcelable] variant of [arcs.data.EntityType]. */
    data class EntityType(
        override val actual: arcs.data.EntityType
    ) : ParcelableType(actual) {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeSchema(actual.entitySchema, flags)
        }

        companion object CREATOR : Parcelable.Creator<EntityType> {
            override fun createFromParcel(parcel: Parcel): EntityType =
                EntityType(actual = EntityType(requireNotNull(parcel.readSchema())))

            override fun newArray(size: Int): Array<EntityType?> = arrayOfNulls(size)
        }
    }

    /** [Parcelable] variant of [arcs.data.ReferenceType]. */
    class ReferenceType(
        override val actual: arcs.data.ReferenceType<*>
    ) : ParcelableType(actual) {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeType(actual.containedType, flags)
        }

        companion object CREATOR : Parcelable.Creator<ReferenceType> {
            override fun createFromParcel(parcel: Parcel): ReferenceType =
                ReferenceType(actual = ReferenceType(requireNotNull(parcel.readType())))

            override fun newArray(size: Int): Array<ReferenceType?> = arrayOfNulls(size)
        }
    }

    /** [Parcelable] variant of [arcs.data.SingletonType]. */
    data class SingletonType(
        override val actual: arcs.data.SingletonType<*>
    ) : ParcelableType(actual) {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeType(actual.containedType, flags)
        }

        companion object CREATOR : Parcelable.Creator<SingletonType> {
            override fun createFromParcel(parcel: Parcel): SingletonType =
                SingletonType(
                    actual = SingletonType(requireNotNull(parcel.readType()))
                )

            override fun newArray(size: Int): Array<SingletonType?> = arrayOfNulls(size)
        }
    }

    companion object CREATOR : Parcelable.Creator<ParcelableType> {
        override fun createFromParcel(parcel: Parcel): ParcelableType =
            when (Tag.values()[parcel.readInt()]) {
                Tag.Collection -> CollectionType.createFromParcel(parcel)
                Tag.Count -> CountType.createFromParcel(parcel)
                Tag.Entity -> EntityType.createFromParcel(parcel)
                Tag.Reference -> ReferenceType.createFromParcel(parcel)
                Tag.Singleton -> SingletonType.createFromParcel(parcel)
            }

        override fun newArray(size: Int): Array<ParcelableType?> = arrayOfNulls(size)
    }
}

/** Converts a raw [Type] to its [ParcelableType] variant. */
fun Type.toParcelable(): ParcelableType = when (tag) {
    Tag.Collection -> ParcelableType.CollectionType(this as CollectionType<*>)
    Tag.Count -> ParcelableType.CountType(this as CountType)
    Tag.Entity -> ParcelableType.EntityType(this as EntityType)
    Tag.Reference -> ParcelableType.ReferenceType(this as ReferenceType<*>)
    Tag.Singleton -> ParcelableType.SingletonType(this as SingletonType<*>)
}

/** Writes a [Type] to the [Parcel]. */
fun Parcel.writeType(type: Type, flags: Int) = writeTypedObject(type.toParcelable(), flags)

/** Reads a [Type] from the [Parcel]. */
fun Parcel.readType(): Type? = readTypedObject(ParcelableType.CREATOR)?.actual

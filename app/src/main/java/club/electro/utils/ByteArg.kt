package club.electro.utils

import android.os.Bundle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object ByteArg: ReadWriteProperty<Bundle, Byte> {
    override fun setValue(thisRef: Bundle, property: KProperty<*>, value: Byte) {
        thisRef.putByte(property.name, value)
    }

    override fun getValue(thisRef: Bundle, property: KProperty<*>): Byte =
        thisRef.getByte(property.name)
}
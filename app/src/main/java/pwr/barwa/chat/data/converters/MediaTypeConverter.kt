package pwr.barwa.chat.data.converters

import androidx.room.TypeConverter
import pwr.barwa.chat.data.model.MediaType

class MediaTypeConverter {
    @TypeConverter
    fun toMediaType(value: String?): MediaType? {
        return value?.let { enumValueOf<MediaType>(it) }
    }

    @TypeConverter
    fun fromMediaType(mediaType: MediaType?): String? {
        return mediaType?.name
    }
}

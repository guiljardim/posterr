package com.example.posterr.data.converters

import androidx.room.TypeConverter
import com.example.posterr.domain.model.PostType

class Converters {

    @TypeConverter
    fun fromPostType(postType: PostType): String {
        return postType.name
    }

    @TypeConverter
    fun toPostType(postTypeString: String): PostType {
        return PostType.valueOf(postTypeString)
    }
}
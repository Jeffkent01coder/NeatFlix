package com.ericg.neatflix.data.response

import com.ericg.neatflix.model.Cast
import com.google.gson.annotations.SerializedName

data class CastResponse(
    @SerializedName("cast")
    val cast: List<Cast>
)

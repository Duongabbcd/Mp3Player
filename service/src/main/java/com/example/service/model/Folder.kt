package com.example.service.model

import java.io.Serializable

data class Folder (
    var name: String = "",
    var path: String = "",
    var tracks: MutableList<Audio> = mutableListOf()
) : Serializable
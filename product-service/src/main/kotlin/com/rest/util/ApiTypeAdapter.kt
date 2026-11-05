package com.rest.util

abstract class ApiTypeAdapter<API_MODEL, SERVICE_MODEL> {

    open fun API_MODEL.toServiceModel(): SERVICE_MODEL {
        TODO("Not yet implemented")
    }

    fun List<API_MODEL>.toServiceModel() = ArrayList<SERVICE_MODEL>(size).apply {
        this@toServiceModel.forEach { add(it.toServiceModel()) }
    }

    fun Set<API_MODEL>.toServiceModel() = HashSet<SERVICE_MODEL>(size).apply {
        this@toServiceModel.forEach { add(it.toServiceModel()) }
    }

    open fun SERVICE_MODEL.toApiModel(): API_MODEL {
        TODO("Not yet implemented")
    }


    fun List<SERVICE_MODEL>.toApiModel() = ArrayList<API_MODEL>(size).apply {
        this@toApiModel.forEach { add(it.toApiModel()) }
    }

    fun Set<SERVICE_MODEL>.toApiModel() = HashSet<API_MODEL>(size).apply {
        this@toApiModel.forEach { add(it.toApiModel()) }
    }

}
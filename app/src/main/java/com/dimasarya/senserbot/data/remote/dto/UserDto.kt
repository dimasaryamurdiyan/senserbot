package com.dimasarya.senserbot.data.remote.dto

import com.dimasarya.senserbot.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
    val address: AddressDto,
    val company: CompanyDto
)

@Serializable
data class AddressDto(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: GeoDto
)

@Serializable
data class GeoDto(
    val lat: String,
    val lng: String
)

@Serializable
data class CompanyDto(
    val name: String,
    val catchPhrase: String,
    val bs: String
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = phone,
        website = website,
        city = address.city,
        companyName = company.name
    )
}

package com.dimasarya.senserbot.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class UserDtoMappingTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = UserDto(
            id = 1,
            name = "Leanne Graham",
            username = "Bret",
            email = "Sincere@april.biz",
            phone = "1-770-736-8031 x56442",
            website = "hildegard.org",
            address = AddressDto(
                street = "Kulas Light",
                suite = "Apt. 556",
                city = "Gwenborough",
                zipcode = "92998-3874",
                geo = GeoDto(lat = "-37.3159", lng = "81.1496")
            ),
            company = CompanyDto(
                name = "Romaguera-Crona",
                catchPhrase = "Multi-layered client-server neural-net",
                bs = "harness real-time e-markets"
            )
        )

        val user = dto.toDomain()

        assertEquals(1, user.id)
        assertEquals("Leanne Graham", user.name)
        assertEquals("Bret", user.username)
        assertEquals("Sincere@april.biz", user.email)
        assertEquals("1-770-736-8031 x56442", user.phone)
        assertEquals("hildegard.org", user.website)
        assertEquals("Gwenborough", user.city)
        assertEquals("Romaguera-Crona", user.companyName)
    }

    @Test
    fun `toDomain maps city from nested address`() {
        val dto = createUserDto(city = "Springfield")
        val user = dto.toDomain()
        assertEquals("Springfield", user.city)
    }

    @Test
    fun `toDomain maps companyName from nested company`() {
        val dto = createUserDto(companyName = "Acme Corp")
        val user = dto.toDomain()
        assertEquals("Acme Corp", user.companyName)
    }

    private fun createUserDto(
        id: Int = 1,
        name: String = "Test User",
        username: String = "testuser",
        email: String = "test@test.com",
        phone: String = "123-456",
        website: String = "test.com",
        city: String = "TestCity",
        companyName: String = "TestCompany"
    ): UserDto = UserDto(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = phone,
        website = website,
        address = AddressDto(
            street = "Street",
            suite = "Suite",
            city = city,
            zipcode = "00000",
            geo = GeoDto(lat = "0.0", lng = "0.0")
        ),
        company = CompanyDto(
            name = companyName,
            catchPhrase = "Phrase",
            bs = "bs"
        )
    )
}
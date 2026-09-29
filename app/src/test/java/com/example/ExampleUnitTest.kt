package com.example

import com.example.data.models.SchoolReview
import com.example.data.repository.SchoolRepository
import com.example.util.ExchangeRateData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    private val repository = SchoolRepository()

    @Test
    fun testLongBayCollegeInRepository() {
        val school = repository.getSchoolById("long_bay_college_nz")
        assertNotNull("Long Bay College should exist in repository", school)
        assertEquals("Long Bay College", school?.name)
        assertEquals("Nueva Zelanda", school?.country)
        assertEquals("https://www.longbaycollege.com", school?.websiteUrl)
        assertTrue(school?.popularPrograms?.isNotEmpty() == true)
    }

    @Test
    fun testExchangeRateCalculation() {
        val fxData = ExchangeRateData(usdToPenRate = 3.75, isLive = true)
        val formatted = fxData.formatUsdAndPen(100.0)
        assertTrue(formatted.contains("100 USD"))
        assertTrue(formatted.contains("375 PEN"))
    }

    @Test
    fun testSchoolReviewSubmission() {
        val initialReviews = repository.getReviewsForSchool("long_bay_college_nz")
        val initialCount = initialReviews.size

        val newReview = SchoolReview(
            id = "test_rev_1",
            schoolId = "long_bay_college_nz",
            studentName = "Renato Vega",
            studentCityPeru = "Lima",
            rating = 5.0,
            programTaken = "ESOL English Pathway",
            reviewText = "Campus increíble junto al mar y excelente comunidad estudiantil.",
            dateString = "Hoy"
        )
        repository.addReview(newReview)

        val updatedReviews = repository.getReviewsForSchool("long_bay_college_nz")
        assertEquals(initialCount + 1, updatedReviews.size)
        assertEquals("Renato Vega", updatedReviews.first().studentName)
    }

    @Test
    fun testLongBayCollegeGalleryAndCaptions() {
        val school = repository.getSchoolById("long_bay_college_nz")
        assertNotNull(school)
        assertEquals(3, school?.galleryImages?.size)
        assertEquals(3, school?.galleryCaptions?.size)
        assertNotNull(school?.featuredImageRes)
    }

    @Test
    fun testFirestoreSyncStatusInitialState() {
        val status = com.example.util.FirestoreSyncStatus()
        assertTrue(status.isCloudConnected)
        assertFalse(status.isSyncing)
        assertEquals("Sincronizado", status.lastSyncedText)
    }
}

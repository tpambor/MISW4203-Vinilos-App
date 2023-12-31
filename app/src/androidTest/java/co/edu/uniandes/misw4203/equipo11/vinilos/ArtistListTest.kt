package co.edu.uniandes.misw4203.equipo11.vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import co.edu.uniandes.misw4203.equipo11.vinilos.pageobjects.ArtistItem
import co.edu.uniandes.misw4203.equipo11.vinilos.pageobjects.ArtistList
import co.edu.uniandes.misw4203.equipo11.vinilos.pageobjects.Login
import co.edu.uniandes.misw4203.equipo11.vinilos.pageobjects.NavBar
import co.edu.uniandes.misw4203.equipo11.vinilos.ui.MainActivity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Rule
import org.junit.Test

class ArtistListTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun clickAndShowListMusicians(navbar: NavBar, artistList: ArtistList) {
        // When I click on the "Músicos" tab
        navbar.getArtistButton().assertIsDisplayed().performClick()

        // Then I see a list of all musicians
        artistList.selectMusiciansTab().performClick()
        assert(artistList.getArtists().fetchSemanticsNodes().isNotEmpty())
    }

    private fun clickAndShowListBands(navbar: NavBar, artistList: ArtistList) {
        // When I click on the "Bandas" tab
        navbar.getArtistButton().assertIsDisplayed().performClick()

        // Then I see a list of all bands
        artistList.selectBandsTab().performClick()
        assert(artistList.getArtists().fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun showsMusiciansCollector() {
        // Given I login as a collector
        val login = Login(composeTestRule)
        login.getCollectorButton().performClick()

        // When - Then explained in clickAndShowListMusicians function
        val navbar = NavBar(composeTestRule)
        val artistList = ArtistList(composeTestRule)
        clickAndShowListMusicians(navbar, artistList)

        // And I can see Fav buttons
        assert(artistList.hasFavButtons())
    }

    @Test
    fun showsMusiciansVisitor() {
        // Given I login as a visitor
        val login = Login(composeTestRule)
        login.getVisitorButton().performClick()

        // When - Then explained in clickAndShowListMusicians function
        val navbar = NavBar(composeTestRule)
        val artistList = ArtistList(composeTestRule)
        clickAndShowListMusicians(navbar, artistList)

        // And I can't see Fav buttons
        assertFalse(artistList.hasFavButtons())
    }

    @Test
    fun showsBandsCollector() {
        // Given I login as a collector
        val login = Login(composeTestRule)
        login.getCollectorButton().performClick()

        // When - Then explained in clickAndShowListBands function
        val navbar = NavBar(composeTestRule)
        val artistList = ArtistList(composeTestRule)
        clickAndShowListBands(navbar, artistList)

        // And I can see Fav buttons
        assert(artistList.hasFavButtons())
    }

    @Test
    fun showsBandsVisitor() {
        // Given I login as a visitor
        val login = Login(composeTestRule)
        login.getVisitorButton().performClick()

        // When - Then explained in clickAndShowListBands function
        val navbar = NavBar(composeTestRule)
        val artistList = ArtistList(composeTestRule)
        clickAndShowListBands(navbar, artistList)

        // And I can't see Fav buttons
        assertFalse(artistList.hasFavButtons())
    }

    @Test
    fun addRemoveFavoriteMusician() {
        // Given I login as a collector
        val login = Login(composeTestRule)
        login.getCollectorButton().performClick()

        // When - Then explained in clickAndShowListMusicians function
        val navbar = NavBar(composeTestRule)
        val artistList = ArtistList(composeTestRule)
        clickAndShowListMusicians(navbar, artistList)

        val list = artistList.getArtists()
        val artist = ArtistItem(list[0])

        // And there is a artist that is (not) yet my favorite
        val isFavorite = artist.isFavorite()
        assertEquals(!isFavorite, artist.isNotFavorite())

        // And I click on the fav button to make it (not) one of my favorite artists
        artist.clickFavorite()

        // Then the fav button is checked and the artist is one of my favorite artists
        composeTestRule.waitUntil(5000) {
            if (isFavorite)
                artist.isNotFavorite()
            else
                artist.isFavorite()
        }

        // And I click again on the fav button to make it (not) one of my favorite artists
        artist.clickFavorite()

        // Then the fav button is unchecked and the artist is no longer one of my favorite artists
        composeTestRule.waitUntil(5000) {
            if (isFavorite)
                artist.isFavorite()
            else
                artist.isNotFavorite()
        }
    }

    @Test
    fun addRemoveFavoriteBand() {
        // Given I login as a collector
        val login = Login(composeTestRule)
        login.getCollectorButton().performClick()

        // When - Then explained in clickAndShowListMusicians function
        val navbar = NavBar(composeTestRule)
        val artistList = ArtistList(composeTestRule)
        clickAndShowListBands(navbar, artistList)

        val list = artistList.getArtists()
        val artist = ArtistItem(list[0])

        // And there is a artist that is (not) yet my favorite
        val isFavorite = artist.isFavorite()
        assertEquals(!isFavorite, artist.isNotFavorite())

        // And I click on the fav button to make it (not) one of my favorite artists
        artist.clickFavorite()

        // Then the fav button is checked and the artist is one of my favorite artists
        composeTestRule.waitUntil(5000) {
            if (isFavorite)
                artist.isNotFavorite()
            else
                artist.isFavorite()
        }

        // And I click again on the fav button to make it (not) one of my favorite artists
        artist.clickFavorite()

        // Then the fav button is unchecked and the artist is no longer one of my favorite artists
        composeTestRule.waitUntil(5000) {
            if (isFavorite)
                artist.isFavorite()
            else
                artist.isNotFavorite()
        }
    }
}

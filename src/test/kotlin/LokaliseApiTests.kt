import org.junit.Assert
import org.junit.Test
import org.rnazarevych.lokalise.api.Api
import org.rnazarevych.lokalise.api.Api2
import org.rnazarevych.lokalise.api.dto.DownloadParams


const val projectId = ""
const val token = ""

class LokaliseApiTest : Assert() {

    @Test
    fun fetchTranslationsTest() {
        val response = Api.api.fetchTranslations(token, projectId, "['ru']").execute()

        println(response.body())
    }

    @Test
    fun downloadFilesTest() {
        Api2.configure(projectId)

        val response = Api2.api.downloadFiles(token, DownloadParams()).execute()

        assertNotNull(response.body()!!.bundleUrl)
    }
}
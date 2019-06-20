import me.eremkin.lokalise.api.AndroidLokalizeApi2
import me.eremkin.lokalise.api.dto.DownloadParams
import org.junit.Assert
import org.junit.Test
import org.rnazarevych.lokalise.api.Api


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
        AndroidLokalizeApi2.configure(projectId)

        val response = AndroidLokalizeApi2.api.downloadFiles(token, DownloadParams()).execute()

        assertNotNull(response.body()!!.bundleUrl)
    }
}
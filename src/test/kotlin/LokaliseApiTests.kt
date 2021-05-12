//import me.eremkin.lokalise.api.Api2
//import me.eremkin.lokalise.api.dto.DownloadParams
//import org.junit.Assert
//import org.junit.Test
//import org.rnazarevych.lokalise.api.Api
//
//
//const val projectId = "300301045bb21fb08d45d1.20700460"
//const val token = "002dbc955f8964e7bdc67061c7000b5651c7337a"
//
//class LokaliseApiTest : Assert() {
//
//    @Test
//    fun fetchTranslationsTest() {
//        val response = Api.api.fetchTranslations(token, projectId, "['ru']").execute()
//
//        println(response.body())
//    }
//
//    @Test
//    fun downloadFilesTest() {
//        Api2.configure(projectId)
//
//        val langParam = mutableListOf<String>().apply {
//            add("ru")
//        }
//
//        val response = Api2.api.downloadFiles(token, DownloadParams(langs = langParam)).execute()
//
//        assertNotNull(response.body()!!.bundleUrl)
//    }
//}
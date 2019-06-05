package me.eremkin.lokalise.tasks

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object TranslationUpdater {

    fun mergeStrings(orgXml: File, newXml: File, resultXml: File = orgXml) {

        val finalDocument = mergeXml(orgXml, newXml)

        TransformerFactory.newInstance().newTransformer()
            .apply {
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                setOutputProperty(OutputKeys.INDENT, "yes")
            }
            .apply {
                val domSource = DOMSource(finalDocument)
                val streamResult = StreamResult(resultXml)
                transform(domSource, streamResult)
            }
    }

    private fun mergeXml(originalContent: File, newContent: File): Document {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().run {
            val xmlOrgDoc = parse(originalContent)
            val xmlOrgEle = xmlOrgDoc.getDocumentElement()

            println("Root Node:" + xmlOrgEle.nodeName)
            val stringsOrg = xmlOrgDoc.getElementsByTagName("string")
            println("Original strings:" + stringsOrg.length)

            val xmlNewgDoc = parse(newContent)
            val stringsNew = xmlNewgDoc.getElementsByTagName("string")
            println("New strings:" + stringsNew.length)

            val newStringMap = mutableMapOf<String, Node>().let { map ->
                with(stringsNew) {
                    for (i in 0 until length) {
                        val string = item(i) as Element
                        val id = string.attributes.getNamedItem("name").nodeValue
                        map[id] = string
                    }
                }
                map
            }

            println("Updating existing entries")
            with(stringsOrg) {
                for (i in 0 until length) {
                    val string = item(i) as Element
                    val id = string.attributes.getNamedItem("name").nodeValue
                    newStringMap[id]?.let {
                        string.textContent = it.textContent
                        newStringMap.remove(id)
                    }
                }
            }

            println("Writing ${newStringMap.size} new entries")
            if (newStringMap.isNotEmpty()) {
                //xmlOrgDoc.documentElement.appendChild(xmlOrgDoc.createComment("New translations added at ${LocalDateTime.now()}\n"))
                for (node in newStringMap.values) {
                    val xmlImportedNode = xmlOrgDoc.importNode(node, true)
                    xmlOrgEle.appendChild(xmlImportedNode)
                }
            }
            xmlOrgDoc
        }
    }
}
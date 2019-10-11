package me.eremkin.lokalise.tasks

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object TranslationUpdater {

    fun mergeStrings(orgXml: File, newXml: File, resultXml: File = orgXml) {

        val document = mergeXml(orgXml, newXml)
        val domSource = DOMSource(document)

        val textStream = StreamResult(StringWriter())
        val resultStream = StreamResult(resultXml)

        TransformerFactory.newInstance().newTransformer()
            .apply {
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
                setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                setOutputProperty(OutputKeys.INDENT, "yes")
                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                transform(domSource, resultStream)
            }
    }

    private fun mergeXml(originalContent: File, newContent: File): Document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().run {
            val xmlResultDoc = newDocument()
            val xmlResultEle = xmlResultDoc.createElement("resources")
            xmlResultDoc.appendChild(xmlResultEle)

            val xmlOrgDoc = parse(originalContent)
            val xmlOrgEle = xmlOrgDoc.getDocumentElement()

            val stringsOrg = xmlOrgDoc.getElementsByTagName("string")
            println("Original file contains ${stringsOrg.length} strings")

            val xmlNewgDoc = parse(newContent)
            val stringsNew = xmlNewgDoc.getElementsByTagName("string")
            println("New file contains ${stringsNew.length} strings")

            val newStringMap = mutableMapOf<String, Node>()

            with(stringsNew) {
                for (i in 0 until length) {
                    val element = item(i) as Element
                    val id = element.attributes.getNamedItem("name").nodeValue
                    newStringMap[id] = element
                }
            }

            println("Updating existing entries")
            with(stringsOrg) {
                for (i in 0 until length) {
                    val element = item(i) as Element
                    val id = element.attributes.getNamedItem("name").nodeValue
                    newStringMap[id]?.let {
                        element.textContent = it.textContent
                        newStringMap.remove(id)
                    }
                }
            }

            with(xmlOrgEle.childNodes) {
                for (i in 0 until length) {
                    val node = xmlResultDoc.importNode(item(i), true)
                    xmlResultEle.appendChild(node)
                }
            }

            if (newStringMap.isNotEmpty()) {
                println("Writing ${newStringMap.size} new entries")
                for (stringNode in newStringMap.values) {
                    val node = xmlResultDoc.importNode(stringNode, true)
                    xmlResultEle.appendChild(node)
                }
            }
            xmlResultDoc
        }
}
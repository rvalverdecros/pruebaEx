// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.time.LocalDate
import javax.xml.parsers.DocumentBuilderFactory
import java.util.logging.Level
import java.util.logging.LogManager

internal val l = LogManager.getLogManager().getLogger("").apply { level = Level.ALL }
internal fun i(tag: String, msg: String) {
    l.info("[$tag] - $msg")
}

interface LeerLibro {
    fun leer(pathName: String): Document {
        val archivo = File(pathName)
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(archivo)

    }

    fun obtenerAtributosEnMapKV(e: Element): MutableMap<String, String> {
        val mMap = mutableMapOf<String, String>()
        for (j in 0..e.attributes.length - 1)
            mMap.putIfAbsent(e.attributes.item(j).nodeName, e.attributes.item(j).nodeValue)
        return mMap
    }

    fun obtenerListaNodosPorNombre(doc: Document, tagName: String): MutableList<Node> {
        val bookList: NodeList = doc.getElementsByTagName(tagName)
        val lista = mutableListOf<Node>()
        for (i in 0..bookList.length - 1)
            lista.add(bookList.item(i))
        return lista
    }
}
open class gestionLibrosIU(cargador: String):LeerLibro{

    private var xmlDoc: Document? = null
    init {
        try {
            xmlDoc = leer(cargador)
            xmlDoc?.let { it.documentElement.normalize() }
        } catch (e: Exception) {
            requireNotNull(xmlDoc , { e.message.toString() })
        }
    }


    fun existeLibro(idLibro: String): Boolean {
        var existe: Boolean
        if (idLibro.isNullOrBlank())
            existe = false
        else {
            var encontrado = xmlDoc?.let {
                var nodosLibro = obtenerListaNodosPorNombre(it, "book")
                ((nodosLibro.indexOfFirst {
                    if (it.getNodeType() === Node.ELEMENT_NODE) {
                        val elem = it as Element
                        obtenerAtributosEnMapKV(elem)["id"] == idLibro
                    } else
                        false
                }) >= 0)
            }
            existe = (encontrado != null && encontrado)
        }
        return existe
    }
    fun infoLibro(idLibro: String): Map<String, Any> {
        var m = mutableMapOf<String, Any>()
        if (!idLibro.isNullOrBlank())
            xmlDoc?.let {
                var nodosLibro = obtenerListaNodosPorNombre(it, "book")

                var posicionDelLibro = nodosLibro.indexOfFirst {
                    if (it.getNodeType() === Node.ELEMENT_NODE) {
                        val elem = it as Element
                        obtenerAtributosEnMapKV(elem)["id"] == idLibro
                    } else false
                }
                if (posicionDelLibro >= 0) {
                    if (nodosLibro[posicionDelLibro].getNodeType() === Node.ELEMENT_NODE) {
                        val elem = nodosLibro[posicionDelLibro] as Element
                        m.put("id", idLibro)
                        m.put("author", elem.getElementsByTagName("author").item(0).textContent)
                        m.put("genre", elem.getElementsByTagName("genre").item(0).textContent)
                        m.put("price", elem.getElementsByTagName("price").item(0).textContent.toDouble())
                        m.put(
                            "publish_date",
                            LocalDate.parse(elem.getElementsByTagName("publish_date").item(0).textContent)
                        )
                        m.put("description", elem.getElementsByTagName("description").item(0).textContent)
                    }
                }
            }
        return m
    }


}
open class GestorDeLibrosIUT1(cargador: String): gestionLibrosIU(cargador) {
    open fun preguntarPorUnLibro() {
        println("Introduzca un ID: ")
        var idLibro = readLine().toString()
        if (existeLibro(idLibro))
            println("El libro $idLibro existe!")
        else
            println("El libro $idLibro NO existe!")
    }
    open fun mostrarInfoDeUnLibro()
    {
        println("Introduzca un ID: ")
        var idLibro = readLine().toString()
        var infoLibro = infoLibro(idLibro)
        if (!infoLibro.isEmpty())
            println("La información sobre es la siguiente\n$infoLibro")
        else
            println("No se encontró información sobre el libro")
    }
}

open class gestionLibros(cargador:String): GestorDeLibrosIUT1(cargador) {

    var cat = leer(cargador)
    override fun preguntarPorUnLibro() {
        super.preguntarPorUnLibro()
    }

    override fun mostrarInfoDeUnLibro() {
        super.mostrarInfoDeUnLibro()
    }

}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val botonarchivo by remember { mutableStateOf("Pulsa para confirmar") }
    val botonnuevo by remember { mutableStateOf("Pulsa para cambiar el tipo de archivo") }
    var nuevoarchivo by remember { mutableStateOf(false) }
    var nuevodialogo by remember { mutableStateOf(false) }
    var archivo by remember { mutableStateOf("") }
    if (nuevodialogo) {
        Dialog(
            onCloseRequest = { nuevodialogo = false },
            onPreviewKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                    nuevodialogo = false
                    true
                } else {
                    false
                }
            }) {
            val inputStream = File(archivo).inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            Text(inputString)

        }
    }
    if (nuevoarchivo) {
        Dialog(
            onCloseRequest = { nuevodialogo = false },
            onPreviewKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                    nuevodialogo = false
                    true
                } else {
                    false
                }
            }) {
            val inputStream = File(archivo).inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            Text(inputString)

        }
    }

    Row {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
                .wrapContentWidth(Alignment.Start),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            TextField(
                value = archivo,
                onValueChange = { archivo = it },
                label = { Text("Archivo") },
                placeholder = { Text("¿Que archivo quieres comprobar?") }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
                .wrapContentWidth(Alignment.Start),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = { nuevodialogo = true }) {
                Text(botonarchivo)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
                .wrapContentWidth(Alignment.Start),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = { nuevoarchivo = true }) {
                Text(botonnuevo)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

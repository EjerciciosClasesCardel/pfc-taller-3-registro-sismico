package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

/**
 * Casos de prueba propios para los puntos 1 y 2, distintos de los ejemplos del
 * enunciado y de RegistroSismicoTest. Usan su propio registro.
 */
@RunWith(classOf[JUnitRunner])
class RegistroSismicoTestExtras extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()

  // Un registro distinto del del enunciado. Fíjese en que:
  //  - Narino y Cauca comparten la magnitud 5.5.
  //  - La diferencia entre Cauca (4.0) y Huila (3.0) es exactamente 1.0.
  //  - La máxima de Cauca está al principio de su grupo y la de Narino al final.
  val otro = List(
    Sismo(Fecha(2024, 1), "Narino", 4.5, 12),
    Sismo(Fecha(2024, 2), "Cauca", 5.5, 20),
    Sismo(Fecha(2024, 3), "Narino", 5.5, 15),
    Sismo(Fecha(2024, 4), "Huila", 3.0, 8),
    Sismo(Fecha(2024, 5), "Cauca", 4.0, 22),
    Sismo(Fecha(2024, 6), "Narino", 6.5, 30))

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: el umbral es estricto aunque haya magnitudes iguales a él") {
    // Dos sismos miden exactamente 5.5; ninguno debe salir con umbral 5.5.
    assert(fuertes(otro, 5.5) == List("Narino"))
  }

  test("fuertes: conserva el orden del registro y los repetidos") {
    assert(fuertes(otro, 4.0) == List("Narino", "Cauca", "Narino", "Narino"))
  }

  test("fuertes: el registro vacío no tiene sismos fuertes") {
    assert(fuertes(vacio, 1.0) == List())
  }

  test("enRegion: las magnitudes de una región, en el orden del registro") {
    assert(enRegion(otro, "Narino") == List(4.5, 5.5, 6.5))
  }

  test("enRegion: una región con un solo sismo") {
    assert(enRegion(otro, "Huila") == List(3.0))
  }

  test("enRegion: distingue mayúsculas de minúsculas") {
    assert(enRegion(otro, "narino") == List())
  }

  test("paresCercanos: con diferencia 0 solo se emparejan magnitudes iguales") {
    assert(paresCercanos(otro, 0.0) == List(("Narino", "Cauca")))
  }

  test("paresCercanos: a lo sumo es inclusivo, una diferencia exacta de 1.0 cuenta") {
    // Cauca-Huila solo se acercan por 4.0 y 3.0, que difieren exactamente 1.0.
    assert(paresCercanos(otro, 1.0) == List(("Narino", "Cauca"), ("Cauca", "Huila")))
  }

  test("paresCercanos: cada pareja sale una sola vez aunque varios sismos las acerquen") {
    assert(paresCercanos(otro, 2.0) ==
      List(("Narino", "Cauca"), ("Narino", "Huila"), ("Cauca", "Huila")))
  }

  test("paresCercanos: el orden de cada pareja sigue la aparición en el registro") {
    val orden = List(
      Sismo(Fecha(2024, 1), "Zeta", 4.0, 10),
      Sismo(Fecha(2024, 1), "Alfa", 4.0, 10))
    // Zeta aparece antes que Alfa, así que la pareja es (Zeta, Alfa) y no al revés.
    assert(paresCercanos(orden, 0.5) == List(("Zeta", "Alfa")))
  }

  test("paresCercanos: el registro vacío y una sola región no dan parejas") {
    val unaRegion = List.fill(3)(Sismo(Fecha(2024, 1), "Arauca", 4.0, 10))
    assert(paresCercanos(vacio, 1.0) == List())
    assert(paresCercanos(unaRegion, 5.0) == List())
  }

  // Punto 2 -------------------------------------------------------------------

  test("porRegion: cada grupo conserva el orden del registro") {
    assert(porRegion(otro)("Narino").map(_.magnitud) == List(4.5, 5.5, 6.5))
  }

  test("porRegion: una región con un solo sismo") {
    assert(porRegion(otro)("Huila") == List(Sismo(Fecha(2024, 4), "Huila", 3.0, 8)))
  }

  test("porRegion: no se pierde ni se repite ningún sismo") {
    val todos = porRegion(otro).values.flatten.toList
    assert(todos.length == otro.length)
    assert(todos.toSet == otro.toSet)
  }

  test("porRegion: el registro vacío da el mapa vacío") {
    assert(porRegion(vacio) == Map())
  }

  test("conteoPorRegion: tres, dos y uno") {
    assert(conteoPorRegion(otro) == Map("Narino" -> 3, "Cauca" -> 2, "Huila" -> 1))
  }

  test("conteoPorRegion: una sola región repetida") {
    val unaRegion = List.fill(5)(Sismo(Fecha(2024, 1), "Arauca", 4.0, 10))
    assert(conteoPorRegion(unaRegion) == Map("Arauca" -> 5))
  }

  test("magnitudMaxima: la máxima puede estar al principio o al final del grupo") {
    assert(magnitudMaxima(otro) ==
      Map("Narino" -> 6.5, "Cauca" -> 5.5, "Huila" -> 3.0))
  }

  test("magnitudMaxima: con magnitudes repetidas devuelve esa magnitud") {
    val repetidas = List.fill(3)(Sismo(Fecha(2024, 1), "Arauca", 4.0, 10))
    assert(magnitudMaxima(repetidas) == Map("Arauca" -> 4.0))
  }

  test("magnitudMaxima: el registro vacío da el mapa vacío") {
    assert(magnitudMaxima(vacio) == Map())
  }

  test("el conteo y la máxima no dependen del orden en que se recorre el registro") {
    assert(conteoPorRegion(otro.reverse) == conteoPorRegion(otro))
    assert(magnitudMaxima(otro.reverse) == magnitudMaxima(otro))
  }

  test("porRegion: con el registro invertido cada región tiene los mismos sismos") {
    // Cambia el orden dentro de cada lista, pero no qué sismos tiene cada región.
    def comoConjuntos(m: Map[String, List[Sismo]]) = m.map { case (k, v) => (k, v.toSet) }
    assert(comoConjuntos(porRegion(otro.reverse)) == comoConjuntos(porRegion(otro)))
  }

  // ===========================================================================
  // Punto 3 — consultas seguras
  // ===========================================================================
  test("P3 propio: conteoSeguro responde el conteo de una región presente") {
    assert(conteoSeguro(otro)("Huila") == 1)
    assert(conteoSeguro(otro)("Narino") == 3)
  }
  test("P3 propio: conteoSeguro responde cero por una región ausente") {
    assert(conteoSeguro(otro)("Atlantico") == 0)
  }
  test("P3 propio: conteoSeguro sobre el registro vacío responde cero") {
    assert(conteoSeguro(vacio)("Nariño") == 0)
  }
  test("P3 propio: maximaDe devuelve la máxima de una región presente") {
    assert(maximaDe(otro, "Cauca") == Some(5.5))
    assert(maximaDe(otro, "Huila") == Some(3.0))
  }
  test("P3 propio: maximaDe devuelve None por una región ausente") {
    assert(maximaDe(otro, "Guajira") == None)
  }
  test("P3 propio: maximaDe sobre el registro vacío devuelve None") {
    assert(maximaDe(vacio, "Nariño") == None)
  }
  test("P3 propio: regionMasActiva devuelve la región con mayor conteo de sismos") {
    // En 'otro', Narino tiene 3 sismos, Cauca 2 y Huila 1; gana Narino por mayoría estricta.
    assert(regionMasActiva(otro) == Some("Narino"))
  }
  test("P3 propio: regionMasActiva desempata alfabéticamente en empate") {
    val empateDos = List(
      Sismo(Fecha(2024, 1), "Narino", 4.0, 10),
      Sismo(Fecha(2024, 2), "Cauca", 4.0, 20))
    // Ambos tienen 1 sismo: gana Cauca por orden alfabético ("Cauca" < "Narino")
    assert(regionMasActiva(empateDos) == Some("Cauca"))
  }
  test("P3 propio: regionMasActiva desempata entre tres empatadas") {
    val empateTres = List(
      Sismo(Fecha(2024, 1), "Zulia", 4.0, 10),
      Sismo(Fecha(2024, 1), "Arauca", 4.0, 10),
      Sismo(Fecha(2024, 1), "Amazonas", 4.0, 10))
    assert(regionMasActiva(empateTres) == Some("Amazonas"))
  }

  // ===========================================================================
  // Punto 4 — réplicas perezosas
  // ===========================================================================
  test("P4 propio: replicas de 6.0, las tres primeras") {
    assert(replicas(6.0).take(3).toList == List(6.0, 4.8, 3.84))
  }

  test("P4 propio: replicas de 10.0, las cinco primeras") {
    assert(replicas(10.0).take(5).toList == List(10.0, 8.0, 6.4, 5.12, 4.1))
  }

  test("P4 propio: replicas de 2.5 llega exacto a 1.6") {
    assert(replicas(2.5).take(3).toList == List(2.5, 2.0, 1.6))
  }

  test("P4 propio: replicas arranca en la magnitud redondeada") {
    assert(replicas(9.99).head == 9.99)
  }

  test("P4 propio: replicas materializa muchos elementos sin colgarse") {
    assert(replicas(7.7).take(150).toList.length == 150)
  }

  test("P4 propio: la cadena de redondeo de 4.0 baja de dos en dos decimales") {
    // Cada elemento es el 80 % del anterior, redondeado a dos decimales:
    // 4.0, 3.2, 2.56, 2.05, 1.64, 1.31, 1.05, 0.84, 0.67, 0.54.
    assert(replicasSensibles(4.0, 0.5) ==
      List(4.0, 3.2, 2.56, 2.05, 1.64, 1.31, 1.05, 0.84, 0.67, 0.54))
  }

  test("P4 propio: replicasSensibles corta en el primer elemento que no supera") {
    // 6.0, 4.8 y 3.84 superan 3.5; la siguiente, 3.07, ya no.
    assert(replicasSensibles(6.0, 3.5) == List(6.0, 4.8, 3.84))
  }

  test("P4 propio: replicasSensibles con mínimo apenas menor que la primera") {
    // Solo la primera (4.0) supera 3.99: la segunda es 3.2.
    assert(replicasSensibles(4.0, 3.99) == List(4.0))
  }

  test("P4 propio: replicasSensibles con mínimo igual a la magnitud da vacía") {
    // El mínimo es estricto: 4.0 no supera a 4.0.
    assert(replicasSensibles(4.0, 4.0) == List())
  }

  test("P4 propio: replicasSensibles cuando la primera no supera el mínimo") {
    assert(replicasSensibles(0.5, 1.0) == List())
  }


}


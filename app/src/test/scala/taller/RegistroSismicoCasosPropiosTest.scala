package taller

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegistroSismicoCasosPropiosTest extends AnyFunSuite {

  private val consultas = new RegistroSismico()
  import consultas._

  private def sismo(region: String, magnitud: Double): Sismo =
    Sismo(Fecha(2026, 1), region, magnitud, 20)

  test("fuertes: respeta el orden y conserva regiones repetidas") {
    val datos = List(sismo("Norte", 5.2), sismo("Sur", 4.9),
      sismo("Norte", 6.1))
    assert(fuertes(datos, 5.0) == List("Norte", "Norte"))
  }

  test("fuertes: no incluye una magnitud igual al umbral") {
    assert(fuertes(List(sismo("Centro", 4.5)), 4.5).isEmpty)
  }

  test("enRegion: mantiene el orden original de las magnitudes") {
    val datos = List(sismo("Este", 2.1), sismo("Oeste", 3.2),
      sismo("Este", 1.8))
    assert(enRegion(datos, "Este") == List(2.1, 1.8))
  }

  test("paresCercanos: no empareja sismos de una misma región") {
    val datos = List(sismo("A", 4.0), sismo("A", 4.1))
    assert(paresCercanos(datos, 1.0).isEmpty)
  }

  test("paresCercanos: cada pareja aparece una vez aunque haya varios sismos") {
    val datos = List(sismo("A", 4.0), sismo("B", 4.1),
      sismo("A", 4.2), sismo("B", 4.3))
    assert(paresCercanos(datos, 0.5) == List(("A", "B")))
  }

  test("porRegion: cada grupo conserva el orden de sus sismos") {
    val primero = sismo("A", 2.0)
    val otro = sismo("B", 3.0)
    val ultimo = sismo("A", 4.0)
    assert(porRegion(List(primero, otro, ultimo))("A") ==
      List(primero, ultimo))
  }

  test("porRegion: no crea entradas para regiones ausentes") {
    assert(!porRegion(List(sismo("A", 2.0))).contains("B"))
  }

  test("conteoPorRegion: cuenta grupos de distinto tamaño") {
    val datos = List(sismo("A", 2.0), sismo("A", 3.0), sismo("B", 4.0))
    assert(conteoPorRegion(datos) == Map("A" -> 2, "B" -> 1))
  }

  test("magnitudMaxima: obtiene la máxima aunque no sea el último sismo") {
    val datos = List(sismo("A", 7.0), sismo("A", 3.0), sismo("B", 4.0))
    assert(magnitudMaxima(datos) == Map("A" -> 7.0, "B" -> 4.0))
  }

  test("magnitudMaxima: una región con un solo sismo conserva su magnitud") {
    assert(magnitudMaxima(List(sismo("A", 2.7))) == Map("A" -> 2.7))
  }

  test("conteoPorRegion: no incluye regiones que no tienen sismos") {
    assert(!conteoPorRegion(List(sismo("A", 2.0))).contains("B"))
  }

  test("conteoSeguro: cualquier llave ausente responde cero") {
    assert(conteoSeguro(List(sismo("A", 1.0)))("Región desconocida") == 0)
  }

  test("conteoSeguro: consultar la llave por defecto no la agrega al mapa") {
    val conteos = conteoSeguro(List(sismo("A", 1.0)))
    assert(conteos("B") == 0)
    assert(!conteos.contains("B"))
  }

  test("maximaDe: devuelve el máximo cuando hay varios sismos") {
    val datos = List(sismo("A", 2.0), sismo("A", 5.0))
    assert(maximaDe(datos, "A") == Some(5.0))
  }

  test("maximaDe: el registro vacío no tiene máximo") {
    assert(maximaDe(List.empty[Sismo], "A").isEmpty)
  }

  test("regionMasActiva: desempata alfabéticamente y no por aparición") {
    val datos = List(sismo("Zeta", 2.0), sismo("Beta", 3.0))
    assert(regionMasActiva(datos) == Some("Beta"))
  }

  test("regionMasActiva: devuelve la única región cuando solo hay una") {
    assert(regionMasActiva(List(sismo("Isla", 3.0))) == Some("Isla"))
  }

  test("maximaDe: no confunde una región ausente con una magnitud cero") {
    assert(maximaDe(List(sismo("A", 0.0)), "B") == None)
  }

  test("replicas: el primer valor también se redondea") {
    assert(replicas(1.236).head == 1.24)
  }

  test("replicas: produce la cantidad pedida de valores") {
    assert(replicas(10.0).take(7).toList.length == 7)
  }

  test("replicas: cada nuevo valor es el 80 por ciento del anterior") {
    assert(replicas(10.0).take(4).toList == List(10.0, 8.0, 6.4, 5.12))
  }

  test("replicasSensibles: el valor igual al mínimo ya no se incluye") {
    assert(replicasSensibles(5.0, 4.0) == List(5.0))
  }

  test("replicasSensibles: con mínimo cero termina cuando redondea a cero") {
    assert(replicasSensibles(1.0, 0.0).last == 0.01)
  }

  test("informe: resume conteo, máximo y réplicas para cada región") {
    val datos = List(sismo("A", 5.0), sismo("A", 2.0), sismo("B", 3.5))
    assert(informe(datos, 3.0) ==
      Map("A" -> Resumen(2, 5.0, 3), "B" -> Resumen(1, 3.5, 1)))
  }

  test("informe: las réplicas se calculan desde el máximo de cada región") {
    val datos = List(sismo("A", 4.0), sismo("A", 6.0))
    assert(informe(datos, 3.0)("A") == Resumen(2, 6.0, 4))
  }

  test("informe: una región ausente no aparece en el resultado") {
    assert(!informe(List(sismo("A", 4.0)), 2.0).contains("B"))
  }

  test("informe: el mínimo excluye una réplica que lo iguala") {
    assert(informe(List(sismo("A", 5.0)), 4.0)("A").replicas == 1)
  }

  test("informe: una región con un solo sismo genera un resumen completo") {
    assert(informe(List(sismo("Sur", 2.5)), 2.0) ==
      Map("Sur" -> Resumen(1, 2.5, 1)))
  }
}

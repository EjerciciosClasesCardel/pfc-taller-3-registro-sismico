package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Datos.registro

/** Pruebas propias del punto 3 (consultas seguras). */
@RunWith(classOf[JUnitRunner])
class RegistroSismicoPunto3Test extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  // Registro propio: Cauca con tres sismos y Huila con uno.
  val propio: List[Sismo] = List(
    Sismo(Fecha(2026, 1), "Cauca", 3.5, 12),
    Sismo(Fecha(2026, 1), "Huila", 4.4, 80),
    Sismo(Fecha(2026, 2), "Cauca", 3.5, 15),
    Sismo(Fecha(2026, 3), "Cauca", 2.9, 9))

  test("conteoSeguro: registro propio, conocidas con su conteo y ausente con cero") {
    val c = conteoSeguro(propio)
    assert(c("Cauca") == 3)
    assert(c("Huila") == 1)
    assert(c("Narino") == 0)
  }

  test("maximaDe: con magnitudes repetidas devuelve ese valor máximo") {
    assert(maximaDe(propio, "Cauca") == Some(3.5))
  }

  test("maximaDe: distingue mayúsculas y minúsculas en el nombre de la región") {
    assert(maximaDe(registro, "valle") == None)
    assert(maximaDe(registro, "Valle") == Some(5.3))
  }

  test("regionMasActiva: gana la de más sismos aunque no sea la primera alfabéticamente") {
    val r = List(
      Sismo(Fecha(2026, 1), "Cauca", 3.0, 10),
      Sismo(Fecha(2026, 1), "Tolima", 4.0, 10),
      Sismo(Fecha(2026, 2), "Tolima", 3.2, 20),
      Sismo(Fecha(2026, 3), "Tolima", 5.0, 30),
      Sismo(Fecha(2026, 3), "Cauca", 2.5, 10))
    assert(regionMasActiva(r) == Some("Tolima"))
  }

  test("regionMasActiva: empate de tres regiones, gana la primera alfabéticamente") {
    val r = List(
      Sismo(Fecha(2026, 1), "Tolima", 3.0, 10),
      Sismo(Fecha(2026, 1), "Huila", 4.0, 10),
      Sismo(Fecha(2026, 2), "Cauca", 3.2, 20))
    assert(regionMasActiva(r) == Some("Cauca"))
  }
}
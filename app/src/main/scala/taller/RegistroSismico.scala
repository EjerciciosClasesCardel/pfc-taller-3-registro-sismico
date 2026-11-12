package taller

/**
 * Taller 3 — consultas sobre un registro sísmico con expresiones for, mapas y
 * secuencias perezosas.
 *
 * Complete cada función. Las firmas y los tipos no se modifican.
 */
class RegistroSismico {

  type Registro = List[Sismo]

  // Punto 1: consultas con expresiones for --------------------------------------

  /** Las regiones de los sismos que superan el umbral, en orden y con repetidos. */
  def fuertes(r: Registro, umbral: Double): List[String] = ???

  /** Las magnitudes registradas en una región. */
  def enRegion(r: Registro, region: String): List[Double] = ???

  /** Parejas de regiones distintas con algún sismo de magnitudes cercanas. */
  def paresCercanos(r: Registro, dif: Double): List[(String, String)] = ???

  // Punto 2: agrupar con mapas --------------------------------------------------

  def porRegion(r: Registro): Map[String, List[Sismo]] =
    r.groupBy(sismo => sismo.region)

  def conteoPorRegion(r: Registro): Map[String, Int] = {
    val grupos = porRegion(r)
    grupos.map { case (region, sismos) =>
      (region, sismos.length)
    }
  }

  def magnitudMaxima(r: Registro): Map[String, Double] = {
    val grupos = porRegion(r)
    grupos.map { case (region, sismos) =>
      val magnitudes = sismos.map(sismo => sismo.magnitud)
      (region, magnitudes.max)
    }
  }

  // Punto 3: consultas seguras --------------------------------------------------

  /** Un mapa que responde cero por las regiones que no están en el registro. */
  def conteoSeguro(r: Registro): Map[String, Int] =
    conteoPorRegion(r).withDefaultValue(0)

  /** La magnitud máxima de una región, o vacío si no hay registros suyos. */
  def maximaDe(r: Registro, region: String): Option[Double] =
    magnitudMaxima(r).get(region)

  /** La región con más sismos; en empate, la primera en orden alfabético. */
  def regionMasActiva(r: Registro): Option[String] = {
    val conteo = conteoPorRegion(r)
    if (conteo.isEmpty) None
    else {
      // Ordenadas alfabéticamente, maxBy se queda con la primera que tiene más.
      val ordenadas = conteo.keys.toList.sorted
      Some(ordenadas.maxBy(region => conteo(region)))
    }
  }

  // Punto 4: réplicas perezosas -------------------------------------------------

  def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

  /** La secuencia infinita que empieza en la magnitud y multiplica por 0.8. */
  def replicas(magnitud: Double): LazyList[Double] = {
    LazyList.iterate(magnitud)(_ * 0.8).map(x=> redondear(x))
  }

  /** Las réplicas mientras superen el mínimo, incluida la primera. */
  def replicasSensibles(magnitud: Double, minimo: Double): List[Double] = {
    replicas(magnitud).takeWhile(_ > minimo).toList
  }

  // Punto 5: informe combinado --------------------------------------------------

  /** Conteo, magnitud máxima y réplicas esperadas del sismo más fuerte, por región. */
  def informe(r: Registro, minimo: Double): Map[String, Resumen] = {
    porRegion(r).map{case (region,sismos) =>
      val maxima = sismos.map(_.magnitud).max
      region->Resumen (sismos.length,maxima,replicasSensibles(maxima,minimo).length)
    }
  }
}


# Informe de proceso — Taller 3: registro sísmico

Fundamentos de Programación Funcional y Concurrente
Escuela de Ingeniería de Sistemas y Computación, Universidad del Valle

Este informe explica *cómo se ejecuta* cada función de
RegistroSismico.scala paso a paso, y *en qué se traduce* cada expresión
for que aparece en el código. Se usa el modelo de sustitución: cada paso
reemplaza una expresión por su valor hasta llegar al resultado.

## Convenciones

- $r$ denota un registro ($\text{List}[\text{Sismo}]$).
- $s.\text{region}$, $s.\text{magnitud}$, $s.\text{profundidad}$ denotan los campos de un Sismo.
- $[\,e \mid x \leftarrow xs,\ P(x)\,]$ denota la lista de los $e$ tales que $x$ recorre $xs$ y se cumple $P(x)$.
- $\text{fst}(p)$ y $\text{snd}(p)$ denotan las componentes de una pareja.

## Traducción de las expresiones for

El compilador de Scala traduce cada for con yield a combinaciones de map, flatMap y withFilter (que es la versión perezosa de filter). La siguiente tabla recoge las traducciones que se usan en el taller:

| Expresión for en el código | Traducción del compilador |
|---|---|
| for (s <- r if p(s)) yield e(s) | r.withFilter(p).map(s => e(s)) |
| for (s <- r) yield e(s) | r.map(s => e(s)) |
| for { x <- xs; y <- ys } yield e(x,y) | xs.flatMap(x => ys.map(y => e(x,y))) |
| for { x <- xs; y <- ys; if g(x,y) } yield e(x,y) | xs.flatMap(x => ys.withFilter(y => g(x,y)).map(y => e(x,y))) |
| for { (a,i) <- ps; b <- bs; if h(a,b) } yield (a,b) | ps.flatMap { case (a,i) => bs.withFilter(b => h(a,b)).map(b => (a,b)) } |

La forma general es:

$$
\text{for } (x \leftarrow xs \text{ if } P(x)) \text{ yield } e(x)
\;\equiv\;
xs.\text{withFilter}(P).\text{map}(x \mapsto e(x))
$$

y para dos generadores:

$$
\text{for } \{x \leftarrow xs;\ y \leftarrow ys;\ \text{if } g(x,y)\}\ \text{yield } e(x,y)
\;\equiv\;
xs.\text{flatMap}\bigl(x \mapsto ys.\text{withFilter}(y \mapsto g(x,y)).\text{map}(y \mapsto e(x,y))\bigr)
$$

El for *sin* yield se traduce a foreach y no se usa en este taller, porque el curso prohíbe los ciclos iterativos.

## Punto 1 — consultas con expresiones for

### fuertes(r, umbral)

scala
def fuertes(r: Registro, umbral: Double): List[String] =
  for (s <- r if s.magnitud > umbral) yield s.region


*Traducción:* r.withFilter(_.magnitud > umbral).map(_.region).

*Ejecución paso a paso* con $r = \text{registro}$, $\text{umbral} = 5.0$:

1. withFilter deja pasar los sismos con $\text{magnitud} > 5.0$: $\text{Santander}(5.1)$, $\text{Choco}(6.0)$, $\text{Valle}(5.3)$.
2. map(_.region) proyecta cada uno a su región: $\text{List}(\text{"Santander"}, \text{"Choco"}, \text{"Valle"})$.

*Resultado:* $\text{List}(\text{"Santander"}, \text{"Choco"}, \text{"Valle"})$.

*Observación:* como map preserva el orden de la entrada, el resultado sale en el orden en que aparecen los sismos en $r$. El umbral es estricto: un sismo de magnitud exactamente $\text{umbral}$ *no* aparece.

### enRegion(r, region)

scala
def enRegion(r: Registro, region: String): List[Double] =
  for (s <- r if s.region == region) yield s.magnitud


*Traducción:* r.withFilter(_.region == region).map(_.magnitud).

*Ejecución paso a paso* con $\text{region} = \text{"Valle"}$:

1. withFilter deja los sismos de Valle: $4.2$, $3.8$, $5.3$.
2. map(_.magnitud) proyecta a sus magnitudes: $\text{List}(4.2, 3.8, 5.3)$.

*Resultado:* $\text{List}(4.2, 3.8, 5.3)$. Si la región no aparece, la lista es $\text{List}()$.

### paresCercanos(r, dif)

scala
def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
  val regiones = (for (s <- r) yield s.region).distinct

  def cercanas(a: String, b: String): Boolean = {
    val magsA = for (x <- r if x.region == a) yield x.magnitud
    val magsB = for (y <- r if y.region == b) yield y.magnitud
    (for {
      ma <- magsA
      mb <- magsB
      if math.abs(ma - mb) <= dif
    } yield (ma, mb)).nonEmpty
  }

  for {
    (a, i) <- regiones.zipWithIndex
    b       <- regiones.drop(i + 1)
    if cercanas(a, b)
  } yield (a, b)
}


*Traducciones internas:*

- $\text{regiones}$: r.map(_.region).distinct — regiones en orden de aparición, sin repetidos.
- $\text{magsA}$: r.withFilter(_.region == a).map(_.magnitud).
- $\text{magsB}$: r.withFilter(_.region == b).map(_.magnitud).
- El for anidado de cercanas:
  $$
  \text{magsA}.\text{flatMap}\bigl(ma \mapsto \text{magsB}.\text{withFilter}(mb \mapsto |ma - mb| \leq \text{dif}).\text{map}(mb \mapsto (ma, mb))\bigr)
  $$
  Luego .nonEmpty pregunta si el resultado no está vacío.
- El for externo:
  $$
  \text{regiones}.\text{zipWithIndex}.\text{flatMap}\bigl\{(a,i) \mapsto \text{regiones}.\text{drop}(i+1).\text{withFilter}(b \mapsto \text{cercanas}(a,b)).\text{map}(b \mapsto (a,b))\bigr\}
  $$

*Ejecución paso a paso* con $\text{registro}$, $\text{dif} = 1.0$:

1. $\text{regiones} = \text{List}(\text{"Valle"}, \text{"Santander"}, \text{"Choco"})$.
2. Para $(a, i) = (\text{"Valle"}, 0)$: $b$ recorre $\text{List}(\text{"Santander"}, \text{"Choco"})$.
   - $(\text{"Valle"}, \text{"Santander"})$: $|5.3 - 5.1| = 0.2 \leq 1.0$ $\rightarrow$ cercanas es $\text{true}$ $\rightarrow$ se emite $(\text{"Valle"}, \text{"Santander"})$.
   - $(\text{"Valle"}, \text{"Choco"})$: $|5.3 - 6.0| = 0.7 \leq 1.0$ $\rightarrow$ se emite $(\text{"Valle"}, \text{"Choco"})$.
3. Para $(a, i) = (\text{"Santander"}, 1)$: $b$ recorre $\text{List}(\text{"Choco"})$.
   - $(\text{"Santander"}, \text{"Choco"})$: $|5.1 - 6.0| = 0.9 \leq 1.0$ $\rightarrow$ se emite $(\text{"Santander"}, \text{"Choco"})$.
4. Para $(a, i) = (\text{"Choco"}, 2)$: $b$ recorre $\text{List}()$ $\rightarrow$ no se emite nada.

*Resultado:* $\text{List}((\text{"Valle"}, \text{"Santander"}), (\text{"Valle"}, \text{"Choco"}), (\text{"Santander"}, \text{"Choco"}))$.

*Por qué no hay duplicados:* $b$ recorre solo las regiones *después* de $a$ (por drop(i + 1)), así que $(\text{"Choco"}, \text{"Valle"})$ nunca se genera. Cada pareja no ordenada sale exactamente una vez, en orden de aparición de las regiones.

## Punto 2 — agrupar con mapas

### porRegion(r)

scala
def porRegion(r: Registro): Map[String, List[Sismo]] =
  r.groupBy(_.region)


*Ejecución paso a paso* con $\text{registro}$:

1. groupBy(_.region) recorre $r$ y agrupa los elementos por el valor de _.region, preservando el orden dentro de cada grupo.
2. Resultado:

scala
Map(
  "Valle"     -> List(Sismo(..., "Valle", 4.2, ...), Sismo(..., "Valle", 3.8, ...), Sismo(..., "Valle", 5.3, ...)),
  "Santander" -> List(Sismo(..., "Santander", 5.1, ...), Sismo(..., "Santander", 4.7, ...)),
  "Choco"     -> List(Sismo(..., "Choco", 6.0, ...), Sismo(..., "Choco", 3.1, ...)))


### conteoPorRegion(r)

scala
def conteoPorRegion(r: Registro): Map[String, Int] =
  r.groupBy(_.region).view.mapValues(_.length).toMap


*Ejecución paso a paso:*

1. groupBy(_.region) agrupa como en porRegion.
2. .view evita materializar un mapa intermedio.
3. .mapValues(_.length) reemplaza cada lista por su longitud.
4. .toMap fuerza la evaluación y devuelve un Map normal.

*Resultado* con $\text{registro}$:
$\text{Map}(\text{"Valle"} \mapsto 3, \text{"Santander"} \mapsto 2, \text{"Choco"} \mapsto 2)$.

### magnitudMaxima(r)

scala
def magnitudMaxima(r: Registro): Map[String, Double] =
  r.groupBy(_.region).view.mapValues(_.map(_.magnitud).max).toMap


*Ejecución paso a paso:*

1. groupBy(_.region) agrupa como antes.
2. .mapValues(_.map(_.magnitud).max): dentro de cada grupo, map extrae las magnitudes y .max toma la mayor.
3. .toMap fuerza la evaluación.

*Resultado* con $\text{registro}$:
$\text{Map}(\text{"Valle"} \mapsto 5.3, \text{"Santander"} \mapsto 5.1, \text{"Choco"} \mapsto 6.0)$.

*Observación:* las regiones ausentes no aparecen en el mapa (el enunciado lo pide explícitamente).

## Punto 3 — consultas seguras

### conteoSeguro(r)

scala
def conteoSeguro(r: Registro): Map[String, Int] =
  conteoPorRegion(r).withDefaultValue(0)


*Ejecución paso a paso:*

1. Se calcula conteoPorRegion(r) como antes.
2. .withDefaultValue(0) devuelve una *vista* del mismo mapa que, al consultar una llave ausente, responde $0$ en lugar de lanzar NoSuchElementException.

*Resultado* con $\text{registro}$:
$\text{conteoSeguro}(\text{registro})(\text{"Valle"}) = 3$, $\text{conteoSeguro}(\text{registro})(\text{"Amazonas"}) = 0$.

### maximaDe(r, region)

scala
def maximaDe(r: Registro, region: String): Option[Double] =
  r.filter(_.region == region).map(_.magnitud) match {
    case Nil => None
    case ms  => Some(ms.max)
  }


*Ejecución paso a paso* con $\text{region} = \text{"Choco"}$:

1. r.filter(_.region == "Choco") deja los dos sismos de Choco.
2. .map(_.magnitud) extrae $\text{List}(6.0, 3.1)$.
3. match:
   - Si la lista es $\text{Nil}$ $\rightarrow$ $\text{None}$.
   - Si no $\rightarrow$ $\text{Some}(ms.\text{max})$. Con $\text{List}(6.0, 3.1)$, $ms.\text{max} = 6.0$, así que $\text{Some}(6.0)$.

*Resultado:* $\text{Some}(6.0)$. Con $\text{region} = \text{"Amazonas"}$ $\rightarrow$ $\text{None}$.

### regionMasActiva(r)

scala
def regionMasActiva(r: Registro): Option[String] =
  if (r.isEmpty) None
  else {
    val conteos = conteoPorRegion(r)
    val max = conteos.values.max
    Some(conteos.filter(_._2 == max).keys.min)
  }


*Ejecución paso a paso* con $\text{registro}$:

1. $r.\text{isEmpty}$ es $\text{false}$.
2. $\text{conteos} = \text{Map}(\text{"Valle"} \mapsto 3, \text{"Santander"} \mapsto 2, \text{"Choco"} \mapsto 2)$.
3. $\text{max} = 3$.
4. $\text{conteos}.\text{filter}(\_ .\_2 == 3)$ deja $\text{Map}(\text{"Valle"} \mapsto 3)$.
5. $\text{keys}.\text{min} = \text{"Valle"}$.
6. $\text{Some}(\text{"Valle"})$.

*Resultado:* $\text{Some}(\text{"Valle"})$. Con $r = \text{List}()$ $\rightarrow$ $\text{None}$. Con empate, el keys.min elige la primera alfabéticamente (p. ej. $\text{Arauca}$ antes que $\text{Zulia}$).

## Punto 4 — réplicas perezosas

### redondear(x)

scala
def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0


*Ejecución paso a paso* con $x = 3.2$:

1. $3.2 \cdot 100.0 = 320.0$.
2. $\text{Math.round}(320.0) = 320$.
3. $320 / 100.0 = 3.2$.

Con $x = 2.56$: $2.56 \cdot 100 = 256.0$, $\text{round} = 256$, $256 / 100 = 2.56$.
Con $x = 2.048$: $204.8$, $\text{round} = 205$, $2.05$.

### replicas(magnitud)

scala
def replicas(magnitud: Double): LazyList[Double] =
  LazyList.iterate(redondear(magnitud))(x => redondear(x * 0.8))


*Ejecución paso a paso* con $\text{magnitud} = 5.0$:

1. $\text{LazyList.iterate}(\text{inicial})(f)$ produce $\text{inicial}, f(\text{inicial}), f(f(\text{inicial})), \ldots$ de forma *perezosa*: solo calcula un elemento cuando se pide.
2. $\text{inicial} = \text{redondear}(5.0) = 5.0$.
3. $f(x) = \text{redondear}(x \cdot 0.8)$:
   - $f(5.0) = \text{redondear}(4.0) = 4.0$.
   - $f(4.0) = \text{redondear}(3.2) = 3.2$.
   - $f(3.2) = \text{redondear}(2.56) = 2.56$.
   - $f(2.56) = \text{redondear}(2.048) = 2.05$.
   - $\ldots$

*Resultado:* la secuencia infinita $5.0, 4.0, 3.2, 2.56, 2.05, 1.64, \ldots$.

*Por qué no se cuelga:* LazyList no evalúa la cola hasta que se pide. replicas(5.0).take(4).toList solo evalúa cuatro elementos y devuelve $\text{List}(5.0, 4.0, 3.2, 2.56)$. Si replicas devolviera una List, el programa se colgaría intentando construir una lista infinita.

### replicasSensibles(magnitud, minimo)

scala
def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
  replicas(magnitud).takeWhile(_ > minimo).toList


*Ejecución paso a paso* con $\text{magnitud} = 5.0$, $\text{minimo} = 3.0$:

1. $\text{replicas}(5.0) = \text{LazyList}(5.0, 4.0, 3.2, 2.56, 2.05, \ldots)$.
2. takeWhile(_ > 3.0) pide elementos mientras superen $3.0$:
   - $5.0 > 3.0$ ✓
   - $4.0 > 3.0$ ✓
   - $3.2 > 3.0$ ✓
   - $2.56 > 3.0$ ✗ $\rightarrow$ se detiene.
3. .toList materializa solo los tres primeros: $\text{List}(5.0, 4.0, 3.2)$.

*Resultado:* $\text{List}(5.0, 4.0, 3.2)$. Con $\text{minimo} = 3.0$ y $\text{magnitud} = 3.0$: $3.0 > 3.0$ es falso $\rightarrow$ $\text{List}()$. El mínimo es estricto.

*Observación sobre pereza:* takeWhile es perezoso sobre LazyList, así que *no* materializa más elementos de los necesarios. Solo pide hasta el primer elemento que no cumple la condición.

## Punto 5 — informe combinado

### informe(r, minimo)

scala
def informe(r: Registro, minimo: Double): Map[String, Resumen] =
  r.groupBy(_.region).view.mapValues { sismos =>
    val max = sismos.map(_.magnitud).max
    Resumen(
      sismos   = sismos.length,
      maxima   = max,
      replicas = replicasSensibles(max, minimo).length
    )
  }.toMap


*Ejecución paso a paso* con $\text{registro}$, $\text{minimo} = 3.0$:

1. groupBy(_.region) agrupa por región (igual que en el Punto 2).
2. Para cada grupo $\text{sismos}$:
   - $\text{max} = \text{sismos}.\text{map}(\_.\text{magnitud}).\text{max}$: la mayor magnitud del grupo.
   - $\text{sismos}.\text{length}$: el conteo.
   - $\text{replicasSensibles}(\text{max}, \text{minimo}).\text{length}$: cuántas réplicas sensibles tiene el sismo más fuerte.
3. Se arma un Resumen por región y se devuelve el Map.

*Cálculo por región* con $\text{registro}$, $\text{minimo} = 3.0$:

| Región | $\text{sismos}$ | $\text{max}$ | $\text{replicasSensibles}(\text{max}, 3.0)$ | $\text{Resumen}$ |
|---|---|---|---|---|
| Valle | $[4.2, 3.8, 5.3]$ | $5.3$ | $[5.3, 4.24, 3.39]$ (3) | $\text{Resumen}(3, 5.3, 3)$ |
| Santander | $[5.1, 4.7]$ | $5.1$ | $[5.1, 4.08, 3.26]$ (3) | $\text{Resumen}(2, 5.1, 3)$ |
| Choco | $[6.0, 3.1]$ | $6.0$ | $[6.0, 4.8, 3.84, 3.07]$ (4) | $\text{Resumen}(2, 6.0, 4)$ |

*Resultado:*

scala
Map(
  "Valle"     -> Resumen(3, 5.3, 3),
  "Santander" -> Resumen(2, 5.1, 3),
  "Choco"     -> Resumen(2, 6.0, 4))


## Diagrama de flujo del procesamiento

mermaid
flowchart TD
    A[registro: List Sismo] --> B[groupBy .region]
    B --> C[Map Region a List Sismo]
    C --> D[para cada grupo]
    D --> E[conteo = sismos.length]
    D --> F[max = sismos.map .magnitud .max]
    F --> G[replicasSensibles max minimo]
    G --> H[replicas = longitud]
    E --> I[Resumen conteo max replicas]
    H --> I
    I --> J[Map Region a Resumen]


## Tabla de traducciones for

| Función | for usado | Traducción |
|---|---|---|
| fuertes | for (s <- r if s.magnitud > umbral) yield s.region | r.withFilter(_.magnitud > umbral).map(_.region) |
| enRegion | for (s <- r if s.region == region) yield s.magnitud | r.withFilter(_.region == region).map(_.magnitud) |
| paresCercanos (regiones) | for (s <- r) yield s.region | r.map(_.region) |
| paresCercanos (magsA/magsB) | for (x <- r if x.region == a) yield x.magnitud | r.withFilter(_.region == a).map(_.magnitud) |
| paresCercanos (anidado) | for { ma <- magsA; mb <- magsB; if ... } yield (ma, mb) | magsA.flatMap(ma => magsB.withFilter(...).map(mb => (ma, mb))) |
| paresCercanos (externo) | for { (a,i) <- regiones.zipWithIndex; b <- regiones.drop(i+1); if cercanas(a,b) } yield (a,b) | regiones.zipWithIndex.flatMap { case (a,i) => regiones.drop(i+1).withFilter(b => cercanas(a,b)).map(b => (a,b)) } |

Todos los for del taller tienen yield y producen una colección nueva. Ninguno es un for sin yield (que se traduciría a foreach y sería un ciclo iterativo, prohibido en el curso).
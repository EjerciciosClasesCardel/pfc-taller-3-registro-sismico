# Informe de proceso

Este informe explica cómo se ejecuta cada función de los Puntos 1 y 2 sobre el
registro de ejemplo del enunciado, y en qué operaciones se traduce cada
expresión `for`. Las traducciones se obtuvieron del compilador
(`scalac -Xprint:parser`) y las trazas salen de ejecutar el código, no de
cálculos a mano.

## Registro de ejemplo

```Scala
val registro = List(
  Sismo(Fecha(2025, 3), "Valle", 4.2, 30),
  Sismo(Fecha(2025, 3), "Santander", 5.1, 150),
  Sismo(Fecha(2025, 4), "Valle", 3.8, 25),
  Sismo(Fecha(2025, 4), "Choco", 6.0, 40),
  Sismo(Fecha(2025, 5), "Santander", 4.7, 160),
  Sismo(Fecha(2025, 5), "Valle", 5.3, 35),
  Sismo(Fecha(2025, 6), "Choco", 3.1, 20)
)
```

En las trazas, el número de la primera columna es la posición del sismo en el
registro (del 1 al 7).

## Cómo se traduce una expresión `for`

El compilador reescribe cada `for` con `yield` en llamadas a `map`, `flatMap` y
`withFilter`:

| Forma del `for` | Se traduce a |
|-----------------|--------------|
| `for { x <- e } yield f` | `e.map(x => f)` |
| `for { x <- e; if p } yield f` | `e.withFilter(x => p).map(x => f)` |
| `for { x <- e1; y <- e2 } yield f` | `e1.flatMap(x => e2.map(y => f))` |

La regla general es: el **último** generador se traduce a `map`, cada generador
**anterior** a `flatMap`, y cada guarda `if` a un `withFilter`. `withFilter`
no construye una lista intermedia: deja pasar cada elemento solo si cumple la
condición, y el `map` que sigue transforma únicamente los que pasaron.

Las expresiones `for` que usa el código, con la traducción exacta que imprime
el compilador:

| Función | Expresión `for` | Traducción |
|---------|-----------------|------------|
| `fuertes` | `for { s <- r; if s.magnitud > umbral } yield s.region` | `r.withFilter(s => s.magnitud > umbral).map(s => s.region)` |
| `enRegion` | `for { s <- r; if s.region == region } yield s.magnitud` | `r.withFilter(s => s.region == region).map(s => s.magnitud)` |
| `paresCercanos` (regiones) | `for (s <- r) yield s.region` | `r.map(s => s.region)` |
| `paresCercanos` (`cercanas`) | `for { s1 <- r; s2 <- r; if ...; if ... } yield (s1, s2)` | `r.flatMap(s1 => r.withFilter(s2 => s1.region == a && s2.region == b).withFilter(s2 => Math.abs(s1.magnitud - s2.magnitud) <= dif).map(s2 => (s1, s2)))` |
| `paresCercanos` (resultado) | `for { a <- regiones; b <- regiones; if ...; if ... } yield (a, b)` | `regiones.flatMap(a => regiones.withFilter(b => regiones.indexOf(a) < regiones.indexOf(b)).withFilter(b => cercanas(a, b)).map(b => (a, b)))` |

## Punto 1: consultas con expresiones `for`

### `fuertes`

```Scala
def fuertes(r: Registro, umbral: Double): List[String] =
  for {
    s <- r
    if s.magnitud > umbral
  } yield s.region
```

Se traduce a `r.withFilter(s => s.magnitud > umbral).map(s => s.region)`. Cada
sismo del registro, en orden, pasa por la guarda `magnitud > umbral`. Si la
cumple, `map` entrega su región; si no, se descarta. Como no hay ninguna
operación que quite repetidos, una región con varios sismos fuertes aparece
varias veces.

Ejecución de `fuertes(registro, 5.0)`:

| # | Región | Magnitud | Guarda | Aporta al resultado |
|:-:|--------|:--------:|--------|---------------------|
| 1 | Valle     | 4.2 | `4.2 > 5.0` es false | se descarta |
| 2 | Santander | 5.1 | `5.1 > 5.0` es true  | `"Santander"` |
| 3 | Valle     | 3.8 | `3.8 > 5.0` es false | se descarta |
| 4 | Choco     | 6.0 | `6.0 > 5.0` es true  | `"Choco"` |
| 5 | Santander | 4.7 | `4.7 > 5.0` es false | se descarta |
| 6 | Valle     | 5.3 | `5.3 > 5.0` es true  | `"Valle"` |
| 7 | Choco     | 3.1 | `3.1 > 5.0` es false | se descarta |

Resultado: `List("Santander", "Choco", "Valle")`.

```mermaid
flowchart LR
    R["registro: 7 sismos"] --> F["withFilter: magnitud > 5.0, pasan 3"]
    F --> M["map: region"]
    M --> S["List Santander, Choco, Valle"]
```

### `enRegion`

```Scala
def enRegion(r: Registro, region: String): List[Double] =
  for {
    s <- r
    if s.region == region
  } yield s.magnitud
```

Se traduce a `r.withFilter(s => s.region == region).map(s => s.magnitud)`. Es la
misma estructura que `fuertes`, con otra guarda y otra proyección: se queda con
los sismos de la región pedida y entrega su magnitud, en el orden del registro.
Si la región no está, ningún sismo pasa la guarda y el resultado es la lista
vacía.

Ejecución de `enRegion(registro, "Valle")`:

| # | Región | Guarda | Aporta al resultado |
|:-:|--------|--------|---------------------|
| 1 | Valle     | `"Valle" == "Valle"` es true      | `4.2` |
| 2 | Santander | `"Santander" == "Valle"` es false | se descarta |
| 3 | Valle     | `"Valle" == "Valle"` es true      | `3.8` |
| 4 | Choco     | `"Choco" == "Valle"` es false     | se descarta |
| 5 | Santander | `"Santander" == "Valle"` es false | se descarta |
| 6 | Valle     | `"Valle" == "Valle"` es true      | `5.3` |
| 7 | Choco     | `"Choco" == "Valle"` es false     | se descarta |

Resultado: `List(4.2, 3.8, 5.3)`.

### `paresCercanos`

```Scala
def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
  val regiones = (for (s <- r) yield s.region).distinct

  def cercanas(a: String, b: String): Boolean =
    (for {
      s1 <- r
      s2 <- r
      if s1.region == a && s2.region == b
      if Math.abs(s1.magnitud - s2.magnitud) <= dif
    } yield (s1, s2)).nonEmpty

  for {
    a <- regiones
    b <- regiones
    if regiones.indexOf(a) < regiones.indexOf(b)
    if cercanas(a, b)
  } yield (a, b)
}
```

La función trabaja en tres pasos.

**Paso 1: las regiones en orden de aparición.** `(for (s <- r) yield s.region)`
se traduce a `r.map(s => s.region)`, que da una región por cada sismo, y
`.distinct` conserva solo la primera aparición de cada una. Para el registro de
ejemplo: `List("Valle", "Santander", "Choco")`.

**Paso 2: `cercanas(a, b)`.** Su `for` se traduce a un `flatMap` sobre `s1`, y
dentro de él un `withFilter` (la guarda de las regiones), un segundo
`withFilter` (la guarda de la diferencia) y un `map` que arma la pareja de
sismos. Cada combinación `(s1, s2)` de sismos del registro se examina en ese
orden. La función devuelve `true` si queda al menos una pareja (`.nonEmpty`),
es decir, si existe algún sismo de `a` y algún sismo de `b` cuyas magnitudes
difieren a lo sumo `dif`.

**Paso 3: las parejas de regiones.** El `for` externo recorre todas las
combinaciones `(a, b)` de regiones. La primera guarda,
`regiones.indexOf(a) < regiones.indexOf(b)`, solo deja pasar las combinaciones
en que `a` aparece antes que `b`. Esto elimina las parejas con una región
repetida (`(a, a)`) y garantiza que cada pareja salga en un solo orden:
`("Valle", "Choco")` pasa, pero `("Choco", "Valle")` no. La segunda guarda llama
a `cercanas`. El resultado conserva el orden de los generadores: primero todas
las parejas de la primera región, luego las de la segunda, y así.

Ejecución de `paresCercanos(registro, 1.0)`. Con `regiones` igual a
`List("Valle", "Santander", "Choco")`, el `for` externo examina las nueve
combinaciones:

| `a` | `b` | `indexOf(a) < indexOf(b)` | `cercanas(a, b)` | Resultado |
|-----|-----|---------------------------|------------------|-----------|
| Valle     | Valle     | `0 < 0` es false | no se evalúa | se descarta |
| Valle     | Santander | `0 < 1` es true  | true (5 parejas de sismos cumplen) | `("Valle", "Santander")` |
| Valle     | Choco     | `0 < 2` es true  | true (2 parejas de sismos cumplen) | `("Valle", "Choco")` |
| Santander | Valle     | `1 < 0` es false | no se evalúa | se descarta |
| Santander | Santander | `1 < 1` es false | no se evalúa | se descarta |
| Santander | Choco     | `1 < 2` es true  | true (1 pareja de sismos cumple) | `("Santander", "Choco")` |
| Choco     | Valle     | `2 < 0` es false | no se evalúa | se descarta |
| Choco     | Santander | `2 < 1` es false | no se evalúa | se descarta |
| Choco     | Choco     | `2 < 2` es false | no se evalúa | se descarta |

`cercanas` solo se evalúa cuando la primera guarda se cumple, porque los dos
`withFilter` se aplican en cadena a cada elemento.

Por dentro, `cercanas("Valle", "Santander")` examina las seis combinaciones de
un sismo del Valle con uno de Santander:

| Sismo de Valle | Sismo de Santander | Diferencia | `<= 1.0` |
|:--------------:|:------------------:|:----------:|:--------:|
| 4.2 | 5.1 | 0.9 | true  |
| 4.2 | 4.7 | 0.5 | true  |
| 3.8 | 5.1 | 1.3 | false |
| 3.8 | 4.7 | 0.9 | true  |
| 5.3 | 5.1 | 0.2 | true  |
| 5.3 | 4.7 | 0.6 | true  |

Cinco parejas cumplen, así que la lista no queda vacía y `cercanas` devuelve
`true`. Las diferencias son aproximadas: con `Double`, la resta `5.1 - 4.2` da
`0.8999999999999995` y no exactamente `0.9`.

Resultado: `List(("Valle", "Santander"), ("Valle", "Choco"), ("Santander", "Choco"))`.

```mermaid
flowchart TB
    R["registro"] --> RG["regiones: Valle, Santander, Choco"]
    RG --> P["for externo: 9 combinaciones a, b"]
    P --> G1["guarda 1: indexOf a menor que indexOf b"]
    G1 --> G2["guarda 2: cercanas a, b"]
    G2 --> OUT["3 parejas: Valle-Santander, Valle-Choco, Santander-Choco"]
    R --> C["cercanas: recorre todas las parejas de sismos s1, s2"]
    C --> G2
```

## Punto 2: agrupar con mapas

Este punto no usa `for`: las tres funciones se apoyan en `groupBy`, que arma
el mapa, y en `map` sobre ese mapa. No hay entonces expresiones `for` que
traducir; el recorrido lo hace la biblioteca.

### `porRegion`

```Scala
def porRegion(r: Registro): Map[String, List[Sismo]] =
  r.groupBy(_.region)
```

`groupBy(_.region)` calcula la región de cada sismo y arma un mapa cuyas llaves
son las regiones que **aparecen** en el registro y cuyos valores son las listas
de sus sismos, en el mismo orden en que aparecen en el registro. Una región que
no está en el registro no es una llave del mapa. Con el registro vacío no hay
llaves y el resultado es `Map()`.

Ejecución de `porRegion(registro)`, con las magnitudes de cada lista:

| Llave | Sismos del grupo (magnitudes, en orden) |
|-------|------------------------------------------|
| Valle     | `4.2, 3.8, 5.3` (sismos 1, 3 y 6) |
| Santander | `5.1, 4.7` (sismos 2 y 5) |
| Choco     | `6.0, 3.1` (sismos 4 y 7) |

El orden en que el mapa entrega sus llaves no está garantizado, y no importa:
dos mapas son iguales si tienen las mismas llaves con los mismos valores.

### `conteoPorRegion`

```Scala
def conteoPorRegion(r: Registro): Map[String, Int] =
  porRegion(r).map { case (region, sismos) => (region, sismos.length) }
```

Primero se calcula `porRegion(r)`. Después `map` recorre cada entrada del
mapa, `(región, lista de sismos)`, y la reemplaza por `(región, longitud de la
lista)`. Las llaves se conservan y solo cambia el valor.

Ejecución de `conteoPorRegion(registro)`:

| Entrada de `porRegion` | Entrada resultante |
|------------------------|--------------------|
| `Valle -> 3 sismos`     | `Valle -> 3` |
| `Santander -> 2 sismos` | `Santander -> 2` |
| `Choco -> 2 sismos`     | `Choco -> 2` |

Resultado: `Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2)`.

### `magnitudMaxima`

```Scala
def magnitudMaxima(r: Registro): Map[String, Double] =
  porRegion(r).map { case (region, sismos) => (region, sismos.map(_.magnitud).max) }
```

Es igual que `conteoPorRegion`, pero de cada lista se queda con su mayor
magnitud: `sismos.map(_.magnitud)` extrae las magnitudes y `.max` toma la
mayor. El `max` nunca se aplica a una lista vacía, porque `groupBy` solo crea
una llave cuando hay al menos un sismo de esa región.

Ejecución de `magnitudMaxima(registro)`:

| Entrada de `porRegion` | Magnitudes | `max` | Entrada resultante |
|------------------------|------------|:-----:|--------------------|
| `Valle`     | `4.2, 3.8, 5.3` | 5.3 | `Valle -> 5.3` |
| `Santander` | `5.1, 4.7`      | 5.1 | `Santander -> 5.1` |
| `Choco`     | `6.0, 3.1`      | 6.0 | `Choco -> 6.0` |

Resultado: `Map("Valle" -> 5.3, "Santander" -> 5.1, "Choco" -> 6.0)`.

```mermaid
flowchart LR
    R["registro: 7 sismos"] --> G["groupBy region"]
    G --> V["Valle: 4.2, 3.8, 5.3"]
    G --> S["Santander: 5.1, 4.7"]
    G --> C["Choco: 6.0, 3.1"]
    V --> N["conteoPorRegion: 3, 2, 2"]
    S --> N
    C --> N
    V --> X["magnitudMaxima: 5.3, 5.1, 6.0"]
    S --> X
    C --> X
```

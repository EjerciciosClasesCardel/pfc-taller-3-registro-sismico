# Informe de proceso

## Organización de las consultas

El trabajo quedó separado según los cinco puntos del taller. Las consultas
del primero recorren el registro con comprensiones `for`; para los siguientes
se aprovechan las operaciones de colecciones de Scala: `groupBy` para formar
los grupos, `map` para resumirlos y `get` para consultar sin lanzar una
excepción. La secuencia de réplicas se construye con `LazyList.iterate`, y el
informe combina los resúmenes por región.

## Traducción de las expresiones `for`

En `fuertes`, la comprensión:

```scala
for (sismo <- r if sismo.magnitud > umbral) yield sismo.region
```

se traduce a:

```scala
r.withFilter(sismo => sismo.magnitud > umbral)
  .map(sismo => sismo.region)
```

El generador produce un elemento por sismo, la condición se implementa con
`withFilter` y el `yield` con `map`. Por eso se conserva el orden y también
las regiones repetidas.

En `enRegion` se aplica el mismo esquema:

```scala
for (sismo <- r if sismo.region == region) yield sismo.magnitud
```

equivale a:

```scala
r.withFilter(sismo => sismo.region == region)
  .map(sismo => sismo.magnitud)
```

Para `paresCercanos` primero se obtiene, mediante otra comprensión, la lista
de regiones en orden de aparición. `distinct` deja una sola copia de cada
región. Los índices permiten combinar cada región solo con las que aparecen
después:

```scala
for {
  (region1, indice) <- regiones.zipWithIndex
  region2 <- regiones.drop(indice + 1)
  cercanos = (for {
    sismo1 <- r if sismo1.region == region1
    sismo2 <- r if sismo2.region == region2
    if math.abs(sismo1.magnitud - sismo2.magnitud) <= dif
  } yield true)
  if cercanos.nonEmpty
} yield (region1, region2)
```

Los dos generadores externos se traducen en `flatMap`: por cada par de
regiones candidatas se explora el resto de la lista, y por cada candidata se
genera el resultado con `map`. El `if` externo y los `if` de los generadores
se implementan con `withFilter`. La definición `cercanos = ...` calcula una
lista auxiliar para esa pareja; la comprensión interna se traduce, a su vez,
en filtros sobre los dos recorridos de `r` y un `map` que produce `true`.
Solo se devuelve la pareja si esa lista no está vacía. Como `region2` siempre
viene después de `region1`, no se repiten parejas al invertirlas; además, las
regiones distintas ya están aseguradas por la lista inicial.

En general, una comprensión con un generador y `yield` se traduce a `map`;
cada generador adicional se representa con `flatMap`, y las condiciones con
`withFilter`. Así, las tres consultas expresan qué elementos se quieren
obtener sin construir manualmente cadenas de `filter` y `map`.

## Mapas y valores opcionales

`porRegion` usa `groupBy` y conserva en cada lista los sismos de esa región.
Desde ese mapa se obtienen los conteos y las magnitudes máximas. Para la
consulta segura, `withDefaultValue(0)` da cero al aplicar el mapa a una llave
ausente sin añadir esa llave a sus entradas. `maximaDe` utiliza `get`, que
representa la ausencia con `None`. La región más activa se obtiene ordenando
por cantidad descendente y, en caso de empate, por nombre ascendente; si no
hay grupos, `headOption` devuelve `None`.

## Secuencia perezosa

`LazyList.iterate` construye la secuencia de magnitudes, aplicando el 80 % en
cada paso; `map` redondea a dos decimales los valores que se entregan. No
calcula de antemano toda la secuencia: cada nodo se evalúa cuando se necesita.
Por eso
`take(4).toList` solicita y materializa solo cuatro valores, aunque la
secuencia que los puede producir sea infinita. No se reserva memoria para
todos los elementos futuros. `replicasSensibles` consume únicamente el
prefijo que cumple la condición estricta `magnitud > minimo`. Mantener el
cálculo sin redondear entre pasos también evita que una magnitud pequeña se
quede indefinidamente en `0.01` por efecto del redondeo.

## Informe combinado

`informe` recorre los grupos de `porRegion`. Para cada uno calcula su
cantidad, encuentra la magnitud máxima y cuenta el prefijo sensible de
réplicas que parte de esa magnitud. El resultado es un mapa con una entrada
por cada región presente; el registro vacío produce un mapa vacío.

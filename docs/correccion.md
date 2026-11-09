# Informe de corrección — Taller 3: registro sísmico

Fundamentos de Programación Funcional y Concurrente
Escuela de Ingeniería de Sistemas y Computación, Universidad del Valle

Este informe argumenta la **corrección** de las funciones de
`RegistroSismico.scala` con respecto a su especificación. Para las funciones
recursivas se usa **inducción estructural**; para las iterativas, el método
del **invariante de iteración**; y para todas, se argumenta por qué el
resultado **no depende del orden de recorrido**.

## Convenciones

- $r$ denota un registro ($\text{List}[\text{Sismo}]$).
- $L = \text{List}(a_1, a_2, \ldots, a_n)$ denota una lista de $n$ sismos.
- $|L|$ denota la longitud de $L$.
- $L[i]$ denota el $i$-ésimo elemento de $L$ (1-indexado).
- $\text{regiones}(L)$ denota la lista de regiones de $L$ en orden de aparición, sin repetidos.
- $\text{multiset}(L)$ denota el multiconjunto de elementos de $L$.
- $P_f$ denota el programa que calcula la función $f$.
- $\text{Inv}(s)$ denota el invariante de iteración sobre el estado $s$.
- $\text{transformar}(s)$ denota la función de transición de estados.
- $\text{esFinal}(s)$ denota el predicado que reconoce un estado final.
- $\text{respuesta}(s)$ denota el valor que devuelve el programa en el estado final $s$.

## 1. Corrección de programas recursivos

### 1.1. Esquema general

Sea $f : A \to B$ una función y $A$ un conjunto definido recursivamente. Sea
$P_f$ un programa recursivo que calcula $f$. Para argumentar que $P_f$ es
correcto con respecto a su especificación se demuestra:

$$
\forall a \in A : P_f(a) = f(a)
$$

Por **inducción estructural** sobre $A$, se demuestra:

- **Caso base:** para cada valor básico $a$ de $A$, $P_f(a) = f(a)$.
- **Caso inductivo:** para cada $a \in A$ construido recursivamente a partir
  de $a' \in A$, se cumple

$$
P_f(a') = f(a') \;\rightarrow\; P_f(a) = f(a)
$$

donde $P_f(a') = f(a')$ es la **hipótesis de inducción (HI)**.

### 1.2. Ejemplo: `maximaDe`

La función `maximaDe(r, region)` devuelve la magnitud máxima de los sismos de
`region` en $r$, o `None` si no hay registros suyos. Su especificación es:

$$
f(r, \text{region}) =
\begin{cases}
\text{None} & \text{si } \{s \in r \mid s.\text{region} = \text{region}\} = \emptyset \\
\text{Some}\left(\max\{s.\text{magnitud} \mid s \in r,\ s.\text{region} = \text{region}\}\right) & \text{en otro caso}
\end{cases}
$$

El programa es:

```scala
def maximaDe(r: Registro, region: String): Option[Double] =
  r.filter(_.region == region).map(_.magnitud) match {
    case Nil => None
    case ms  => Some(ms.max)
  }
```

**Demostración por inducción estructural sobre $r$.**

**Caso base:** $r = \text{Nil}$.

$$
P_f(\text{Nil}, \text{region}) \;\rightarrow\; \text{Nil}.\text{filter}(\ldots).\text{map}(\ldots) \;\rightarrow\; \text{Nil}
$$

Como $\text{Nil}$ hace `match` con el patrón `Nil`, devuelve `None`. Por otro
lado, $f(\text{Nil}, \text{region}) = \text{None}$ porque el conjunto de
sismos de esa región es vacío. Por lo tanto, $P_f(\text{Nil}, \text{region}) = f(\text{Nil}, \text{region})$.

**Caso inductivo:** $r = s :: r'$, con $s$ un sismo y $r'$ un registro.
Supongamos como **HI** que
$P_f(r', \text{region}) = f(r', \text{region})$.

Hay dos casos:

- **Caso A:** $s.\text{region} \neq \text{region}$. Entonces
  $s :: r'$ filtrado por `region` es igual a $r'$ filtrado por `region`.
  Por lo tanto $P_f(s :: r', \text{region}) = P_f(r', \text{region})$, y por
  HI esto es $f(r', \text{region})$. Como $s$ no pertenece a la región,
  $f(s :: r', \text{region}) = f(r', \text{region})$. Así,
  $P_f(s :: r', \text{region}) = f(s :: r', \text{region})$.

- **Caso B:** $s.\text{region} = \text{region}$. Entonces el filtro deja
  $s :: (r' \text{ filtrado})$, y `.map(_.magnitud)` produce
  $s.\text{magnitud} :: (r' \text{ filtrado y proyectado})$. El `match` cae
  en el segundo caso y devuelve
  $\text{Some}\left(\max\left(s.\text{magnitud} :: (r' \text{ proyectado})\right)\right)$.
  Por HI, el máximo de $r'$ proyectado es $f(r', \text{region})$ (si $r'$
  filtrado no es vacío) o el máximo del singleton (si $r'$ filtrado es
  vacío). En ambos casos, el máximo de $s.\text{magnitud} :: (\ldots)$ es
  $\max(s.\text{magnitud}, f(r', \text{region}))$, que coincide con
  $f(s :: r', \text{region})$ por definición de $f$.

Por lo tanto, $P_f(r, \text{region}) = f(r, \text{region})$ para todo $r$. $\blacksquare$

### 1.3. Ejemplo: `replicasSensibles`

La función `replicasSensibles(magnitud, minimo)` devuelve la lista de
términos de la secuencia de réplicas que superan estrictamente `minimo`,
incluida la primera. Su especificación es:

$$
f(m, \text{min}) = [\,r_0, r_1, \ldots, r_{k-1}\,]
\quad\text{donde}\quad
r_0 = m,\ r_{i+1} = \text{redondear}(0.8 \cdot r_i),\ r_i > \text{min}
$$

y $k$ es el menor índice tal que $r_k \leq \text{min}$ (o $\infty$ si nunca
ocurre).

El programa es:

```scala
def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
  replicas(magnitud).takeWhile(_ > minimo).toList
```

donde $\text{replicas}(m) = \text{LazyList.iterate}(\text{redondear}(m))(x \mapsto \text{redondear}(x \cdot 0.8))$.

**Demostración por inducción sobre el número de términos que superan el mínimo.**

**Caso base:** $m \leq \text{min}$. Entonces $r_0 = m \leq \text{min}$, así
que `takeWhile(_ > minimo)` no toma ningún elemento y devuelve $\text{List}()$.
Por otro lado, $f(m, \text{min}) = []$ porque el primer término no supera el
mínimo. Por lo tanto, $P_f(m, \text{min}) = f(m, \text{min})$.

**Caso inductivo:** $m > \text{min}$. Entonces `takeWhile` toma $r_0 = m$ y
pasa a evaluar el resto de la secuencia con la misma condición. El resto de
la secuencia es exactamente $\text{replicas}(\text{redondear}(0.8 \cdot m))$,
que es la secuencia de réplicas de $m' = \text{redondear}(0.8 \cdot m)$.
Por **HI**, el resultado de aplicar `replicasSensibles(m', min)` es
$f(m', \text{min})$. Por lo tanto,

$$
P_f(m, \text{min}) = m :: f(m', \text{min}) = f(m, \text{min})
$$

donde la última igualdad se sigue de la definición recursiva de $f$. $\blacksquare$

**Observación sobre terminación:** si $m > 0$ y $\text{min} > 0$, la secuencia
$r_i$ decrece geométricamente ($r_{i+1} \approx 0.8 \cdot r_i$), así que
eventualmente $r_k \leq \text{min}$ y la recursión termina. Si $\text{min} \leq 0$,
la secuencia nunca deja de superar el mínimo y `takeWhile` **no termina**.
Esto es coherente con la especificación: $f(m, \text{min})$ sería una lista
infinita, y `takeWhile` sobre una `LazyList` infinita no termina. Los tests
acotan este caso con `take(k)`.

## 2. Corrección de programas iterativos

### 2.1. Esquema general

Un programa iterativo tiene la forma:

```scala
def Pf(a: A): B = {
  def Pf_iter(s: Estado): B =
    if (esFinal(s)) respuesta(s) else Pf_iter(transformar(s))
  Pf_iter(s0)
}
```

Para argumentar su corrección se define:

- Un **estado** $s$ de la iteración.
- El **estado inicial** $s_0$.
- El predicado **`esFinal`** que reconoce estados finales.
- El **invariante** $\text{Inv}(s)$, un predicado que se cumple en todo estado alcanzable.
- La **transformación** $\text{transformar}(s)$ que produce el siguiente estado.

Se demuestran cuatro puntos:

1. **Inicialización:** $\text{Inv}(s_0)$.
2. **Preservación:** si $s_i \neq s_f$ y $\text{Inv}(s_i)$, entonces $\text{Inv}(\text{transformar}(s_i))$.
3. **Finalización correcta:** $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) = f(a)$.
4. **Terminación:** la iteración alcanza un estado final $s_f$ en un número finito de pasos.

De estos cuatro puntos se concluye:

$$
P_f(a) = \text{iter}(s_0) = \text{respuesta}(s_f) = f(a)
$$

### 2.2. Ejemplo: `paresCercanos`

La función `paresCercanos(r, dif)` devuelve las parejas de regiones distintas
que tienen algún par de sismos con magnitud a lo sumo `dif` de diferencia.
Cada pareja no ordenada sale una sola vez, en orden de aparición de las
regiones.

El programa es:

```scala
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
```

Este programa se puede ver como un proceso iterativo sobre un **estado** que
representa el par de índices $(i, j)$ en la lista `regiones`:

- **Estado:** $s = (i, j)$ con $0 \leq i < |\text{regiones}|$ y $i < j \leq |\text{regiones}|$.
- **Estado inicial:** $s_0 = (0, 1)$.
- **`esFinal`:** $i \geq |\text{regiones}| - 1$.
- **`transformar`:** si $j < |\text{regiones}| - 1$, entonces $(i, j+1)$; si $j = |\text{regiones}| - 1$ e $i < |\text{regiones}| - 1$, entonces $(i+1, i+2)$.
- **Invariante:** $\text{Inv}(i, j) \equiv$ todos los pares $(a, b)$ con $a$ antes que $b$ en `regiones` y con índice menor que $(i, j)$ (en orden lexicográfico) ya fueron emitidos si cumplen `cercanas`.
- **`respuesta`:** la lista de pares emitidos hasta $s_f$.

**Demostración de los cuatro puntos.**

**1. Inicialización:** $\text{Inv}(s_0) = \text{Inv}(0, 1)$. No hay pares con
índice menor que $(0, 1)$ en orden lexicográfico (el primero es $(0, 1)$),
así que la lista de pares emitidos es vacía y el invariante se cumple
trivialmente.

**2. Preservación:** supongamos $\text{Inv}(i, j)$ y $s_i \neq s_f$. Al
transformar a $(i, j+1)$ (o a $(i+1, i+2)$), el único par nuevo que se
considera es $(\text{regiones}[i], \text{regiones}[j])$. Si
$\text{cercanas}(\text{regiones}[i], \text{regiones}[j])$ es verdadero, se
añade a la lista de emitidos; si es falso, no se añade. En ambos casos, todos
los pares con índice menor que el nuevo estado ya fueron considerados, así
que $\text{Inv}(i, j+1)$ (o $\text{Inv}(i+1, i+2)$) se cumple.

**3. Finalización correcta:** en $s_f$, $i \geq |\text{regiones}| - 1$, así
que se han considerado todos los pares $(a, b)$ con $a$ antes que $b$ en
`regiones`. Por lo tanto, la lista de pares emitidos es exactamente
$f(\text{regiones}, \text{dif})$, que es lo que devuelve `respuesta`.

**4. Terminación:** en cada paso, el índice $j$ aumenta hasta
$|\text{regiones}| - 1$, momento en que $i$ aumenta en 1 y $j$ se reinicia a
$i + 2$. Como $i$ aumenta estrictamente y está acotado por
$|\text{regiones}| - 1$, la iteración termina en a lo sumo
$\binom{|\text{regiones}|}{2}$ pasos.

Por lo tanto, $P_f(r, \text{dif}) = f(r, \text{dif})$. $\blacksquare$

## 3. Independencia del orden de recorrido

Esta sección argumenta por qué el resultado de cada función **no depende del
orden en que se recorran los elementos de $r$**.

### 3.1. Funciones basadas en `filter` y `map`

Las funciones `fuertes`, `enRegion` y `enRegion` (vía `for` con `yield`)
aplican `filter` y `map` sobre $r$. Su especificación es:

$$
f_{\text{fuertes}}(r, u) = [\,s.\text{region} \mid s \in r,\ s.\text{magnitud} > u\,]
$$

$$
f_{\text{enRegion}}(r, \text{reg}) = [\,s.\text{magnitud} \mid s \in r,\ s.\text{region} = \text{reg}\,]
$$

Estas funciones son **extensiones** de predicados y proyecciones sobre $r$:
el resultado es la concatenación, en el orden de $r$, de los elementos que
satisfacen el predicado, proyectados por la función. Formalmente, si
$r = s_1 :: s_2 :: \ldots :: s_n$:

$$
f_{\text{fuertes}}(r, u) = \bigoplus_{k=1}^{n} \begin{cases} [s_k.\text{region}] & \text{si } s_k.\text{magnitud} > u \\ [] & \text{en otro caso} \end{cases}
$$

donde $\bigoplus$ denota concatenación. La concatenación es **asociativa**,
así que el resultado no depende del orden en que se procesen los elementos,
solo del orden en que aparecen en $r$ (que es parte de la especificación).

**Nota sobre el orden de salida:** aunque el conjunto de elementos que
satisfacen el predicado es independiente del orden, la **lista resultante**
sí preserva el orden de $r$ (porque `map` y `filter` preservan el orden). Eso
es lo que pide el enunciado ("en el orden en que aparecen"). La
**independencia del orden de recorrido** se refiere a que el resultado **como
colección** (multiconjunto o lista ordenada por $r$) no cambia si se recorre
$r$ de otra manera, porque el orden de $r$ es fijo y la operación es
conmutativa a nivel de multiconjuntos.

### 3.2. `paresCercanos`: simetría de la relación

La relación $\text{cercanas}(a, b)$ es **simétrica**:

$$
\text{cercanas}(a, b) \;\equiv\; \exists\, x \in r_a, y \in r_b : |x.\text{magnitud} - y.\text{magnitud}| \leq \text{dif}
$$

$$
\equiv\; \exists\, y \in r_b, x \in r_a : |y.\text{magnitud} - x.\text{magnitud}| \leq \text{dif}
\;\equiv\; \text{cercanas}(b, a)
$$

donde $r_a = \{s \in r \mid s.\text{region} = a\}$. La simetría se debe a que
$|x - y| = |y - x|$.

Como la relación es simétrica, el conjunto de parejas no ordenadas que
satisfacen `cercanas` es el mismo sin importar en qué orden se examinen los
pares. El programa examina cada par no ordenado **exactamente una vez** (con
$a$ antes que $b$ en `regiones`), y emite la pareja en el orden de aparición
de las regiones. Por lo tanto:

$$
\{\, \{a, b\} \mid a \neq b,\ \text{cercanas}(a, b)\,\} \;=\; \{\, \{a, b\} \mid a \neq b,\ \text{cercanas}(b, a)\,\}
$$

y el resultado es independiente del orden de recorrido.

**Por qué no hay duplicados:** la restricción $i < j$ sobre la lista
`regiones` elige un **representante canónico** de cada par no ordenado. Como
la relación es simétrica, si $(a, b)$ cumple, $(b, a)$ también, pero solo se
emite uno de los dos (el que tiene $a$ antes que $b$ en `regiones`). Esto
garantiza que cada pareja sale exactamente una vez.

### 3.3. `porRegion`, `conteoPorRegion`, `magnitudMaxima`: conmutatividad de `groupBy` y del máximo

La función `groupBy` agrupa los elementos de $r$ por región. Formalmente:

$$
\text{porRegion}(r) = \{\, \text{reg} \mapsto \{s \in r \mid s.\text{region} = \text{reg}\} \mid \text{reg} \in \text{regiones}(r)\,\}
$$

El **conjunto** de elementos de cada grupo es independiente del orden de
recorrido (es la intersección de $r$ con la región). El **orden interno** de
cada grupo sí depende de $r$, pero el enunciado no lo especifica como
requisito. Por lo tanto, `porRegion` es independiente del orden de recorrido
a nivel de multiconjuntos.

Para `conteoPorRegion`:

$$
\text{conteoPorRegion}(r)(\text{reg}) = |\{s \in r \mid s.\text{region} = \text{reg}\}|
$$

La cardinalidad de un conjunto es independiente del orden. Por lo tanto,
`conteoPorRegion` es independiente del orden de recorrido.

Para `magnitudMaxima`:

$$
\text{magnitudMaxima}(r)(\text{reg}) = \max\{s.\text{magnitud} \mid s \in r,\ s.\text{region} = \text{reg}\}
$$

El máximo de un conjunto es **conmutativo** y **asociativo**: el resultado no
depende del orden en que se examinen los elementos. Por lo tanto,
`magnitudMaxima` es independiente del orden de recorrido.

### 3.4. `conteoSeguro` y `maximaDe`: herencia de la independencia

`conteoSeguro` es `conteoPorRegion` con `withDefaultValue(0)`. Como
`conteoPorRegion` es independiente del orden, `conteoSeguro` también lo es.

`maximaDe` es `filter` + `map` + `max`, que es la composición de operaciones
independientes del orden (el máximo es conmutativo). Por lo tanto,
`maximaDe` es independiente del orden.

### 3.5. `regionMasActiva`: máximo con desempate canónico

`regionMasActiva` calcula el máximo de los conteos y, en caso de empate,
elige la región **alfabéticamente menor**:

$$
\text{regionMasActiva}(r) = \text{Some}\left(\min\{\text{reg} \mid \text{conteoPorRegion}(r)(\text{reg}) = M\}\right)
$$

donde $M = \max_{\text{reg}} \text{conteoPorRegion}(r)(\text{reg})$. Tanto el
máximo $M$ como el mínimo alfabético de las regiones que empatan son
**conmutativos** (no dependen del orden de recorrido). El desempate
alfabético hace que la elección sea **única**, independientemente del orden
en que se recorran las regiones. Por lo tanto, `regionMasActiva` es
independiente del orden.

### 3.6. `replicas` y `replicasSensibles`: determinismo de la recurrencia

La secuencia de réplicas está definida por la recurrencia:

$$
r_0 = \text{redondear}(m), \qquad r_{i+1} = \text{redondear}(0.8 \cdot r_i)
$$

Esta recurrencia es **determinista**: el valor de $r_i$ depende solo de $m$ y
de $i$, no del orden en que se pidan los elementos. La pereza de `LazyList`
garantiza que el valor de $r_i$ es el mismo sin importar cuándo se solicite
(antes o después de otros elementos). Por lo tanto, `replicas` es
independiente del orden de evaluación.

`replicasSensibles` aplica `takeWhile` sobre `replicas`. Como `takeWhile` es
perezoso y la secuencia es determinista, el resultado es independiente del
orden en que se pidan los elementos. Por lo tanto, `replicasSensibles` es
independiente del orden.

### 3.7. `informe`: composición de funciones independientes

`informe` combina `groupBy`, `max`, `length` y `replicasSensibles`, todas
independientes del orden. Por lo tanto, `informe` es independiente del orden
de recorrido:

$$
\text{informe}(r, \text{min})(\text{reg}) = \text{Resumen}\left(|\text{grupo}|,\ \max(\text{grupo}),\ |\text{replicasSensibles}(\max(\text{grupo}), \text{min})|\right)
$$

donde $\text{grupo} = \{s \in r \mid s.\text{region} = \text{reg}\}$. Cada
componente es independiente del orden, así que el `Resumen` completo también
lo es.

## 4. Conclusión

Se ha argumentado la corrección de las doce funciones de
`RegistroSismico.scala`:

- **Funciones recursivas** (`maximaDe`, `replicasSensibles`): por inducción estructural.
- **Funciones iterativas** (`paresCercanos`): por invariante de iteración.
- **Funciones basadas en `groupBy`, `filter`, `map` y `max`**: por composición de operaciones conmutativas y asociativas.
- **Independencia del orden de recorrido**: para las doce funciones, argumentando que las operaciones subyacentes (`filter`, `map`, `groupBy`, `max`, `length`, `recurrencia determinista`) son conmutativas, asociativas o deterministas, y que el desempate de `regionMasActiva` es canónico.

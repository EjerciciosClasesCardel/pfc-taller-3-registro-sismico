# Ejemplo de informe de corrección

Fundamentos de Programación Funcional y Concurrente.
Documento realizado por el docente Juan Francisco Díaz.

## 1. Argumentar la corrección de programas recursivos

Sea $f : A \to B$ una función, y $A$ un conjunto definido recursivamente
(recordar la definición de Matemáticas Discretas I), como por ejemplo los
naturales o las listas.

Sea $P_f$ un programa recursivo (lineal o en árbol) desarrollado en Scala (o en
cualquier lenguaje de programación) hecho para calcular $f$:

```scala
def Pf(a: A): B = { // Pf recibe a de tipo A, y devuelve f(a) de tipo B
  ...
}
```

¿Cómo argumentar que $P_f(a)$ siempre devuelve $f(a)$ como respuesta? Es decir,
¿cómo argumentar que $P_f$ es correcto con respecto a su especificación?

La respuesta es sencilla: demostrando el siguiente teorema.

```math
\forall a \in A : P_f(a) == f(a)
```

Cuando uno tiene que demostrar que algo se cumple para todos los elementos de
un conjunto definido recursivamente, es natural usar inducción estructural. En
términos prácticos, esto significa demostrar que:

- Para cada valor básico $a$ de $A$, se tiene que $P_f(a) == f(a)$.
- Para cada valor $a \in A$ construido recursivamente a partir de otro(s)
  valor(es) $a' \in A$, se tiene que
  $P_f(a') == f(a') \rightarrow P_f(a) == f(a)$. (Esta es la hipótesis de
  inducción).

### Ejemplo: factorial recursivo

Sea $f : \mathbb{N} \to \mathbb{N}$ la función que calcula el factorial de un
número natural, es decir, $f(n) = n!$. Y sea $P_f$ el siguiente programa en
Scala:

```scala
def Pf(n: Int): Int = { // Pf recibe n de tipo Int, y devuelve n! de tipo Int
  if (n == 0) 1 else n * Pf(n - 1)
}
```

Vamos a demostrar que $\forall n \in \mathbb{N} : P_f(n) == n!$

**Caso base:** $n = 0$

```math
P_f(0) \rightarrow \text{if } (0 == 0)\ 1 \text{ else } 0 \ast P_f(-1) \rightarrow 1
```

Por otro lado, $f(0) = 0! = 1$. Entonces $P_f(0) == f(0)$.

**Caso de inducción:** $n = k + 1$, $k \geq 0$. Hay que demostrar:
$P_f(k) == f(k) \rightarrow P_f(k + 1) == f(k + 1)$

```math
P_f(k+1) \rightarrow \text{if } (k+1 == 0)\ 1 \text{ else } (k+1) \ast P_f(k) \rightarrow (k+1) \ast P_f(k)
```

Usando la hipótesis de inducción (HI):

```math
\rightarrow (k+1) \ast k! = (k+1)!
```

Por lo tanto, $P_f(k + 1) == f(k + 1)$.

Concluimos por inducción que $\forall n \in \mathbb{N} : P_f(n) == n!$

### Ejemplo: el máximo de una lista

Sea $f : \text{List}[\mathbb{N}] \to \mathbb{N}$ la función que calcula el
máximo de una lista de enteros positivos, no vacía. Y sea $P_f$ el siguiente
programa en Scala:

```scala
def maxLin(l: List[Int]): Int = {
  if (l.tail.isEmpty) l.head
  else math.max(maxLin(l.tail), l.head)
}
```

Demostraremos que:

```math
\forall n \in \mathbb{N} \setminus \{0\} : P_f(\text{List}(a_1, a_2, \ldots, a_n)) == f(\text{List}(a_1, a_2, \ldots, a_n))
```

**Caso base:** $n = 1$

```math
P_f(\text{List}(a_1)) \rightarrow \text{if } \text{List}(a_1).\text{tail.isEmpty then } \text{List}(a_1).\text{head else } \ldots \rightarrow \text{List}(a_1).\text{head} \rightarrow a_1
```

Por otro lado, $f(\text{List}(a_1)) = a_1$. Entonces
$P_f(\text{List}(a_1)) == f(\text{List}(a_1))$.

**Caso de inducción:** $n = k + 1$, $k \geq 1$. Se debe demostrar:

```math
P_f(\text{List}(b_1, b_2, \ldots, b_k)) == f(\text{List}(b_1, b_2, \ldots, b_k)) \rightarrow P_f(\text{List}(a_1, a_2, \ldots, a_{k+1})) == f(\text{List}(a_1, a_2, \ldots, a_{k+1}))
```

Empecemos por calcular qué devuelve $P_f$ usando el modelo de sustitución:

```math
P_f(L) \rightarrow \text{if } L.\text{tail.isEmpty then } L.\text{head else math.max}(P_f(L.\text{tail}), L.\text{head})
```

```math
\rightarrow \text{math.max}(P_f(\text{List}(a_2, \ldots, a_{k+1})), a_1)
```

Sea $b = P_f(\text{List}(a_2, \ldots, a_{k+1}))$; por la hipótesis de
inducción, $b = f(\text{List}(a_2, \ldots, a_{k+1}))$. Hay dos posibilidades:

- Si $\text{math.max}(b, a_1) = b$, entonces $b \geq a_1$ y
 $b == f(\text{List}(a_1, a_2, \ldots, a_{k+1}))$.
- Si $\text{math.max}(b, a_1) = a_1$, entonces $a_1 \geq b$ y
 $a_1 == f(\text{List}(a_1, a_2, \ldots, a_{k+1}))$.

Por lo tanto, $P_f(L) == f(L)$.

Concluimos por inducción que:

```math
\forall n \in \mathbb{N} \setminus \{0\} : P_f(\text{List}(a_1, a_2, \ldots, a_n)) == f(\text{List}(a_1, a_2, \ldots, a_n))
```

## 2. Argumentar la corrección de programas iterativos

Para argumentar la corrección de programas iterativos, se debe formalizar cómo
es la iteración. Esto implica definir:

- Cómo se representa un estado de la iteración, $s$.
- Cuál es el estado inicial, $s_0$.
- Cuál es el estado final (o cómo se reconoce que un estado es final): $s_f$.
- Qué condición (o predicado) cumple todo estado: $\text{Inv}(s)$ (invariante
  de la iteración).
- El mecanismo para pasar de un estado al siguiente: $\text{transformar}(s)$.
  Si $s_i$ es el estado $i$, entonces $\text{transformar}(s_i) = s_{i+1}$.

Un programa iterativo tiene la siguiente forma:

```scala
def Pf(a: A): B = { // Pf recibe a de tipo A, y devuelve f(a) de tipo B
  def Pf_iter(s: Estado): B =
    if (esFinal(s)) respuesta(s) else Pf_iter(transformar(s))
  Pf_iter(s0)
}
```

Demostración de corrección:

- $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.
- Si $(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$:
  el nuevo estado cumple la condición invariante si el estado anterior la
  cumplía.
- De lo anterior se concluye $\text{Inv}(s_f)$, es decir, el estado final
  cumple la condición invariante. Luego,
  $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == f(a)$.
- Finalmente, demostrar que siempre se llega al estado final $s_f$. Esto
  implica que
  $P_f(a) == \text{iter}(s_0) == \text{respuesta}(s_f) == f(a)$.

### Ejemplo: factorial iterativo

Considere el siguiente programa iterativo en Scala para calcular la función
factorial:

```scala
def Pf(n: Int): Int = { // Pf recibe n de tipo Int, y devuelve n! de tipo Int
  def Pf_iter(i: Int, n: Int, ac: Int): Int =
    if (i > n) ac else Pf_iter(i + 1, n, i * ac)
  Pf_iter(1, n, 1)
}
```

Este programa implementa el siguiente proceso iterativo:

- Un estado $s = (i, n, ac)$.
- El estado inicial es $s_0 = (1, n, 1)$.
- $(i, n, ac)$ es final si $i > n$, o lo que es lo mismo, si $i = n + 1$.
- La invariante de ciclo es
  $\text{Inv}(i, n, ac) \equiv i \leq n + 1 \land ac = (i-1)!$.
  La invariante de ciclo es una relación que SIEMPRE se cumple en el ciclo.
- $\text{transformar}((i, n, ac)) = (i+1, n, i \ast ac)$.

Ahora, demostramos los puntos mencionados:

**1.** $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.

```math
s_0 = (1, n, 1) \implies 1 \leq n + 1 \land 1 = 0!
```

**2.** La invariante se mantiene con la transformación de estados,
$(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$:

1. Primer cambio, $i = i + 1$, lo que implica $ac = ((i+1) - 1)! = i!$.
2. Segundo cambio, $ac = i \ast ac$, entonces $ac = (i - 1)! \ast i = i!$.
3. Como se puede ver en ambos cambios indicados en la transformación, la
   invariante se mantiene.

**3.** $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == f(a)$

```math
(n + 1 \leq n + 1) \land ac = ((n+1)-1)! \rightarrow ac == n!
```

**4.** En cada paso, la componente $i$ del estado incrementa, acercándose a $n+1$.
Después de $n$ iteraciones, se alcanza $n+1$.

Esto implica que $P_f(n) == \text{iter}(1, n, 1) == n!$

### Ejemplo: el máximo de una lista

Se desea calcular el máximo de una lista de enteros positivos, no vacía. Sea
$f : \text{List}[\mathbb{N}] \to \mathbb{N}$ la función que calcula ese valor.
Y sea $P_f$ el siguiente programa en Scala:

```scala
def maxIt(l: List[Int]): Int = {
  def maxAux(max: Int, l: List[Int]): Int = {
    if (l.isEmpty) max
    else maxAux(math.max(max, l.head), l.tail)
  }
  maxAux(l.head, l.tail)
}
```

Este programa implementa el siguiente proceso iterativo:

- Un estado $s = (max, l)$ donde $l = \text{List}(a_i, a_{i+1}, \ldots, a_k)$
  es una cola de $L$.
- El estado inicial es
  $s_0 = (L.\text{head}, L.\text{tail}) = (a_1, \text{List}(a_2, \ldots, a_k))$.
- $s = (max, l)$ es final si $l$ es vacía.
- $\text{Inv}(max, l) \equiv l = \text{List}(a_i, a_{i+1}, \ldots, a_k) \land max = f(\text{List}(a_1, a_2, \ldots, a_{i-1}))$.
- $\text{transformar}((max, l)) = (nmax, l.\text{tail})$ donde $nmax = max$ si
  $max \geq l.\text{head}$, y $nmax = l.\text{head}$ si no.

Demostración de los puntos:

**1.** $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.

```math
s_0 = (a_1, \text{List}(a_2, \ldots, a_k)) \implies a_1 = f(\text{List}(a_1))
```

**2.** $(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$

```math
\neg\, l.\text{isEmpty} \land l = \text{List}(a_i, a_{i+1}, \ldots, a_k) \land max = f(\text{List}(a_1, a_2, \ldots, a_{i-1}))
```

```math
\rightarrow l.\text{tail} = \text{List}(a_{i+1}, \ldots, a_k) \land nmax = f(\text{List}(a_1, \ldots, a_i))
```

**3.** $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == f(a)$

```math
\text{Inv}((max, \text{List}())) \rightarrow max = f(\text{List}(a_1, \ldots, a_k))
```

**4.** En cada paso, la lista $l$ se reduce, acercándose a ser vacía. Después de
$k$ iteraciones, $l = \text{List}()$.

Esto implica que $P_f(L) == \text{maxAux}(L.\text{head}, L.\text{tail}) == f(L)$


## Notación común

Un sismo es $s = (f, \rho, \mu, p)$ con región $\rho(s)$ y magnitud $\mu(s)$.
Un registro es una lista $r = \text{List}(s_1, \ldots, s_n)$, y se define
recursivamente: el registro vacío $\text{List}()$ es el caso base, y
$s :: r'$ es el registro que añade el sismo $s$ al frente de $r'$.

Para una región $g$, se define el sub-registro de esa región:

```math
S_g(r) = \text{List}(s \in r \mid \rho(s) = g)
```

y el conjunto de regiones presentes $R(r) = \{\rho(s) \mid s \in r\}$.

---

## Punto 4: réplicas perezosas

Este punto no recorre un registro: construye una secuencia infinita. El
argumento de inducción es distinto al de los demás puntos: cada elemento se
calcula cuando se pide, así que las propiedades se enuncian sobre **la
posición $k$** y sobre **los primeros $n$ elementos**, no sobre toda la
secuencia a la vez.

### 4.1 Especificación

Sea $m$ la magnitud inicial y $\mu_{min}$ el mínimo. Se supone $m > 0$ y
$\mu_{min} \geq 0$ (las magnitudes del registro son positivas; ver 4.6 para el
caso en que $\mu_{min}$ es negativo).

El redondeo a dos decimales que usa el programa, `Math.round(x * 100.0) / 100.0`,
es:

```math
\text{red}(x) = \frac{\lfloor 100\,x + \tfrac{1}{2} \rfloor}{100}
```

La secuencia ideal de magnitudes sin redondear es $a_0 = m$ y
$a_{k+1} = 0.8 \cdot a_k$, es decir $a_k = m \cdot 0.8^k$. La secuencia de
réplicas es:

```math
r_k = \text{red}(a_k) \qquad k \in \mathbb{N}
```

Lo que el enunciado pide es:

- $\text{replicas}(m) = (r_0, r_1, r_2, \ldots)$.
- $\text{replicasSensibles}(m, \mu_{min}) = \text{List}(r_0, \ldots, r_{n-1})$,
  donde $n = \min\{\,k \in \mathbb{N} \mid r_k \leq \mu_{min}\,\}$. Es decir, las
  réplicas mientras **superen** el mínimo.

### 4.2 Elemento $k$ de `replicas`

**Lema 1 (`iterate`).** Sea $\text{xs} = \text{LazyList.iterate}(s)(f)$. Para
todo $k \in \mathbb{N}$, $\text{xs}(k) = f^k(s)$, donde $f^0(s) = s$ y
$f^{k+1}(s) = f(f^k(s))$.

_Demostración._ Por la definición de `iterate`,
$\text{iterate}(s)(f) = s \;\#::\; \text{iterate}(f(s))(f)$. Inducción sobre
$k$ (para todo valor de $s$):

**Caso base:** $k = 0$. $\text{xs}(0) = \text{head}(\text{xs}) = s = f^0(s)$.

**Caso de inducción:** $k + 1$. Hipótesis de inducción (HI): para todo $s'$,
$\text{iterate}(s')(f)(k) = f^k(s')$.

```math
\text{xs}(k+1) = \text{tail}(\text{xs})(k) = \text{iterate}(f(s))(f)(k) \overset{\text{HI}}{=} f^k(f(s)) = f^{k+1}(s)
```

Por lo tanto $\text{xs}(k) = f^k(s)$ para todo $k$. $\blacksquare$

**Lema 2 (`map`).** Para toda secuencia $\text{xs}$ y función $g$,
$\text{xs.map}(g)(k) = g(\text{xs}(k))$. Es la definición de `map`: aplica $g$
a cada elemento conservando la posición.

**Teorema A.** Para todo $k \in \mathbb{N}$, $\text{replicas}(m)(k) = r_k$.

_Demostración._ Con $f(x) = 0.8\,x$, el Lema 1 da
$\text{iterate}(m)(f)(k) = f^k(m) = m \cdot 0.8^k = a_k$. Por el Lema 2,
$\text{replicas}(m)(k) = \text{red}(a_k) = r_k$. $\blacksquare$

Comprobación con $m = 5.0$: $r_0 = 5.0$, $r_1 = 4.0$, $r_2 = 3.2$ y
$r_3 = \text{red}(2.56) = 2.56$, que es lo que da `replicas(5.0).take(4)`.

### 4.3 La secuencia es decreciente

**Lema 3.** Si $m > 0$, entonces $r_{k+1} \leq r_k$ para todo $k$.

_Demostración._ Como $a_k = m \cdot 0.8^k > 0$, se tiene
$a_{k+1} = 0.8\,a_k < a_k$. La función $\text{red}$ es monótona no decreciente:
si $x \leq y$ entonces $100x + \tfrac12 \leq 100y + \tfrac12$, y como
$\lfloor \cdot \rfloor$ es monótona, $\text{red}(x) \leq \text{red}(y)$. Entonces:

```math
r_{k+1} = \text{red}(a_{k+1}) \leq \text{red}(a_k) = r_k \qquad \blacksquare
```

(Es no estricta: dos réplicas seguidas pueden redondear al mismo valor cuando
son muy pequeñas.)

### 4.4 `replicasSensibles` devuelve exactamente las réplicas sensibles

**Lema 4 (terminación).** Si $m > 0$, existe $k$ con $r_k \leq \mu_{min}$.

_Demostración._ $a_k < 0.005 \iff 0.8^k < \dfrac{0.005}{m} \iff k > \dfrac{\ln(200\,m)}{\ln(5/4)}$.
Para todo $k$ mayor que esa cota, $a_k < 0.005$, luego
$100\,a_k + \tfrac12 < 1$ y $r_k = \text{red}(a_k) = 0 \leq \mu_{min}$, porque
$\mu_{min} \geq 0$. $\blacksquare$

Por el Lema 4 el conjunto $\{k \mid r_k \leq \mu_{min}\}$ no es vacío, así que
$n = \min\{k \mid r_k \leq \mu_{min}\}$ existe.

**Teorema B.** $\text{replicasSensibles}(m, \mu_{min}) = \text{List}(r_0, \ldots, r_{n-1})$,
y además $\{\,k \mid r_k > \mu_{min}\,\} = \{0, \ldots, n-1\}$: no hay ninguna réplica
sensible fuera de ese prefijo.

_Demostración._

1. Por el Teorema A, el elemento $k$ de `replicas(m)` es $r_k$.
2. `takeWhile(p)` entrega el prefijo más largo de elementos que cumplen $p$:
   avanza mientras $p$ sea verdadero y se detiene en el primer elemento que
   falla. Aquí $p(x) \equiv x > \mu_{min}$.
3. Por la definición de $n$ como mínimo, para todo $k < n$ se cumple
   $r_k > \mu_{min}$, y $r_n \leq \mu_{min}$. Entonces `takeWhile` deja pasar
   exactamente $r_0, \ldots, r_{n-1}$ y se detiene en $r_n$.
4. `toList` convierte ese prefijo finito en `List`, así que el resultado es
   $\text{List}(r_0, \ldots, r_{n-1})$.
5. Para la segunda afirmación: si $k \geq n$, por el Lema 3
   $r_k \leq r_n \leq \mu_{min}$, así que ninguna réplica posterior supera el
   mínimo. $\blacksquare$

Comprobación:

```math
m = 5.0,\ \mu_{min} = 3.0:\quad r = (5.0,\ 4.0,\ 3.2,\ 2.56,\ \ldots) \Rightarrow n = 3 \Rightarrow \text{List}(5.0, 4.0, 3.2)
```

```math
m = 2.0,\ \mu_{min} = 3.0:\quad r_0 = 2.0 \leq 3.0 \Rightarrow n = 0 \Rightarrow \text{List}()
```

El mínimo es estricto: si $r_k = \mu_{min}$ la réplica no entra, porque
$r_k > \mu_{min}$ es falso.

### 4.5 No se calculan más elementos de los pedidos

**Proposición.** Con memoización de cada celda del `LazyList`:

- `replicas(m).take(j).toList` calcula exactamente $j$ elementos.
- $\text{replicasSensibles}(m, \mu_{min})$ calcula exactamente $n + 1$ elementos:
  $r_0, \ldots, r_n$.

_Argumento._ Un elemento solo se calcula cuando un consumidor lo pide.
`toList` pide elementos de uno en uno, de la posición $0$ en adelante, y solo
pide el siguiente si el anterior fue aceptado. Con `take(j)` se aceptan las
posiciones $0, \ldots, j-1$ y el consumo termina. Con `takeWhile`, por el
Teorema B se aceptan $0, \ldots, n-1$ y la posición $n$ se evalúa solo para
comprobar que falla. Ninguna posición mayor se pide. La memoria usada es
proporcional a $n$, no al largo infinito de la secuencia.

### 4.6 Condiciones sobre los datos y precisión

- **Si $\mu_{min} < 0$ la función no termina.** Entonces $r_k \geq 0 > \mu_{min}$
  para todo $k$, nunca se cumple $r_k \leq \mu_{min}$ (el Lema 4 necesita
  $\mu_{min} \geq 0$), `takeWhile` no se detiene y `toList` se cuelga. Por eso se
  exige $\mu_{min} \geq 0$. Con $m \leq 0$ tampoco vale el Lema 3.
- **Punto flotante.** El programa calcula $a_{k+1}$ multiplicando en `Double`,
  no evalúa $m \cdot 0.8^k$ en los reales, así que los valores crudos tienen
  errores del orden de $10^{-16}$ (por ejemplo, $3.2 \cdot 0.8$ da
  `2.5600000000000005`). El argumento es exacto sobre los reales; en `Double`
  solo podría cambiar un resultado si $100\,a_k + \tfrac12$ cae a $10^{-14}$
  de un entero, y el redondeo a dos decimales absorbe ese error en los casos
  del taller.

### 4.7 Independencia del orden de recorrido

El punto 4 no recorre un registro. La secuencia $(r_k)$ está determinada solo
por $(m, \mu_{min})$ y su orden es parte de la definición (la posición $k$ es
el número de réplica), así que la función es pura: las mismas entradas dan
siempre el mismo resultado. Cuando el punto 5 la usa sobre la magnitud máxima
de una región, el resultado depende solo de ese valor máximo, y por tanto no
depende del orden de los sismos del registro (ver Teorema D).

---

## Punto 5: informe combinado
### 5.1 Especificación

Para un registro $r$ y un mínimo $\mu_{min}$, para cada región
$g \in R(r)$ se definen:

```math
c_g = \text{len}(S_g(r)), \qquad M_g = \max\{\mu(s) \mid s \in S_g(r)\}, \qquad n_g = \text{len}\bigl(\text{replicasSensibles}(M_g, \mu_{min})\bigr)
```

El enunciado pide:

```math
\text{informe}(r, \mu_{min}) = \{\, g \mapsto \text{Resumen}(c_g,\ M_g,\ n_g) \mid g \in R(r) \,\}
```

Es decir, un mapa cuyas llaves son exactamente las regiones del registro y sin
entradas para regiones ausentes. Para $r = \text{List}()$ debe ser $\text{Map}()$.

### 5.2 Corrección

**Lema 5 (`porRegion`, del punto 2).** `porRegion(r)` tiene como llaves
exactamente $R(r)$ y `porRegion(r)(g)` $= S_g(r)$ para toda $g \in R(r)$. Es la
especificación de `groupBy`, que conserva el orden relativo dentro de cada grupo.

**Teorema C.** Para todo registro $r$ y $\mu_{min} \geq 0$, el programa
`informe` cumple la especificación 5.1.

_Demostración._ Por inducción estructural sobre $r$.

**Caso base:** $r = \text{List}()$. $R(r) = \emptyset$, y por el Lema 5
`porRegion(List())` es `Map()`. Un `map` sobre un mapa vacío no visita ninguna
entrada y devuelve `Map()`. La especificación también da el mapa vacío.

**Caso de inducción:** $r = s :: r'$, con $g_0 = \rho(s)$. La hipótesis de
inducción (HI) dice que `informe(r')` cumple la especificación. Comparando
$r$ con $r'$ (el sismo $s$ queda al frente de $S_{g_0}$):

- Si $g \neq g_0$: $S_g(r) = S_g(r')$, así que $c_g$, $M_g$ y $n_g$ no cambian
  y la entrada coincide con la de $r'$, correcta por HI.
- Si $g = g_0$: $S_{g_0}(r) = s :: S_{g_0}(r')$, luego $c_{g_0}(r) = c_{g_0}(r') + 1$
  (o $c_{g_0} = 1$ si $g_0$ era una región nueva), y
  $M_{g_0}(r) = \max(\mu(s),\ M_{g_0}(r'))$ (o $\mu(s)$ si era nueva).
  $n_{g_0}$ se recalcula sobre ese nuevo máximo.

Falta ver que el programa calcula bien cada entrada, para $g \in R(r)$:

1. Por el Lema 5, $\text{porRegion}(r)$ contiene el par $(g, S_g(r))$ y las
   llaves son distintas.
2. Como $g \in R(r)$, $S_g(r)$ es no vacía, así que
   `sismos.map(_.magnitud).max` está definido (no lanza excepción) y es $M_g$.
3. `sismos.length` es $c_g$.
4. Por el Teorema B, `replicasSensibles(maxima, minimo)` es la lista
   $\text{List}(r_0, \ldots, r_{n-1})$ de la magnitud $M_g$, y su `length` es
   $n_g$.
5. La función del `map` devuelve el par $(g, \text{Resumen}(c_g, M_g, n_g))$,
   que conserva la llave. Como las llaves de entrada eran distintas, las de
   salida también, y ninguna entrada pisa a otra. $\blacksquare$

Comprobación con `informe(registro, 3.0)`:

| Región | $S_g$ (magnitudes) | $c_g$ | $M_g$ | $r_0, r_1, \ldots$ | $n_g$ |
|---|---|:---:|:---:|---|:---:|
| Valle | 4.2, 3.8, 5.3 | 3 | 5.3 | 5.3, 4.24, 3.39, 2.71 | 3 |
| Santander | 5.1, 4.7 | 2 | 5.1 | 5.1, 4.08, 3.26, 2.61 | 3 |
| Choco | 6.0, 3.1 | 2 | 6.0 | 6.0, 4.8, 3.84, 3.07, 2.46 | 4 |

que coincide con el resultado del enunciado.

### 5.3 El resultado no depende del orden de recorrido

**Teorema D.** Si $r'$ es una permutación de $r$, entonces
$\text{informe}(r', \mu_{min}) = \text{informe}(r, \mu_{min})$.

_Demostración._ Sea $r'$ una permutación de $r$ (los mismos sismos, en otro
orden).

1. $R(r') = R(r)$: ambos conjuntos contienen las regiones de los mismos
   sismos. Las llaves del informe coinciden.
2. Para cada $g$, $S_g(r')$ es una permutación de $S_g(r)$, porque filtrar por
   región selecciona los mismos sismos. Una permutación no cambia la longitud,
   así que $c_g(r') = c_g(r)$.
3. El máximo no depende del orden. La operación $\max$ es conmutativa
   ($\max(a,b) = \max(b,a)$) y asociativa
   ($\max(\max(a,b),c) = \max(a,\max(b,c))$), por lo que cualquier forma de
   combinar los elementos da el mismo valor: el único elemento del conjunto
   de magnitudes que es mayor o igual que todos los demás. Entonces
   $M_g(r') = M_g(r)$.
4. $n_g$ es una función de $(M_g, \mu_{min})$ únicamente (Teorema B), luego
   $n_g(r') = n_g(r)$.
5. Dos mapas son iguales cuando tienen las mismas llaves y el mismo valor para
   cada llave; el orden interno de las entradas no cuenta. Por 1 a 4, todas
   las entradas coinciden. $\blacksquare$

Esto es lo que garantiza que `groupBy` y los pliegues internos (`length`,
`max`) puedan recorrer el registro en cualquier orden sin cambiar el informe.
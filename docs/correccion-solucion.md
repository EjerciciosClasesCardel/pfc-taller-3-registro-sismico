# Informe de corrección

## Consultas con expresiones `for`

`fuertes` recorre el registro en su orden original y conserva un sismo solo
si su magnitud es estrictamente mayor que el umbral. El `yield` devuelve su
región; como no se aplica `distinct`, las regiones repetidas se mantienen.
`enRegion` aplica el mismo razonamiento, seleccionando por nombre y
devolviendo las magnitudes en el orden de entrada. Si no hay elementos que
cumplan la condición, ambas comprensiones producen una lista vacía.

Para `paresCercanos`, la lista inicial de regiones se obtiene del registro y
`distinct` conserva la primera aparición de cada una. A cada región se le
asocian únicamente las regiones que vienen después; por eso cada par de
regiones distintas se considera una sola vez y en el orden de aparición.
Para una pareja, la comprensión interna encuentra combinaciones de sismos
que cumplen
$|\text{magnitud}_1-\text{magnitud}_2|\leq\text{dif}$.
La pareja se produce si existe al menos una combinación así. Si no hay dos
regiones o ningún par de magnitudes cumple la condición, no se produce esa
pareja.

## Agrupación y consultas seguras

`groupBy(_.region)` asigna cada sismo a la lista de su región, sin perder
ningún elemento. Toda lista en ese mapa corresponde a una región presente y
no está vacía. Por ello, su longitud define el conteo y el máximo de sus
magnitudes está definido. Si el registro es vacío, `groupBy` entrega un mapa
vacío y las transformaciones también producen mapas vacíos.

`conteoSeguro` conserva los conteos existentes y define cero como valor por
defecto para una llave ausente. `maximaDe` usa `get`, que devuelve `Some` si
hay una máxima para la región y `None` en caso contrario. Para
`regionMasActiva`, ordenar primero por cantidad descendente y luego por
nombre ascendente coloca en la primera posición una región de conteo máximo
y, entre empates, la primera alfabéticamente. `headOption` da `None` cuando
no existe ninguna región.

## Réplicas e informe

`replicas` parte de la magnitud dada y genera cada término siguiente
multiplicando el anterior sin redondear por `0.8`; `map(redondear)` redondea
los valores observables a dos decimales. La secuencia es perezosa, de modo
que pedir un prefijo calcula solo los elementos de ese prefijo. Para un
mínimo positivo, `takeWhile(_ > minimo)` termina al llegar al primer valor
que no supera el mínimo. La condición es estricta, así que un valor igual al
mínimo queda excluido.

En `informe`, cada grupo contiene al menos un sismo. Por tanto, su longitud
y su magnitud máxima están definidas, y la cantidad de réplicas se calcula
desde esa máxima usando el mismo criterio estricto del punto anterior. Se
genera exactamente una entrada por grupo; para un registro vacío no hay
grupos y el resultado es el mapa vacío.

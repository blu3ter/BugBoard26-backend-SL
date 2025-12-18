/**
 * quickSort
 *
 * Purpose:
 *   Ordina un array di numeri in ordine crescente usando quicksort (puro, non in-place rispetto all'input).
 *
 * Arguments:
 *   array {number[]} - array di numeri da ordinare
 *
 * Returns:
 *   {number[]} - nuovo array ordinato
 *
 * Exceptions:
 *   Throws {TypeError} se l'argomento non è un array o contiene elementi non numerici
 */

function quickSort(array) {
  if (!Array.isArray(array)) throw new TypeError('array must be an Array');
  const arr = array.slice();
  if (arr.length <= 1) return arr;
  _quickSort(arr, 0, arr.length - 1);
  return arr;
}

/**
 * _quickSort
 *
 * Purpose:
 *   Implementazione ricorsiva di quicksort con ottimizzazione: median-of-three pivot e insertion sort per piccole partizioni.
 *
 * Arguments:
 *   arr {number[]} - array da ordinare (modificato in place)
 *   lo {number} - indice sinistro
 *   hi {number} - indice destro
 *
 * Returns:
 *   {void}
 */
function _quickSort(arr, lo, hi) {
  const INSERTION_SORT_THRESHOLD = 16;
  if (lo >= hi) return;
  if (hi - lo + 1 <= INSERTION_SORT_THRESHOLD) {
    _insertionSort(arr, lo, hi);
    return;
  }
  const pivotIndex = _medianOfThree(arr, lo, hi);
  const pivotNewIndex = _partition(arr, lo, hi, pivotIndex);
  _quickSort(arr, lo, pivotNewIndex - 1);
  _quickSort(arr, pivotNewIndex + 1, hi);
}

/**
 * _partition
 *
 * Purpose:
 *   Partiziona l'array attorno al pivot e restituisce l'indice finale del pivot.
 *
 * Arguments:
 *   arr {number[]}
 *   lo {number}
 *   hi {number}
 *   pivotIndex {number}
 *
 * Returns:
 *   {number} - indice del pivot dopo la partizione
 */
function _partition(arr, lo, hi, pivotIndex) {
  const pivotValue = arr[pivotIndex];
  _swap(arr, pivotIndex, hi);
  let storeIndex = lo;
  for (let i = lo; i < hi; i++) {
    if (arr[i] < pivotValue) {
      _swap(arr, i, storeIndex);
      storeIndex++;
    }
  }
  _swap(arr, storeIndex, hi);
  return storeIndex;
}

/**
 * _medianOfThree
 *
 * Purpose:
 *   Seleziona un pivot robusto usando la mediana di arr[lo], arr[mid], arr[hi].
 *
 * Arguments:
 *   arr {number[]}
 *   lo {number}
 *   hi {number}
 *
 * Returns:
 *   {number} - indice della mediana
 */
function _medianOfThree(arr, lo, hi) {
  const mid = lo + ((hi - lo) >> 1);
  const a = arr[lo], b = arr[mid], c = arr[hi];
  if (a > b) {
    if (b > c) return mid;
    if (a > c) return hi;
    return lo;
  } else {
    if (a > c) return lo;
    if (b > c) return hi;
    return mid;
  }
}

/**
 * _insertionSort
 *
 * Purpose:
 *   Ordinamento per inserzione usato per partizioni piccole (migliora le performance complessive).
 *
 * Arguments:
 *   arr {number[]}
 *   lo {number}
 *   hi {number}
 *
 * Returns:
 *   {void}
 */
function _insertionSort(arr, lo, hi) {
  for (let i = lo + 1; i <= hi; i++) {
    const key = arr[i];
    let j = i - 1;
    while (j >= lo && arr[j] > key) {
      arr[j + 1] = arr[j];
      j--;
    }
    arr[j + 1] = key;
  }
}

/**
 * _swap
 *
 * Purpose:
 *   Scambia due elementi in un array.
 *
 * Arguments:
 *   arr {any[]}
 *   i {number}
 *   j {number}
 *
 * Returns:
 *   {void}
 */
function _swap(arr, i, j) {
  const tmp = arr[i];
  arr[i] = arr[j];
  arr[j] = tmp;
}

module.exports = { quickSort };
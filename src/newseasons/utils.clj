(ns newseasons.utils)


(defn sort-maps-by [coll k]
  (sort #(compare (%1 k) (%2 k)) coll))


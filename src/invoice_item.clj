(ns invoice-item)

(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))

(defn valid-item? [item]
  (not (and (some? (:retentionable/retentions item))
            (some? (:taxable/taxes item)))))

(defn search-predicate [{retentions :retentionable/retentions
                         taxes :taxable/taxes}]
  (or (when retentions
        (some (fn [{:retention/keys [category rate]}]
                (and (= category :ret_fuente) (= rate 1))) retentions))
      (when taxes
        (some (fn [{:tax/keys [category rate]}]
                (and (= category :iva) (= rate 19))) taxes))))


(ns invoice-item
  (:require 
   [clojure.string :as str]
   [clojure.data.json :as json])
  (:import java.time.ZonedId
           java.time.LocalDate))

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

(defn invoice-items [{:invoice/keys [items]}]
  (->> items
       (filter valid-item?)
       (filter search-predicate)))

(defn snake->kebab [key-string]
  (str/replace key-string #"_" "-"))

(defn rebind-tax [{:keys [tax-category tax-rate]}]
  (let [tax-lookup-table {"IVA" :iva}]
    #:tax{:category (tax-lookup-table tax-category)
          :rate (double tax-rate)}))

(defn rebind-item [{:keys [price quantity sku taxes]}]
  #:invoice-item{:price (double price)
                 :quantity (double quantity)
                 :sku (or sku "N/A")
                 :taxes (mapv rebind-tax taxes)})

(defn format-date-string [date-str]
  (apply str (interpose "-" (reverse (str/split date-str #"/")))))

(defn date-string->inst [date-str]
  (.toInstant (.atStartOfDay (LocalDate/parse (format-date-string date-str)) (ZoneId/of "America/Bogota"))))

(defn json-file->invoice [file-name]
  (let [{:keys [items issue-date customer]} (:invoice (json/read-str
                                                       (slurp file-name)
                                                       {:key-fn (comp keyword camel->kebab)}))
        {:keys [company-name email]} customer]
    #:invoice{:issue-date (date-string->inst issue-date)
              :customer #:customer{:name company-name
                                   :email email}
              :items (mapv rebind-item items)}))

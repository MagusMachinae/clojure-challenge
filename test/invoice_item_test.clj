(ns invoice-item-test
  (:require [clojure.test :as test :refer [deftest]]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            [invoice-spec :as sut-spec]
            [invoice-item :as sut]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(deftest json-file->invoice-test
  (test/testing "Spec Conformation" 
   (test/is (s/valid? ::sut-spec/invoice (sut/json-file->invoice "invoice.json")))))

(deftest subtotal-test
  (test/testing "Default Discount passed correctly when rate not given"
    (let [invoice-item #:invoice-item{:precise-quantity 2.00 
                                     :precise-price 10.00}]
      (test/is (= 20.00 (sut/subtotal invoice-item)))))
  (test/testing "Zero Price/Quantity Tests"
    (let [zero-price-item #:invoice-item{:precise-quantity 1.00 
                                        :precise-price 0.00 
                                        :discount-rate 105.00}
          zero-quantity-item #:invoice-item{:precise-quantity 0.00 
                                           :precise-price 100.00 
                                           :discount-rate 105.00}]
      (test/is (zero? (sut/subtotal zero-price-item)))
      (test/is (zero? (sut/subtotal zero-quantity-item)))))
  (test/testing "Discount Boundary Condition Tests"
    (test/testing "100% Discount"
      (let [invoice-item #:invoice-item{:precise-price 200.00 
                                       :precise-quantity 1.00 
                                       :discount-rate 100.00}]
        (test/is (zero? (sut/subtotal invoice-item)))))
    (test/testing "Negative Line-Item Test"
      (let [invoice-item #:invoice-item{:precise-price 200.00 
                                       :precise-quantity 1.00 
                                       :discount-rate 105.00}]
        (test/is (neg? (sut/subtotal invoice-item))))))
  (test/testing "Fractional monetary values truncated correctly"
    (let [invoice-item #:invoice-item{:precise-price 10.55 
                                     :precise-quantity 1.00 
                                     :discount-rate 15.00}]
      (test/is (= 8.97 (sut/subtotal invoice-item)))))
  (test/testing "Item value access compliant with invoice-specs"
    (let [invoice-item #:invoice-item{:price 200.00 
                                     :quantity 1.00 
                                     :discount-rate 50.00}]
      (test/is (= 100 (sut/subtotal invoice-item))))))
  
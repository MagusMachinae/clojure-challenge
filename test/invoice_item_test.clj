(ns invoice-item-test
  (:require [clojure.test :as test]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            [invoice-spec :as sut-spec]
            [invoice-item :as sut]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(deftest json-file->invoice-test
  (test/testing "Spec Conformation" 
   (test/is (s/valid? ::sut-spec/invoice (sut/json-file->invoice "invoice.json")))))


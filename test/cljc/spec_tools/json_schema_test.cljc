(ns spec-tools.json-schema-test
  (:require  [clojure.test :refer [deftest testing is]]
             [clojure.spec :as s]
             [clojure.spec.test :as stest]
             [com.gfredericks.test.chuck.clojure-test :refer [checking]]
             #?(:clj [scjsv.core :as scjsv])
             [spec-tools.core :as st]
             [spec-tools.impl :as impl]
             [spec-tools.json-schema :as jsc]))

(s/def ::int int?)
(s/def ::string string?)
(s/def ::set #{1 2 3})

(deftest simple-spec-test
  (testing "primitive predicates"
    ;; You're intented to call jsc/to-json with a registered spec, but to avoid
    ;; boilerplate, we do inline specization here.
    (is (= (jsc/to-json (s/spec int?)) {:type "integer"}))
    (is (= (jsc/to-json (s/spec integer?)) {:type "integer"}))
    (is (= (jsc/to-json (s/spec float?)) {:type "number"}))
    (is (= (jsc/to-json (s/spec double?)) {:type "number"}))
    (is (= (jsc/to-json (s/spec string?)) {:type "string"}))
    (is (= (jsc/to-json (s/spec boolean?)) {:type "boolean"}))
    (is (= (jsc/to-json (s/spec nil?)) {:type "null"}))
    (is (= (jsc/to-json #{1 2 3}) {:enum [1 3 2]})))
  (testing "clojure.spec predicates"
    (is (= (jsc/to-json (s/nilable ::string)) {:oneOf [{:type "string"} {:type "null"}]}))
    (is (= (jsc/to-json (s/int-in 1 10)) {:allOf [{:type "integer"} {:minimum 1 :maximum 10}]})))
  (testing "simple specs"
    (is (= (jsc/to-json ::int) {:type "integer"}))
    (is (= (jsc/to-json ::set) {:enum [1 3 2]})))
  (testing "composite objects"
    (is (= (jsc/to-json (s/keys :req-un [::int] :opt-un [::string]))
           {:type "object"
            :properties {"int" {:type "integer"} "string" {:type "string"}}
            :required ["int"]}))
    (is (= (jsc/to-json (s/tuple int? string?))
           {:type "array" :items [{:type "integer"} {:type "string"}] :minItems 2}))
    (is (= (jsc/to-json (s/* int?)) {:type "array" :items {:type "integer"}}))
    (is (= (jsc/to-json (s/+ int?)) {:type "array" :items {:type "integer"} :minItems 1}))
    ;; The following tests require the full qualifying of the predicates until
    ;; this is fixed: <http://dev.clojure.org/jira/browse/CLJ-2035>
    (is (= (jsc/to-json (s/every clojure.core/int?)) {:type "array" :items {:type "integer"}}))
    (is (= (jsc/to-json (s/map-of clojure.core/string? clojure.core/integer?))
           {:type "object" :additionalProperties {:type "integer"}})))
  (testing "composite specs"
    (is (= (jsc/to-json (s/or :int int? :string string?))
           {:anyOf [{:type "integer"} {:type "string"}]}))
    (is (= (jsc/to-json (s/and int? pos?))
           {:allOf [{:type "integer"} {:minimum 0 :exclusiveMinimum true}]}))))

#?(:clj
   (defn test-spec-conversion [spec]
     (let [validate (scjsv/validator (jsc/to-json spec))]
       (testing (str "with spec " spec)
         (checking "JSON schema accepts the data generated by the spec gen" 100
           [x (s/gen spec)]
           (is (nil? (validate x))))))))

(s/def ::compound (s/keys :req-un [::int] :opt-un [::string]))

#?(:clj
   (deftest validating-test
     (test-spec-conversion ::int)
     (test-spec-conversion ::string)
     (test-spec-conversion ::set)
     (test-spec-conversion ::compound)
     (test-spec-conversion (s/nilable ::string))
     (test-spec-conversion (s/int-in 0 100))))

;; Test the example from README

(s/def ::age (s/and integer? #(> % 18)))

(def person-spec
  (st/coll-spec
   ::person
   {::id clojure.core/integer?
    :age ::age
    :name clojure.core/string?
    :likes {clojure.core/string? clojure.core/boolean?}
    (st/req :languages) #{clojure.core/keyword?}
    (st/opt :address) {:street clojure.core/string?
                       :zip clojure.core/string?}}))

(deftest readme-test
  (is (= {:type "object"
          :required ["id" "age" "name" "likes" "languages"]
          :properties
          {"id" {:type "integer"}
           "age" {:type "integer"}  ; not supporting > yet
           "name" {:type "string"}
           "likes" {:type "object" :additionalProperties {:type "boolean"}}
           "languages" {:type "array", :items {:type "string"}}
           "address" {:type "object"
                      :required ["street" "zip"]
                      :properties {"street" {:type "string"}
                                   "zip" {:type "string"}}}}}
         (jsc/to-json person-spec))))

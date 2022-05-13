(require '[clojure.test :as t]
         '[clojure.spec.alpha :as s]
         '[spec-tools.core :as st])

(require '[spec-tools
           core-test
           data-spec-test
           impl_test
           json-schema-test
           parse-test
           spec-test
           spell-test
           transform-test
           visitor-test
           visitor-all-test]
         '[spec-tools.openapi
           core-test
           spec-test]
         '[spec-tools.spell-spec
           expound-test
           alpha-test]
         '[spec-tools.swagger
           core-test
           spec-test])


(t/run-tests
  'spec-tools.core-test
  'spec-tools.data-spec-test
  'spec-tools.impl-test
  'spec-tools.json-schema-test
  'spec-tools.parse-test
  'spec-tools.spec-test
  'spec-tools.spell-test
  'spec-tools.transform-test
  'spec-tools.visitor-all-test
  'spec-tools.visitor-test
  'spec-tools.openapi.core-test
  'spec-tools.openapi.spec-test
  'spec-tools.spell-spec.expound-test
  'spec-tools.spell-spec.alpha-test
  'spec-tools.swagger.core-test
  'spec-tools.swagger.spec-test
  )

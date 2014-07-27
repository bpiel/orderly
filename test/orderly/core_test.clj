(ns orderly.core-test
  (:require [midje.sweet :refer :all]
            [orderly.core :refer :all]))

(def plan [{:inputs [:a]
            :outputs [:b]
            :action #(hash-map :b (+ 1 (:a %)))}
           
           {:inputs [:b]
            :outputs [:c]
            :action #(hash-map :c (+ 1 (:b %)))}
           ])

(fact "execute a simple plan with one input and one output"
      (execute {:a 5} [:c] plan)
      => {:c 7})

(fact "execute a simple plan with one input and two outputs"
      (execute {:a 5} [:c :b] plan)
      => {:c 7 :b 6})

(fact "execute-recur pulls an input out of the prepared plan"
      (let [prepped-plan (prep-plan plan {:a 5})]
        (execute-recur :a prepped-plan)
        )
      => {:a 5})

(fact "Prepare the plan -- not much to test here really"
      (map #(select-keys % [:inputs :outputs]) (prep-plan plan-b {:a 5}))
      => [{:inputs [:f :g :h1]
           :outputs [:i]}
          {:inputs [:d3]
           :outputs [:h1 :h2]}
          {:inputs [:e1 :e2 [:vector :key] :d1]
           :outputs [:g]}
          {:inputs [:e1]
           :outputs [:f]}
          {:inputs [[:vector :key]]
           :outputs [:e1 :e2]}
          {:inputs [[:vector :key]]
           :outputs [:d1 :d2 :d3]}
          {:inputs [:a :b]
           :outputs [[:vector :key]]}
          {:inputs []
           :outputs [:a]}])


(def plan-b [{:inputs [:a :b]
              :outputs [[:vector :key]]
              :action #(hash-map [:vector :key] (+ 1 (:b %)))}
             
             {:inputs [[:vector :key]]
              :outputs [:d1 :d2 :d3]
              :action (fn [x] (Thread/sleep 0)
                        (hash-map :d1 (- (get x [:vector :key]) 1)
                                  :d2 (- (get x [:vector :key]) 2)
                                  :d3 (- (get x [:vector :key]) 3)))}
             
             {:inputs [[:vector :key]]
              :outputs [:e1 :e2]
              :action (fn [x] (Thread/sleep 0)
                        (hash-map :e1 (+ 10 (get x [:vector :key]))
                                  :e2 (+ 100 (get x [:vector :key]))))}
             
             {:inputs [:e1]
              :outputs [:f]
              :action #(hash-map :f (/ (:e1 %) 2))}
             
             {:inputs [:e1 :e2 [:vector :key] :d1]
              :outputs [:g]
              :action #(hash-map :g (+ (:e1 %) (:e2 %) (get % [:vector :key]) (:d1 %)))}
             
             {:inputs [:d3]
              :outputs [:h1 :h2]
              :action #(hash-map :h1 (* -1 (:d3 %))
                                 :h2 nil)}
             
             {:inputs [:f :g :h1]
              :outputs [:i]
              :action #(hash-map :i (+ (:f %) (:g %) (:h1 %)))}])

(fact "execute a most complex plan with two inputs and two outputs"
      (execute {:a 1 :b 2} [:i :d2] plan-b)
      => {:d2 1 :i 255/2})
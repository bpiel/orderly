(ns orderly.core)

(defn cache-wrapper
  "wraps a function in another function that adds a sort-of argument-value-agnostic memoization"
  [f] 
  (let [cache (atom ::not-set)] 
    (fn [x] 
      (locking cache 
        (if (= @cache ::not-set)
          (reset! cache 
                  (f x)))) 
      @cache)))

(defn prep-plan
  "Add provided input values to plan. Apply cache-wrapper to each action function."
  [plan input-map]
  (into (map #(hash-map :inputs []
                        :outputs [(first %)]
                        :action (fn [im] (hash-map (first %) (second %))))
             (seq input-map))
        
        (map (fn [step] (update-in step 
                                   [:action] 
                                   #(cache-wrapper %)))
             plan)))

(defn execute-recur
  "recursive function that ascends through dependency tree and executes actions on the way back"
  [output prepped-plan]
  (let [step (first (filter #(some (set [output])
                                   (:outputs %))
                            prepped-plan))
        input-map (or (apply merge (pmap #(select-keys
                                            (execute-recur % prepped-plan)
                                            (:inputs step))
                                         (:inputs step)))
                      {})]
    
    (select-keys ((:action step) input-map)
                 [output])))

(defn execute
  "Takes inputs, outputs requested and the plan. Executes the plan. Returns outputs."
  [input-map output-list plan]
  (let [prepped-plan (prep-plan plan input-map)]
    (select-keys (apply merge 
                        (pmap #(execute-recur % prepped-plan)
                              output-list))
                 output-list)))


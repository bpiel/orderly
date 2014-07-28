orderly
=======

You specify a plan. Orderly executes the plan.

A *plan* consists of a vector of steps. Each step consists of the following properties:

- inputs -- a vector of inputs the step requires
- outputs -- a vector of outputs the step produces
- actions -- a function which constitutes the work to be done when the step is reached

##### Fun facts
- The inputs of a given step must match the outputs of whatever steps it depends on for input values.
- Steps may be specified in any order.
- Steps will be executed as necessary in whatever order is necessary to satisfy the input/output matching requirement.
- Any inputs that do not match ouputs of others steps must be provided when the plan is executed.
- Any steps that can be run in parallel will be, using `pmap`.
- A function used as a step's action should accept a single argument, which will be a hashmap of inputs, and return a hashmap of outputs. The keys of these hashmaps must match the inputs and outputs listed in the step, respectively.

##### Simple example

```clojure
user=> (def plan [{:inputs [:a]
  #_=>             :outputs [:b]
  #_=>             :action #(hash-map :b (+ 1 (:a %)))}
  #_=>            
  #_=>            {:inputs [:b]
  #_=>             :outputs [:c]
  #_=>             :action #(hash-map :c (+ 1 (:b %)))}
  #_=>            
  #_=>            {:inputs [:b]
  #_=>             :outputs [:d]
  #_=>             :action #(hash-map :d (+ 2 (:b %)))}
  #_=>            
  #_=>            {:inputs [:c :d]
  #_=>             :outputs [:e]
  #_=>             :action #(hash-map :e (* (:c %) (:d %)))}           
  #_=>            ])
#'user/plan
user=> (execute {:a 3} [:e] plan)
{:e 30}
```

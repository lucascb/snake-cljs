(ns snake.game
  (:require [clojure.set :as set]))

(defn get-next-food-pos
  [grid-size snake]
  (let [all-pos (set (for [x (range grid-size)
                           y (range grid-size)]
                       [x y]))]
    (->> snake
         set
         (set/difference all-pos)
         vec
         rand-nth)))

(defn get-next-pos
  [[x y] direction]
  (case direction
    :right [(inc x) y]
    :left [(dec x) y]
    :up [x (dec y)]
    :down [x (inc y)]))

(defn will-get-food?
  [snake direction food]
  (= food (get-next-pos (peek snake) direction)))

(defn eat
  [state]
  (let [{grid-size :grid-size
         snake :snake
         food :food
         score :score} state
        growed-snake (conj snake food)]
    (assoc state
           :snake growed-snake
           :food (get-next-food-pos grid-size growed-snake)
           :score (inc score))))

(defn move
  [state]
  (let [{snake :snake
         direction :direction} state]
    (assoc state
           :snake (conj (vec (rest snake))
                        (get-next-pos (peek snake) direction)))))

(defn eat-or-move
  [state]
  (let [{snake :snake
         direction :direction
         food :food} state]
    (cond (will-get-food? snake direction food)
          (eat state)
          :else (move state))))

(defn outside-screen?
  [{grid-size :grid-size snake :snake}]
  (let [[x y] (peek snake)]
    (not (and (< 0 x grid-size)
              (< 0 y grid-size)))))

(defn self-collided?
  [{snake :snake}]
  (let [snake-head (peek snake)]
    (boolean (some #(= % snake-head)
                   (butlast snake)))))

(defn check-dead
  [state]
  (assoc state
         :dead (or (outside-screen? state)
                   (self-collided? state))))

(defn update-high-score
  [state]
  (assoc state :high-score (cond (:dead state)
                                 (max (:score state) (:high-score state))
                                 :else (:high-score state))))

(defn get-next-state
  [state]
  (-> state
      eat-or-move
      check-dead
      update-high-score))

(defn get-initial-state
  [grid-size high-score]
  (let [mid-screen (/ grid-size 2)
        snake [[mid-screen mid-screen]]]
    {:grid-size grid-size
     :snake snake
     :direction :right
     :food (get-next-food-pos grid-size snake)
     :score 0
     :high-score high-score
     :dead false}))

(defn change-direction
  [state new-direction]
  (let [forbidden-moves #{[:up :down]
                          [:down :up]
                          [:left :right]
                          [:right :left]}]
    (cond (not (contains? forbidden-moves
                          [(:direction state) new-direction]))
          (assoc state :direction new-direction)
          :else state)))

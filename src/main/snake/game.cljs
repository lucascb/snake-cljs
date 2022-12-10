(ns snake.game
  (:require [clojure.set :as set]))

(defn- get-next-food-pos
  [grid-size snake]
  (let [all-pos (set (for [x (range grid-size)
                           y (range grid-size)]
                       [x y]))]
    (->> snake
         set
         (set/difference all-pos)
         vec
         rand-nth)))

(defn- get-next-pos
  [[x y] direction grid-size]
  (case direction
    :right [(mod (inc x) grid-size) y]
    :left [(mod (dec x) grid-size) y]
    :up [x (mod (dec y) grid-size)]
    :down [x (mod (inc y) grid-size)]))

(defn- will-get-food?
  [snake direction food grid-size]
  (= food (get-next-pos (peek snake) direction grid-size)))

(defn- eat
  [state]
  (let [{:keys [grid-size snake food score]} state
        growed-snake (conj snake food)]
    (assoc state
           :snake growed-snake
           :food (get-next-food-pos grid-size growed-snake)
           :score (inc score))))

(defn- move
  [state]
  (let [{:keys [snake direction grid-size]} state]
    (assoc state
           :snake (conj (vec (rest snake))
                        (get-next-pos (peek snake) direction grid-size)))))

(defn- eat-or-move
  [state]
  (let [{:keys [snake direction food grid-size]} state]
    (cond (will-get-food? snake direction food grid-size)
          (eat state)
          :else (move state))))

(defn- self-collided?
  [{snake :snake}]
  (let [snake-head (peek snake)]
    (boolean (some #(= % snake-head)
                   (butlast snake)))))

(defn- check-dead
  [state]
  (assoc state :dead (self-collided? state)))

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

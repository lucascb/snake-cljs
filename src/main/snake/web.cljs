(ns snake.web
  (:require [snake.game :as game])) 

;; Constants
(def grid-size 60)
(def block-size (/ grid-size 10))
(def canvas-size (* grid-size block-size))
(def mid-screen (/ canvas-size 2))
(def game-clock 70)

;; HTML Elements
(def canvas (.getElementById js/document "game-canvas"))
(def canvas-ctx (.getContext canvas "2d"))
(def score-txt (.getElementById js/document "score"))
(def start-button (.getElementById js/document "start"))

;; Game state
(def game-state (atom {}))

;; UI handlers
(defn change-direction-on-keydown!
  [event state]
  (let [direction (case (.-key event)
                    "ArrowLeft" :left
                    "ArrowUp" :up
                    "ArrowRight" :right
                    "ArrowDown" :down)]
    (swap! state #(game/change-direction % direction))))

(defn draw-block
  [canvas-ctx block-size [x y]]
  (.fillRect canvas-ctx
             (* x block-size)
             (* y block-size)
             block-size
             block-size))

(defn get-scores-display-text
  [score high-score]
  (str "Score: " score " - High Score: " high-score))

(defn draw-game-state
  [{snake :snake
    food :food
    score :score
    high-score :high-score}]
  (let [draw (partial draw-block canvas-ctx block-size)]
    (set! (.-fillStyle canvas-ctx) "rgb(255,255,255)")
    (.fillRect canvas-ctx 0 0 canvas-size canvas-size)
    (set! (.-fillStyle canvas-ctx) "rgb(255,0,0)")
    (draw food)
    (set! (.-fillStyle canvas-ctx) "rgb(0,0,0)")
    (doseq [part snake] (draw part))
    (set! (.-innerHTML score-txt) (get-scores-display-text score high-score))))

(defn game-over
  []
  (set! (.-font canvas-ctx) "30px Lucida")
  (.fillText canvas-ctx "Game Over!" (- mid-screen 80) mid-screen)
  (set! (.-disabled start-button) false))

(defn game-loop
  [game-state]
  (let [state @game-state]
    (draw-game-state state)
    (cond (not (:dead state))
          (do (swap! game-state game/get-next-state)
              (js/setTimeout #(game-loop game-state) game-clock))
          :else (game-over))))

(defn start-game
  [game-state grid-size]
  (let [state @game-state]
    (cond (empty? state)
          (swap! game-state #(game/get-initial-state grid-size 0))
          :else (swap! game-state
                       #(game/get-initial-state grid-size (:high-score state))))
    (set! (.-disabled start-button) true)
    (game-loop game-state)))

(defn init-game-screen
  []
  (set! (.-width canvas) canvas-size)
  (set! (.-height canvas) canvas-size)
  (set! (.-fillStyle canvas-ctx) "rgb(255,255,255)")
  (.fillRect canvas-ctx 0 0 canvas-size canvas-size)
  (set! (.-innerHTML score-txt) (get-scores-display-text 0 0))
  (.addEventListener js/document
                     "keydown"
                     #(change-direction-on-keydown! % game-state))
  (set! (.-onclick start-button) #(start-game game-state grid-size)))

(defn -main
  []
  (init-game-screen))

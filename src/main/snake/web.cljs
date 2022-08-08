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
(defn change-direction-on-keydown
  [event]
  (when-let [direction (case (.-key event)
                         "ArrowLeft" :left
                         "ArrowUp" :up
                         "ArrowRight" :right
                         "ArrowDown" :down
                         nil)]
    (swap! game-state #(game/change-direction % direction))))

(defn draw-block
  [[x y]]
  (.fillRect canvas-ctx
             (* x block-size)
             (* y block-size)
             block-size
             block-size))

(defn get-scores-display-text
  [score high-score]
  (str "Score: " score " - High Score: " high-score))

(defn clear-screen
  []
  (set! (.-fillStyle canvas-ctx) "rgb(255,255,255)")
  (.fillRect canvas-ctx 0 0 canvas-size canvas-size))

(defn set-score
  [score high-score]
  (set! (.-innerHTML score-txt) (get-scores-display-text score high-score)))

(defn draw-game-state
  [{snake :snake
    food :food
    score :score
    high-score :high-score}] 
  (clear-screen)
  (set! (.-fillStyle canvas-ctx) "rgb(255,0,0)")
  (draw-block food)
  (set! (.-fillStyle canvas-ctx) "rgb(0,0,0)")
  (doseq [part snake] (draw-block part))
  (set-score score high-score))

(defn game-over
  []
  (set! (.-font canvas-ctx) "30px Lucida")
  (.fillText canvas-ctx "Game Over!" (- mid-screen 80) mid-screen)
  (set! (.-disabled start-button) false))

(defn game-loop
  []
  (let [state @game-state]
    (draw-game-state state)
    (cond (not (:dead state))
          (do (swap! game-state game/get-next-state)
              (js/setTimeout game-loop game-clock))
          :else (game-over))))

(defn start-game
  []
  (let [high-score (get @game-state :high-score 0)]
    (swap! game-state #(game/get-initial-state grid-size high-score))
    (set! (.-disabled start-button) true)
    (game-loop)))

(defn init-game-screen
  []
  (set! (.-width canvas) canvas-size)
  (set! (.-height canvas) canvas-size)
  (clear-screen)
  (set-score 0 0)
  (.addEventListener js/document
                     "keydown"
                     change-direction-on-keydown)
  (set! (.-onclick start-button) start-game))

(defn -main
  []
  (init-game-screen))

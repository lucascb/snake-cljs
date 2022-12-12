(ns snake.web
  (:require [snake.game :as game])) 

;; Constants
(def grid-size 50)
(def game-clock 70)

;; HTML Elements
(def canvas (.getElementById js/document "game-canvas"))
(def canvas-ctx (.getContext canvas "2d"))
(def score-txt (.getElementById js/document "score"))
(def start-button (.getElementById js/document "start"))
(def game-over-text (.getElementById js/document "game-over"))

;; Global states
(defonce game-state (atom {}))
(defonce touch-state (atom nil))

;; Event handlers
(defn handle-keydown
  [event]
  (when-let [direction (case (.-key event)
                         "ArrowLeft" :left
                         "ArrowUp" :up
                         "ArrowRight" :right
                         "ArrowDown" :down
                         nil)]
    (swap! game-state #(game/change-direction % direction))))

(defn get-current-touch-pos
  [event]
  (let [touch-event (first (.-touches event))
        touch-x (.-clientX touch-event)
        touch-y (.-clientY touch-event)]
    [touch-x touch-y]))

(defn handle-touch-start
  [event]
  (swap! touch-state #(get-current-touch-pos event)))

(defn get-touch-direction
  [[x-start y-start] [x-end y-end]]
  (let [x-diff (- x-start x-end)
        y-diff (- y-start y-end)]
    (if (> (abs x-diff) (abs y-diff))
      (if (pos? x-diff) :left :right)
      (if (pos? y-diff) :up :down))))

(defn handle-touch-move
  [event]
  (when-let [first-touch @touch-state]
    (let [direction (get-touch-direction first-touch (get-current-touch-pos event))]
      (swap! game-state #(game/change-direction % direction))
      (reset! touch-state nil))))

;; UI handlers
(defn draw-block
  [[x y] block-width block-height]
  (.fillRect canvas-ctx
             (* x block-width)
             (* y block-height)
             block-width
             block-height))

(defn draw-food
  [food-pos block-width block-height]
  (set! (.-fillStyle canvas-ctx) "rgb(255,0,0)")
  (draw-block food-pos block-width block-height))

(defn draw-snake
  [snake block-width block-height]
  (set! (.-fillStyle canvas-ctx) "rgb(0,0,0)")
  (doseq [part snake] (draw-block part block-width block-height)))

(defn clear-screen
  []
  (set! (.-fillStyle canvas-ctx) "rgb(255,255,255)")
  (.fillRect canvas-ctx 0 0 (.-width canvas) (.-height canvas)))

(defn set-score-text
  [score high-score]
  (set! (.-innerHTML score-txt) (str "Score: " score " - High Score: " high-score)))

(defn draw-game-state
  [{:keys [snake food score high-score]}]
  (let [block-width (quot (.-width canvas) grid-size)
        block-height (quot (.-height canvas) grid-size)]
    (clear-screen)
    (draw-food food block-width block-height)
    (draw-snake snake block-width block-height)
    (set-score-text score high-score)))

(defn game-over
  [{:keys [high-score]}]
  (set! (.-visibility (.-style game-over-text)) "visible")
  (set! (.-disabled start-button) false)
  (.setItem js/localStorage "high-score" high-score))

(defn game-loop
  []
  (let [state @game-state]
    (draw-game-state state)
    (if (not (:dead state))
      (do (swap! game-state game/get-next-state)
          (js/setTimeout game-loop game-clock))
      (game-over state))))

(defn start-game
  [high-score]
  (swap! game-state #(game/get-initial-state grid-size high-score))
  (set! (.-visibility (.-style game-over-text)) "hidden")
  (set! (.-disabled start-button) true)
  (game-loop))

(defn get-current-high-score
  []
  (or (.getItem js/localStorage "high-score") 0))

(defn init-game-screen
  []
  (let [high-score (get-current-high-score)]
    (clear-screen)
    (set-score-text 0 high-score)
    (.addEventListener js/document "keydown" handle-keydown)
    (.addEventListener js/document "touchstart" handle-touch-start)
    (.addEventListener js/document "touchmove" handle-touch-move)
    (set! (.-onclick start-button) #(start-game high-score))))

(defn -main
  []
  (init-game-screen))

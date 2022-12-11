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
(defonce touch-state (atom []))

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

(defn get-touch-pos
  [event]
  (let [touch-event (first (.-touches event))
        touch-x (.-clientX touch-event)
        touch-y (.-clientY touch-event)]
    [touch-x touch-y]))

(defn handle-touch-start
  [event]
  (swap! touch-state #(get-touch-pos event)))

(defn handle-touch-move
  [event]
  (when-let [state (seq @touch-state)]
    (let [[x-start y-start] state
          [x y] (get-touch-pos event)
          x-diff (- x-start x)
          y-diff (- y-start y)
          direction (if (> (abs x-diff) (abs y-diff))
                      (if (pos? x-diff) :left :right)
                      (if (pos? y-diff) :up :down))]
      (swap! game-state #(game/change-direction % direction))
      (reset! touch-state []))))

(defn draw-block
  [[x y] block-width block-height]
  (.fillRect canvas-ctx
             (* x block-width)
             (* y block-height)
             block-width
             block-height))

(defn get-scores-display-text
  [score high-score]
  (str "Score: " score " - High Score: " high-score))

(defn clear-screen
  []
  (set! (.-fillStyle canvas-ctx) "rgb(255,255,255)")
  (.fillRect canvas-ctx 0 0 (.-width canvas) (.-height canvas)))

(defn set-score
  [score high-score]
  (set! (.-innerHTML score-txt) (get-scores-display-text score high-score)))

(defn draw-game-state
  [{:keys [snake food score high-score]}]
  (let [block-width (quot (.-width canvas) grid-size)
        block-height (quot (.-height canvas) grid-size)]
    (clear-screen)
    (set! (.-fillStyle canvas-ctx) "rgb(255,0,0)")
    (draw-block food block-width block-height)
    (set! (.-fillStyle canvas-ctx) "rgb(0,0,0)")
    (doseq [part snake] (draw-block part block-width block-height))
    (set-score score high-score)))

(defn game-over
  [{:keys [high-score]}]
  (set! (.-hidden game-over-text) false)
  (set! (.-disabled start-button) false)
  (.setItem js/localStorage "high-score" high-score))

(defn game-loop
  []
  (let [state @game-state]
    (draw-game-state state)
    (cond (not (:dead state))
          (do (swap! game-state game/get-next-state)
              (js/setTimeout game-loop game-clock))
          :else (game-over state))))

(defn get-current-high-score
  []
  (or (.getItem js/localStorage "high-score") 0))

(defn start-game
  []
  (let [high-score (get-current-high-score)]
    (swap! game-state #(game/get-initial-state grid-size high-score))
    (set! (.-hidden game-over-text) true)
    (set! (.-disabled start-button) true)
    (game-loop)))

(defn init-game-screen
  []
  (clear-screen)
  (set-score 0 (get-current-high-score))
  (.addEventListener js/document "keydown" change-direction-on-keydown)
  (.addEventListener js/document "touchstart" handle-touch-start)
  (.addEventListener js/document "touchmove" handle-touch-move)
  (set! (.-onclick start-button) start-game))

(defn -main
  []
  (init-game-screen))

(ns cljs-repl-web.code-mirror.handlers
  (:require [re-frame.core :refer [register-handler dispatch]]))

(def initial-state {:items []
                    :hist-pos 0
                    :history [""]
                    :cm-inst nil})

(register-handler
 :focus-console-editor
 (fn register-console-cm-instance [db [_ console-key]]
   (when-let [cm-instance (get-in db [:consoles (name console-key) :cm-inst])]
     (.focus cm-instance))
   db))

(register-handler
 :register-console-cm-instance
 (fn register-console-cm-instance [db [_ console-key inst]]
   (assoc-in db [:consoles (name console-key) :cm-inst] inst)))

(register-handler
 :clear-console-items
 (fn clear-console-items [db [_ console-key]]
   (dispatch [:focus-console-editor console-key])
   (assoc-in db [:consoles (name console-key) :items] [])))

(register-handler
 :reset-console-items
 (fn reset-console-items [db [_ console-key]]
   (dispatch [:focus-console-editor console-key])
   (let [current-state (get-in db [:consoles (name console-key)])
         new-state (merge current-state
                          (select-keys initial-state [:items :hist-pos :history]))]
    (assoc-in db [:consoles (name console-key)] new-state))))

(register-handler
 :add-console-item
 (fn add-console-item [db [_ console-key item]]
   (update-in db [:consoles (name console-key) :items] conj item)))

(register-handler
 :add-console-items
 (fn add-console-items [db [_ console-key items]]
   (update-in db [:consoles (name console-key) :items] concat items)))

(register-handler
 :add-console-input
 (fn add-console-input [db [_ console-key input ns]]
   (let [inum (count (get-in db [:consoles (name console-key) :history]))]
     (-> db
         (assoc-in [:consoles (name console-key) :hist-pos] 0)
         (update-in [:consoles (name console-key) :history] conj "")
         (update-in [:consoles (name console-key) :items] conj {:type :input :text input :num inum :ns ns})))))

(register-handler
 :add-console-result
 (fn add-console-result [db [_ console-key error? value]]
   (update-in db [:consoles (name console-key) :items] conj {:type (if error? :error :output)
                                                              :value value})))

(register-handler
 :add-console-log
 (fn add-console-log [db [_ console-key item]]
   (update-in db [:consoles (name console-key) :items] conj {:type :log :value item})))

(register-handler
 :console-set-text
 (fn console-set-text [db [_ console-key text]]
   (let [history (get-in db [:consoles (name console-key) :history])
         pos (get-in db [:consoles (name console-key) :hist-pos])
         idx (- (count history) pos 1)]
     (-> db
         (assoc-in [:consoles (name console-key) :hist-pos] 0)
         (assoc-in [:consoles (name console-key) :history]
                   (if (= pos 0)
                     (assoc history idx text)
                     (conj history text)))))))

(register-handler
 :console-go-up
 (fn console-go-up [db [_ console-key]]
   (let [pos (get-in db [:consoles (name console-key) :hist-pos])
         len (count (get-in db [:consoles (name console-key) :history]))
         new-pos (if (>= pos (dec len))
                   pos
                   (inc pos))]
     (assoc-in db [:consoles (name console-key) :hist-pos] new-pos))))

(register-handler
 :console-go-down
 (fn console-go-down [db [_ console-key]]
   (let [pos (get-in db [:consoles (name console-key) :hist-pos])
         new-pos (if (<= pos 0)
                   0
                   (dec pos))]
     (assoc-in db [:consoles (name console-key) :hist-pos] new-pos))))

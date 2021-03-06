(ns ^:figwheel-always binary.core
    (:require [clojure.string :as string]
              [goog.net.XhrIo :as gxhr]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn char-array-to-ab [string]
    (let [strlen (.-length string)
          buf (new js/ArrayBuffer strlen)
          arr (new js/Uint8Array buf)]

        (loop [i 0] (when (< i strlen)
                        (aset arr i  (.charCodeAt string i))
                        (recur (inc i))))


        buf))

(def js-types {:uint8 {:size 8
                       :js-get "getUint8"}

               :bit {:size 1
                     :js-get "getUint8"
                     :process (fn [value pos]
                                (bit-test value (mod pos 8)))}})

(def binary-types {
                   :charstring {:sizable true
                                :js-type :uint8
                                :formatter #(string/join (map js/String.fromCharCode %))}
                   :uint8       {:js-type :uint8}
                   :bit         {:js-type :bit}
                   :ubitnum     {:js-type :bit
                                 :formatter #(reduce + (map-indexed
                                                         (fn [i val]
                                                           (if val
                                                             (bit-shift-left 1 i)
                                                             0)) %))

                                 :sizable true}})

(defn parse-type-key [key]
    (let [parts (.split (name key) "-")
          typ ((keyword (first parts)) binary-types)
          length (if (:sizable typ) (js/parseInt (second parts)) 1)
          js-type ((:js-type typ) js-types)]

        (assoc typ :length length :js-type js-type :le (= :le (:last parts)))))


(defn no-process [val _] val)

(defn read-data [dv {:keys [length formatter le js-type], :or {formatter no-process}} index]
  (let [get-func (.bind (aget dv (:js-get js-type)) dv)
        {:keys [size process] :or {process identity}} js-type]

    [(* length size)
     (formatter
       (if (> length 1)
         (loop [pos 0 data []]
           (if (= (/ pos size) length)
             data
             (recur (+ size pos) (conj data (process
                                           (get-func (/ (+ pos index) 8) le)
                                           (+ index pos))))))
         (process (get-func (/ index 8) le) index)))]))


(defn read-spec ([buf spec position]
    (let [dv (new js/DataView buf)
          partitioned (partition 2 spec)]

        (loop [pos 0 items partitioned output {}]
            (let [[item definition] (first items)
                  [size value] (if (seqable? item)
                                 (read-spec buf item position)
                                 (read-data dv (parse-type-key definition) pos))

                  out (assoc output item value)]

              (if (> (count items) 1)
                  (recur (+ pos size) (drop 1 items) out)
                  [pos out])))))

  ([buf spec] (read-spec buf spec 0)))


(def buf (char-array-to-ab "halohhwareyou"))


(defn on-js-reload []
  ;(println "reloaded")
  ;(download-file "/lev.ark")
  (println (read-spec
             buf
             [:my-tile
               [:name :charstring-4
                :two :uint8
                :smallnum :ubitnum-8]]))

  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

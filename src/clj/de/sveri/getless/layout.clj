(ns de.sveri.getless.layout
  (:require [selmer.parser :as parser]
            [clojure.string :as s]
            [ring.util.response :refer [content-type response]]
            ;[ring.util.response :refer [response]]
            ;[ring.util.http-response :refer [content-type ok]]
            [ring.middleware.anti-forgery :as af]
            [compojure.response :refer [Renderable]]
            [noir.session :as sess]))

(defn merge-flash-messages
  "Expects a map containing keys and a values which will be put into the sessions flash"
  [messages]
  (doseq [m messages]
    (sess/flash-put! (key m) (val m))))

(defn flash-result [message div-class]
  (merge-flash-messages {:flash-message message :flash-alert-type div-class}))

(deftype RenderableTemplate [template params]
  Renderable
  (render [this request]
    (content-type
      (->> (assoc params
             (keyword (s/replace template #".html" "-selected")) "active"
             :servlet-context
             (if-let [context (:servlet-context request)]
               ;; If we're not inside a serlvet environment (for
               ;; example when using mock requests), then
               ;; .getContextPath might not exist
               (try (.getContextPath context)
                    (catch IllegalArgumentException _ context)))
             :identity (sess/get :identity)
             :role (sess/get :role)
             :af-token af/*anti-forgery-token*
             :page template
             :registration-allowed? (sess/get :registration-allowed?)
             :captcha-enabled? (sess/get :captcha-enabled?)
             :flash-message (sess/flash-get :flash-message)
             :flash-alert-type (sess/flash-get :flash-alert-type)
             :localize-fn (:localize request))
           (parser/render-file (str template))
           response)
      "text/html; charset=utf-8")))

(defn render [template & [params]]
  (RenderableTemplate. (str "templates/" template) params))

;(parser/set-resource-path!  (clojure.java.io/resource "templates/"))
;
;(defn render
;  "renders the HTML template located relative to resources/templates"
;  [template & [params]]
;  (content-type
;    (ok
;      (parser/render-file
;        template
;        (assoc params
;          :page template
;          :csrf-token af/*anti-forgery-token*
;          ;:servlet-context (if-let [context (:servlet-context request)]
;          ;                   ;; If we're not inside a serlvet environment (for
;          ;                   ;; example when using mock requests), then
;          ;                   ;; .getContextPath might not exist
;          ;                   (try (.getContextPath context)
;          ;                        (catch IllegalArgumentException _ context)))
;          :identity (sess/get :identity)
;          :role (sess/get :role)
;          :af-token af/*anti-forgery-token*
;          :page template
;          :registration-allowed? (sess/get :registration-allowed?)
;          :captcha-enabled? (sess/get :captcha-enabled?)
;          :flash-message (sess/flash-get :flash-message)
;          :flash-alert-type (sess/flash-get :flash-alert-type))))
;          ;:localize-fn (:localize request))))
;    "text/html; charset=utf-8"))


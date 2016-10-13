(ns de.sveri.getless.middleware
  (:require [prone.middleware :as prone]
            ;[selmer.middleware :refer [wrap-error-page]]
            ;[prone.middleware :refer [wrap-exceptions]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [noir.session :as sess]
            [taoensso.tower.ring :refer [wrap-tower]]
            [de.sveri.clojure.commons.middleware.util :refer [wrap-trimmings]]
            [clojure-miniprofiler :refer [wrap-miniprofiler in-memory-store]]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [de.sveri.getless.service.auth :refer [auth-session-backend]]
            [de.sveri.getless.service.auth :as auth]
            [clojure.spec.test :as stest]))

(defonce in-memory-store-instance (in-memory-store))

(defn add-req-properties [handler config]
  (fn [req]
    (sess/put! :registration-allowed? (:registration-allowed? config))
    (sess/put! :captcha-enabled? (:captcha-enabled? config))
    (handler req)))

(def development-middleware
  [#(wrap-miniprofiler % {:store in-memory-store-instance})
   prone/wrap-exceptions
   wrap-reload])

(defn production-middleware [config tconfig]
  [#(add-req-properties % config)
   #(wrap-access-rules % {:rules auth/rules})
   #(wrap-authorization % auth/auth-session-backend)
   #(wrap-tower % tconfig)
   #(wrap-transit-response % {:encoding :json :opts {}})
   wrap-anti-forgery
   wrap-trimmings])

(defn load-middleware [config tconfig]
  (when (= (:env config) :dev) (stest/instrument))
  (concat (production-middleware config tconfig)
          (when (= (:env config) :dev) development-middleware)))

(ns de.sveri.getless.routes.home
  (:require [compojure.core :refer [defroutes GET]]
            [de.sveri.getless.layout :as layout]
            [ring.util.response :refer [response]]))

(defn home-page []
  (layout/render "home/index.html"))

(defn contact-page []
  (layout/render "home/contact.html"))

(defn tos-page []
  (layout/render "home/tos.html"))

(defn cookies-page []
  (layout/render "home/cookies.html"))

(defn ajax-initial-data []
  (response {:ok "fooo" :loaded true}))

(defroutes home-routes
           (GET "/contact" [] (contact-page))
           (GET "/tos" [] (tos-page))
           (GET "/cookies" [] (cookies-page))
           (GET "/" [] (home-page))
           (GET "/ajax/page/init" [] (ajax-initial-data)))

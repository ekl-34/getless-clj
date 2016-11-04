(ns de.sveri.getless.routes.food
  (:require [compojure.core :refer [routes GET POST]]
            [ring.util.response :refer [response redirect]]
            [de.sveri.getless.layout :as layout]
            [de.sveri.getless.db.food :as db-food]
            [de.sveri.getless.service.user :as s-user]
            [de.sveri.getless.service.food :as s-food]
            [de.sveri.getless.service.off :as s-off]
            [clojure.instant :as inst]
            [clojure.tools.logging :as log]))

(defn index-page [db {:keys [off-url off-user off-password]}]
  (layout/render "food/index.html" {:products-list
                                      (s-food/->foods-with-product-grouped-by-date db off-url off-user off-password)}))

(defn add-food-page [{:keys [session]}]
  (layout/render "food/add-food.html" {:products (s-food/get-food-from-session session)}))

(defn add-product [productid {:keys [session]} {:keys [off-url off-user off-password]}]
  (let [session (s-food/add-food-to-session session (s-off/get-by-id productid off-url off-user off-password))]
    (assoc (redirect "/food/add") :session session)))

(defn add-food [date productids amounts units {:keys [session]} db]
  (let [user-id (s-user/get-logged-in-user-id db)]
    (try
      (db-food/insert-food db (.getTime (inst/read-instant-date date)) user-id (mapv read-string productids)
                              (mapv read-string amounts) units)
      (assoc (redirect "/food") :session (s-food/delete-food-from-session session))
      (catch Exception e
        (log/error "Error adding food")
        (.printStackTrace e)))))


(defn contents-page [db {:keys [off-url off-user off-password]}]
  (layout/render "food/contents.html"
                 {:nutriments (-> (s-food/->foods-with-product-grouped-by-date db off-url off-user off-password)
                                  s-food/->nutriments-grouped-by-date)}))

(defn food-routes [config db]
  (routes
    (GET "/food" [] (index-page db config))
    (GET "/food/add" req (add-food-page req))
    (POST "/food/add" [date productid amount unit :as req] (add-food date productid amount unit req db))
    (GET "/food/add/product/:productid" [productid :as req] (add-product productid req config))
    (GET "/food/contents" [] (contents-page db config))))

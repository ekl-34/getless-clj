(ns de.sveri.getless.db.user
  (:require [korma.core :refer [select where insert delete values update set-fields defentity limit order]]
            [korma.db :refer [h2]]
            [de.sveri.getless.db.entities :refer [users]]))

(defn get-all-users [ & [where-email-like]]
  (select users (where {:email [like (str "%" where-email-like "%")]})
          (order :email :asc)))

(defn get-user-by-email [email] (first (select users (where {:email email}) (limit 1))))
(defn get-user-by-id [id] (first (select users (where {:id id}) (limit 1))))

(defn username-exists? [email] (some? (get-user-by-email email)))

(defn create-user [email pw_crypted]
  (insert users (values {:email email :pass pw_crypted :is_active true})))

(defn update-user [id fields] (update users (set-fields fields) (where {:id id})))
(defn delete-user [id] (delete users (where {:id id})))
(defn change-password [email pw] (update users (set-fields {:pass pw}) (where {:email email})))

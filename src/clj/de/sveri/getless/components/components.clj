(ns de.sveri.getless.components.components
  (:require
    [com.stuartsierra.component :as component]
    (system.components
      [repl-server :refer [new-repl-server]])
    [de.sveri.getless.components.server :refer [new-web-server]]
    [de.sveri.getless.components.handler :refer [new-handler]]
    [de.sveri.getless.components.config :as c]
    [de.sveri.getless.components.db :refer [new-db]]
    [de.sveri.getless.components.locale :as l]))


(defn dev-system []
  (component/system-map
    :locale (l/new-locale)
    :config (c/new-config (c/prod-conf-or-dev))
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :locale])
    :web (component/using (new-web-server) [:handler :config])))


(defn prod-system []
  (component/system-map
    :locale (l/new-locale)
    :config (c/new-config (c/prod-conf-or-dev))
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :locale])
    :web (component/using (new-web-server) [:handler :config])))
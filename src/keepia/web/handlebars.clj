(ns keepia.web.handlebars
  "Handlebars templating subsystem"
  (:import (com.github.jknack.handlebars Handlebars ValueResolver Context)
           (com.github.jknack.handlebars.context MapValueResolver 
                                                 JavaBeanValueResolver
                                                 FieldValueResolver)
           com.github.jknack.handlebars.io.ClassPathTemplateLoader
           com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
           java.util.AbstractMap$SimpleImmutableEntry))

(defn handlebars []
  (-> (Handlebars.)
    (.with (ClassPathTemplateLoader. "/templates" ".html"))
    (.with (ConcurrentMapTemplateCache.))))

(defn- map-entry [key value]
  (AbstractMap$SimpleImmutableEntry. key value))

(deftype KeywordValueResolver []
  ValueResolver
  (resolve [this context name]
    (if (associative? context)
      (get context (keyword name) ValueResolver/UNRESOLVED)
      (ValueResolver/UNRESOLVED)))
  (propertySet [this context]
    (if (associative? context)
      (set (for [[k v] context
                 :when (keyword? k)]
             (map-entry (str k) v)))
      #{})))

(def value-resolvers 
  (into-array ValueResolver 
              [(KeywordValueResolver.)
               MapValueResolver/INSTANCE
               JavaBeanValueResolver/INSTANCE
               FieldValueResolver/INSTANCE]))

(defn- context [^java.util.Map model]
  (-> (Context/newBuilder model)
      (.resolver value-resolvers)
      (.build)))

(defn- apply-template [^Handlebars handlebars ^String template model]  
  (.apply (.compile handlebars template) (context model)))

(defn wrap-handlebars [handler handlebars]
  (fn [req]
    (let [res (handler req)
          body (:body res)]
      (if-let [tname (and (associative? body) (:template body))]        
        (assoc res :body (apply-template handlebars tname body))
        res))))


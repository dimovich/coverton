(ns coverton.ed.db
  (:require [coverton.fonts :refer [default-font]]))


;;fixme move to higher function
(def default-db
  {:cover/image-url "assets/img/coverton.jpg"
   :cover/font      default-font})

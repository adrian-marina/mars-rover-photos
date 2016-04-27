(ns mars-rover-photos.storage
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn- img-name
  "Returns image name from url."
  [url]
  (last
   (string/split url  #"/")))

(defn- download-image
  "Downloads image at src to target."
  [src target]
  (let [file (io/file target)]
    (try
      (with-open [from (io/input-stream src)]
        (io/copy from file))
      target
      (catch java.io.IOException _))))

(defn- add-slash
  [^String path]
  (str path (when-not (.endsWith path "/") "/")))

(defn- prepend-target
  [target img-name]
  (str (add-slash target) img-name))

(defn- image-exists?
  [img]
  (.exists (io/as-file img)))

(defn- src->path
  [path src]
  (prepend-target path (img-name src)))

(defn download-images
  "Downloads images to target. First it filters out all images already downloaded.
  Returns a list of images succcesfully downloaded.

  imgs-src - [collection] a collection of img srcs
  target   - [string] path to a folder where images will be downloaded to
  "
  [imgs-src target]
  (let [src-target-path #(src->path target %)
        [existing to-download] ((juxt filter remove) #(-> % src-target-path image-exists?) imgs-src)
        downloaded (map #(download-image %1 %2)
                        to-download
                        (map src-target-path to-download))]
    (into downloaded (map src-target-path existing))))

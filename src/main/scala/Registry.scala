package reactive.config

import com.typesafe.config.Config
import scala.collection.mutable


trait Registry {
  def register(setting: Setting): Unit
}


object Registry {
  def noop = new Registry {
    def register(setting: Setting) = {}
  }
}


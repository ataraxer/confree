package reactive.config

import com.typesafe.config.Config
import scala.collection.mutable

trait ConfigAware { this: Cache =>
  def config: Config
  def apply(setting: Setting): setting.Result = apply(config, setting)
}


trait Cache {
  def apply(config: Config, setting: Setting): setting.Result
  def invalidate(setting: Setting): Unit
}


object Cache {
  def noop = new Cache {
    def apply(config: Config, setting: Setting) = setting(config)(this)
    def invalidate(setting: Setting): Unit = {}
  }
}


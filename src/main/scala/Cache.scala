package reactive.config

import com.typesafe.config.Config
import scala.collection.mutable


trait Cache {
  def apply(config: Config, setting: Setting): Setting.Dep[setting.Out]
  def invalidate(setting: Setting): Unit
}


object Cache {
  def noop = new Cache {
    def apply(config: Config, setting: Setting) = setting.parse(config)
    def invalidate(setting: Setting): Unit = {}
  }
}


class ConfigAwareCache(config: Config) {
  private[this] var cachedConfig = config
  private[this] val cache = mutable.Map.empty[Setting, Any]
  private[this] val dependencies = mutable.Map.empty[String, mutable.Set[Setting]]


  def apply(setting: Setting) = {
    if (cache contains setting) println("Cache hit") else println("Cache miss")
    val untypedResult = cache.getOrElseUpdate(setting, setting.parse(config))
    val result @ (dependsOn, _) = untypedResult.asInstanceOf[setting.Result]

    dependsOn foreach { key =>
      dependencies.getOrElseUpdate(key, mutable.Set.empty) += setting
    }

    result
  }


  def invalidate(setting: Setting): Unit = {
    cache.remove(setting)
  }


  def invalidateKey(key: String) = {
    for {
      settings <- dependencies.get(key)
      setting <- settings
    } invalidate(setting)
  }


  def mergeConfig(config: Config) = {
    import scala.collection.JavaConversions._
    val changedKeys = config.entrySet.map( _.getKey ).toSet
    println(f"Changed keys: $changedKeys")
    changedKeys.foreach(invalidateKey)
    cachedConfig = config withFallback cachedConfig
  }
}


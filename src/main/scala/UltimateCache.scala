package reactive.config

import com.typesafe.config.Config
import scala.collection.mutable


class UltimateCache(_config: Config) extends Cache with ConfigAware with Registry {
  private[this] var cachedConfig = _config
  private[this] val cache = mutable.Map.empty[Setting, Any]
  private[this] val dependencies = mutable.Map.empty[String, mutable.Set[Setting]]

  def config = cachedConfig
  def get(setting: Setting): setting.Out = setting.cached(cachedConfig)(this)._2


  def register(setting: Setting) = {
    println("Registered! " + apply(config, setting))
  }


  def apply(config: Config, setting: Setting) = {
    if (cache contains setting) println("Cache hit") else println("Cache miss")
    val untypedResult = cache.getOrElseUpdate(setting, setting(cachedConfig)(this))
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


  def recalculate(key: String) = {
    for {
      settings <- dependencies.get(key)
      setting <- settings
    } apply(null, setting)
  }


  def mergeConfig(config: Config) = {
    import scala.collection.JavaConversions._
    val changedKeys = config.entrySet.map( _.getKey ).toSet
    println(f"Changed keys: $changedKeys")
    changedKeys.foreach(invalidateKey)
    changedKeys.foreach(recalculate)
    cachedConfig = config withFallback cachedConfig
  }
}


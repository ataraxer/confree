package reactive.config

import com.typesafe.config.{Config, ConfigFactory, ConfigUtil}


class AppConfig(config: Config) {
  private implicit val cache = new UltimateCache(config)

  def put(key: String, value: String) = {
    val quotedKey = ConfigUtil.quoteString(key)
    val quotedValue = ConfigUtil.quoteString(value)
    val wrapped = ConfigFactory.parseString(f"{$quotedKey: $quotedValue}")
    cache.mergeConfig(wrapped)
  }

  object settings extends {
    implicit val registry: Registry = cache
  } with AppSettings {
    println("Finished")
    println()
  }

  def foobar = settings.foobar.get
  def foo = settings.foo.get
  def bar = settings.bar.get
}


trait AppSettings {
  implicit val registry: Registry

  val foo = Setting[String]("foo")
  val bar = Setting[Int]("bar")

  val foobar = for {
    foo <- foo
    bar <- bar
  } yield {
    f"$foo: $bar"
  }
}


package reactive.config

import com.typesafe.config.{Config, ConfigFactory, ConfigUtil}


class AppConfig(config: Config) {
  private implicit val cache = new ConfigAwareCache(config)

  def put(key: String, value: String) = {
    val quotedKey = ConfigUtil.quoteString(key)
    val quotedValue = ConfigUtil.quoteString(value)
    val wrapped = ConfigFactory.parseString(f"{$quotedKey: $quotedValue}")
    cache.mergeConfig(wrapped)
  }

  def get(setting: Setting): setting.Result = setting.cached

  object settings {
    val foo = Setting[String]("foo")
    val bar = Setting[Int]("bar")
    val foobar = for (foo <- foo; bar <- bar) yield f"$foo: $bar"

    val foobarbaz = for {
      foobar <- foobar
      bar <- bar
      baz <- Setting[Int]("baz")
    } yield {
      val value = bar + baz
      f"$foobar and $value"
    }
  }

  def foobarbaz = get(settings.foobarbaz)
  def foobar = get(settings.foobar)
  def foo = get(settings.foo)
  def bar = get(settings.bar)
}


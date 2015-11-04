package reactive.config

import com.typesafe.config.ConfigFactory


object Main extends App {
  val config = new AppConfig(ConfigFactory.load.getConfig("app"))

  println(config.foo)
  println(config.foo)
  println(config.foobarbaz)
  config.put("foo", "Wow!")
  println(config.foo)
  println(config.foobarbaz)
  config.put("baz", "1337")
  println(config.foo)
  println(config.foobarbaz)
}


package reactive.config

import com.typesafe.config.ConfigFactory


object Main extends App {
  val config = new AppConfig(ConfigFactory.load.getConfig("app"))

  println(config.foo)
  println(config.foobar)
  config.put("foo", "Wow!")
  println(config.bar)
  println(config.foobar)
  config.put("bar", "1337")
  println(config.foo)
  println(config.foobar)
}


package reactive.config

import com.typesafe.config._


trait Setting {
  type Out
  type Self = Setting.Aux[Out]

  def apply(config: Config): Out

  def map[U](f: Out => U): Setting.Aux[U] = {
    Setting(config => f(apply(config)))
  }

  def flatMap[U](f: Out => Setting.Aux[U]): Setting.Aux[U] = {
    Setting(config => f(apply(config))(config))
  }
}


object Setting {
  type Aux[T] = Setting { type Out = T }

  def apply[T](parse: Config => T): Aux[T] = {
    new Setting {
      type Out = T
      def apply(config: Config) = parse(config)
    }
  }

  def apply[T: Parser](name: String): Aux[T] = {
    val parser = implicitly[Parser[T]]
    Setting(parser.from(name) _)
  }
}


trait Parser[T] {
  def apply(config: Config, name: String): T
  def from(name: String)(config: Config): T = apply(config, name)

  def map[U](f: T => U): Parser[U] = {
    Parser { config => name => f(apply(config, name)) }
  }
}


object Parser {
  def apply[T](parse: Config => String => T) = {
    new Parser[T] {
      def apply(config: Config, name: String) = parse(config)(name)
    }
  }

  implicit val intParser = Parser { _.getInt }
  implicit val stringParser = Parser { _.getString }
}


class AppConfig(config: Config) {
  private val cache = mutable.Map.empty[Setting, Any]

  def get(setting: Setting): setting.Out = {
    val result = cache.getOrElseUpdate(setting, setting(config))
    result.asInstanceOf[setting.Out]
  }

  object settings {
    val foo = Setting[String]("foo")
    val bar = Setting[Int]("bar")
    val foobar = for (foo <- foo; bar <- bar) yield f"$foo: $bar"
  }

  def foobar = get(settings.foobar)
  def foo = get(settings.foo)
  def bar = get(settings.bar)
}


object Demo extends App {
  val config = new AppConfig(ConfigFactory.load.getConfig("app"))
  println(config.foo)
  println(config.bar)
  println(config.foobar)
}


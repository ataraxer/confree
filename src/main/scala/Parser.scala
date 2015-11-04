package reactive.config

import com.typesafe.config.Config


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


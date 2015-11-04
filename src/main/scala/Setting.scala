package reactive.config

import com.typesafe.config.Config


trait Setting {
  type Out
  type Result = Setting.Dep[Out]
  type Self = Setting.Aux[Out]

  def parse(config: Config): Result

  def apply(config: Config)(implicit cache: Cache = Cache.noop): Result = {
    cache(config, this)
  }

  def cached(implicit cache: ConfigAwareCache): Result = {
    cache(this)
  }

  def map[U](f: Out => U): Setting.Aux[U] = {
    Setting { config =>
      val (deps, output) = apply(config)
      deps -> f(output)
    }
  }

  def flatMap[U](f: Out => Setting.Aux[U]): Setting.Aux[U] = {
    Setting { config =>
      val (depsA, outputA) = apply(config)
      val next = f(outputA)
      val (depsB, outputB) = next(config)
      (depsA ++ depsB) -> outputB
    }
  }
}


object Setting {
  type Aux[T] = Setting { type Out = T }
  type Dep[T] = (Set[String], T)

  private def apply[T](f: Config => Dep[T]): Aux[T] = {
    new Setting {
      type Out = T
      def parse(config: Config) = f(config)
    }
  }

  def apply[T](name: String)(implicit parser: Parser[T]): Aux[T] = {
    Setting { config => Set(name) -> parser.from(name)(config) }
  }
}


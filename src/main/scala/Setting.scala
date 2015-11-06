package reactive.config

import com.typesafe.config.Config


trait Setting {
  type Out
  type Result = Setting.Dep[Out]
  type Self = Setting.Aux[Out]

  def apply(config: Config)(implicit cache: Cache = Cache.noop): Result

  def cached(config: Config)(implicit cache: Cache): Result = {
    cache(config, this)
  }

  def get(implicit cache: Cache with ConfigAware): Result = {
    cache(this)
  }


  def map[U](f: Out => U)
    (implicit registry: Registry = Registry.noop): Setting.Aux[U] = {

    Setting from { config => implicit cache =>
      val (deps, output) = cached(config)
      deps -> f(output)
    }
  }


  def flatMap[U](f: Out => Setting.Aux[U])
    (implicit registry: Registry = Registry.noop): Setting.Aux[U] = {

    Setting from { config => implicit cache =>
      val (depsA, outputA) = this.cached(config)
      val next = f(outputA)
      val (depsB, outputB) = next.cached(config)
      (depsA ++ depsB) -> outputB
    }
  }
}


object Setting {
  type Aux[T] = Setting { type Out = T }
  type Dep[T] = (Set[String], T)


  private def from[T](f: Config => Cache => Dep[T])
    (implicit registry: Registry = Registry.noop): Aux[T] = {

    val setting = new Setting {
      type Out = T
      def apply(config: Config)(implicit cache: Cache) = f(config)(cache)
    }

    registry.register(setting)
    setting
  }


  def apply[T](name: String)(
    implicit
    parser: Parser[T],
    registry: Registry = Registry.noop): Aux[T] = {

    val setting = Setting.from { config => _ =>
      Set(name) -> parser.from(name)(config)
    } (Registry.noop)

    registry.register(setting)
    setting
  }
}


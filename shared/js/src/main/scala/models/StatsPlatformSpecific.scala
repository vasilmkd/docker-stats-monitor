package models

import scala.scalajs.js

import cats.{ MonadError, Traverse }
import cats.implicits._

private[models] trait StatsPlatformSpecific {
  def parse[F[_]: MonadError[*[_], Throwable]](obj: js.Dynamic): F[Stats] =
    MonadError[F, Throwable].catchNonFatal {
      val data = obj.data.asInstanceOf[js.UndefOr[js.Array[js.Dynamic]]].get.map(ContainerData.parse[F](_)).toList
      Traverse[List].sequence(data).map(cds => Stats(cds.toSet))
    }.flatten
}

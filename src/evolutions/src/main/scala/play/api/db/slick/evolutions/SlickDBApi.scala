package play.api.db.slick.evolutions

import play.api.db.slick.SlickApi
import play.api.db.DBApi
import play.api.db.slick.evolutions.internal.DBApiAdapter

object SlickDBApi {
  def apply(slickApi: SlickApi): DBApi = new DBApiAdapter(slickApi)
}

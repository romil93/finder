package finder.http

import com.twitter.finatra.Controller
import finder.config.FinderConfigReader


class DatasetRouter extends Controller {
  val config = FinderConfigReader.load()
  get("/:dataset/search/:key") { request =>
    val dataset = request.routeParams("dataset")
    val identifier = request.routeParams("key")
    new DatasetController(config.dataset(dataset)).search(identifier).map(render.json)
  }

  get("/:dataset/get/:id/:timestamp") { request =>
    val dataset = request.routeParams("dataset")
    val identifier = request.routeParams("id")
    val timestamp = request.routeParams("timestamp").toLong
    log.info(s"Searching $dataset with $identifier at $timestamp")

    new DatasetController(config.dataset(dataset)).get(identifier, timestamp).map {
      case Some(result) => render.json(result)
      case None => render.notFound
    }
  }

  error { request =>
    request.error match {
      case Some(e: Exception) =>
        log.error(e, e.getMessage)
        render.status(500).plain(e.getMessage).toFuture
      case _ =>
        render.status(500).plain("Something went wrong!").toFuture
    }
  }


}

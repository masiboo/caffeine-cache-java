package nl.ing.api.contacting.conf.web.provider

import com.typesafe.scalalogging.LazyLogging
import jakarta.ws.rs.core.{Context, UriInfo}
import jakarta.ws.rs.ext.{Provider, WriterInterceptor, WriterInterceptorContext}

@Provider
class ToBeCompliantWithRidiculousTPAStandardsInterceptor extends WriterInterceptor with LazyLogging {

  @Context
  var uriInfo: UriInfo = _

  override def aroundWriteTo(ctx: WriterInterceptorContext): Unit = {
    if (uriInfo.getAbsolutePath.getRawPath.startsWith("/contacting-conf/blacklisted-items")) {
      ctx.getEntity match {
        case _: Set[_] | _: Seq[_] =>
          logger.info(s"Receiving calls for ${uriInfo.getAbsolutePath.getRawPath} " +
                        s"by :${ctx.getHeaders.getFirst("Referer")}. Transforming to wrapped Response")
          ctx.setEntity(WrappedEntity(ctx.getEntity))
        case _                     =>
        // no-op
      }
    }
    ctx.proceed()
  }
}

case class WrappedEntity(data: AnyRef)

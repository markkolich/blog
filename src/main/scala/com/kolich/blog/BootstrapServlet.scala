package com.kolich.blog

import com.typesafe.scalalogging.slf4j.Logging
import com.kolich.curacao.CuracaoDispatcherServlet
import javax.servlet.{ServletContext, ServletConfig}

final class BootstrapServlet extends CuracaoDispatcherServlet with Logging {

  override def myInit(config: ServletConfig, context: ServletContext) = {
    logger.info("Blog application service started.")
  }

  override def myDestroy() = {
    logger.info("Blog application service stopped.")
  }

}

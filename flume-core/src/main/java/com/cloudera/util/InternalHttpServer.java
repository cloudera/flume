package com.cloudera.util;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class InternalHttpServer {

  private static final Logger logger = LoggerFactory
      .getLogger(InternalHttpServer.class);

  private Server server;
  private File webappDir;

  public InternalHttpServer() {
    Connector connector;

    connector = new SelectChannelConnector();

    connector.setPort(12345);

    server = new Server();

    server.addConnector(connector);
  }

  protected void registerApplications() {
    logger.debug("Registering webapps in {}", webappDir);

    for (File entry : webappDir.listFiles()) {
      Handler handler;

      logger.debug("checking {}", entry);

      handler = new WebAppContext(entry.getPath(), "/" + entry.getName());

      server.addHandler(handler);
    }
  }

  public void start() {
    Preconditions.checkState(server != null, "Server can not be null");
    Preconditions.checkState(webappDir != null && webappDir.isDirectory(),
        "Webapp dir can not be null and must be a directory - " + webappDir);

    registerApplications();

    logger.info("Starting internal HTTP server");

    try {
      server.start();

      logger.info("Server started");
    } catch (Exception e) {
      logger.warn("Caught exception during HTTP server start.", e);
    }
  }

  public void stop() {
    if (server == null) {
      return;
    }

    logger.info("Stopping internal HTTP server");

    try {
      server.stop();
    } catch (Exception e) {
      logger.warn("Caught exception during HTTP server stop.", e);
    }
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  public File getWebappDir() {
    return webappDir;
  }

  public void setWebappDir(File webappDir) {
    this.webappDir = webappDir;
  }

}

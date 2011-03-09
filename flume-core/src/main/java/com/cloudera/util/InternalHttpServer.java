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
  private int port;
  private String bindAddress;

  public InternalHttpServer() {
    port = 0;
    bindAddress = "0.0.0.0";
  }

  public void initialize() {
    if (server == null) {

      server = new Server();
      Connector connector = new SelectChannelConnector();

      connector.setPort(port);
      connector.setHost(bindAddress);

      server.addConnector(connector);
    }
  }

  protected void registerApplications() {
    logger.debug("Registering webapps in {}", webappDir);

    for (File entry : webappDir.listFiles()) {
      String name;

      logger.debug("checking {}", entry);

      if (entry.isFile()) {
        int idx = entry.getName().indexOf(".war");

        if (idx > -1) {
          name = entry.getName().substring(0, idx);
        } else {
          continue;
        }
      } else {
        name = entry.getName();
      }

      logger.debug("creating context {} -> {}", name, entry);

      Handler handler = new WebAppContext(entry.getPath(), "/" + name);

      server.addHandler(handler);
    }
  }

  public void start() {
    Preconditions.checkState(webappDir != null && webappDir.isDirectory(),
        "Webapp dir can not be null and must be a directory - " + webappDir);

    initialize();
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

  @Override
  public String toString() {
    return "{ bindAddress:" + bindAddress + " webappDir:" + webappDir
        + " port:" + port + " server:" + server + " }";
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

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getBindAddress() {
    return bindAddress;
  }

  public void setBindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
  }

}

package com.myvanitys.api.common.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupLogger implements ApplicationListener<ApplicationStartedEvent> {

  private static final Logger log = LoggerFactory.getLogger(ApplicationStartupLogger.class);

  private final Environment env;

  @Value("${server.servlet.context-path:/}")
  private String contextPath;

  @Value("${server.port:8080}")
  private String serverPort;

  public ApplicationStartupLogger(Environment env) {
    this.env = env;
  }

  @Override
  public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }

    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("Could not determine host address", e);
    }

    // Railway environment variables
    String railwayStaticUrl = System.getenv("RAILWAY_STATIC_URL");
    String railwayPublicDomain = System.getenv("RAILWAY_PUBLIC_DOMAIN");
    String railwayService = System.getenv("RAILWAY_SERVICE_NAME");

    if (log.isInfoEnabled()) {
      log.info("""
              ----------------------------------------------------------
              \tApplication '{}' is RUNNING! Access URLs:
              \tLocal: \t\t{}://localhost:{}{}
              \tExternal: \t{}://{}:{}{}
              \tProfile(s): \t{}
              \tContext: \t{}
              \tHealth Endpoint: \t{}
              ----------------------------------------------------------""",
          env.getProperty("spring.application.name"),
          protocol,
          serverPort,
          contextPath,
          protocol,
          hostAddress,
          serverPort,
          contextPath,
          String.join(", ", env.getActiveProfiles()),
          contextPath,
          contextPath + "/actuator/health"
      );
    }

    String notAvailable = "Not available";
    log.info("""
            ----------------------------------------------------------
            \tRAILWAY INFORMATION:
            \tRailway Static URL: \t{}
            \tRailway Public Domain: \t{}
            \tRailway Service Name: \t{}
            \tPossible Full URL: \t{}{}/actuator/health
            ----------------------------------------------------------""",
        railwayStaticUrl != null ? railwayStaticUrl : notAvailable,
        railwayPublicDomain != null ? railwayPublicDomain : notAvailable,
        railwayService != null ? railwayService : notAvailable,
        railwayStaticUrl != null ? railwayStaticUrl : "https://[service].[project].railway.app",
        contextPath
    );
  }
}

package com.philosobyte.igniteblockedthreadsample;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteSpring;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Configuration
public class TestIgniteConfiguration {
    @Bean
    @SneakyThrows
    public Ignite serverIgnite0(ApplicationContext applicationContext) {
        IgniteConfiguration cfg = buildIgniteConfiguration();
        cfg.setIgniteInstanceName("server1");
        return IgniteSpring.start(cfg, applicationContext);
    }

    @Bean
    @SneakyThrows
    public Ignite serverIgnite1(ApplicationContext applicationContext) {
        IgniteConfiguration cfg = buildIgniteConfiguration();
        cfg.setIgniteInstanceName("server2");
        return IgniteSpring.start(cfg, applicationContext);
    }

    @Bean
    @SneakyThrows
    public Ignite serverIgnite2(ApplicationContext applicationContext) {
        IgniteConfiguration cfg = buildIgniteConfiguration();
        cfg.setIgniteInstanceName("server3");
        return IgniteSpring.start(cfg, applicationContext);
    }

    @Bean
    @SneakyThrows
    public Ignite clientIgnite(ApplicationContext applicationContext) {
        IgniteConfiguration cfg = buildIgniteConfiguration();
        cfg.setIgniteInstanceName("client");
        cfg.setClientMode(true);
        return IgniteSpring.start(cfg, applicationContext);
    }

    @SneakyThrows
    private IgniteConfiguration buildIgniteConfiguration() {
        Path tmpDir = Files.createTempDirectory("ignite-work");
        File tmpDirFile = tmpDir.toFile();
        tmpDirFile.deleteOnExit();

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(List.of(
            "127.0.0.1:47500..47509"
        ));

        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setLocalAddress("127.0.0.1");

        TcpCommunicationSpi tcpCommunicationSpi = new TcpCommunicationSpi();

        IgniteConfiguration cfg = new IgniteConfiguration()
            .setGridLogger(new Slf4jLogger())
            .setWorkDirectory(tmpDirFile.getAbsolutePath())
            .setDiscoverySpi(tcpDiscoverySpi)
            .setCommunicationSpi(tcpCommunicationSpi);

        // Changing thread pool sizes did not appear to make a difference.
        // int commonThreadPoolSize = 16;
        // cfg.setBuildIndexThreadPoolSize(commonThreadPoolSize);
        // cfg.setManagementThreadPoolSize(commonThreadPoolSize);
        // cfg.setPublicThreadPoolSize(commonThreadPoolSize);
        // cfg.setQueryThreadPoolSize(commonThreadPoolSize);
        // cfg.setRebalanceThreadPoolSize(commonThreadPoolSize);
        // cfg.setServiceThreadPoolSize(commonThreadPoolSize);
        // cfg.setSnapshotThreadPoolSize(commonThreadPoolSize);
        // cfg.setSystemThreadPoolSize(commonThreadPoolSize);
        // cfg.setDataStreamerThreadPoolSize(commonThreadPoolSize);
        // cfg.setUtilityCachePoolSize(commonThreadPoolSize);
        // cfg.setPeerClassLoadingThreadPoolSize(commonThreadPoolSize);

        log.info("BuildIndexThreadPoolSize: {}", cfg.getBuildIndexThreadPoolSize());
        log.info("ManagementThreadPoolSize: {}", cfg.getManagementThreadPoolSize());
        log.info("PublicThreadPoolSize: {}", cfg.getPublicThreadPoolSize());
        log.info("QueryThreadPoolSize: {}", cfg.getQueryThreadPoolSize());
        log.info("RebalanceThreadPoolSize: {}", cfg.getRebalanceThreadPoolSize());
        log.info("ServiceThreadPoolSize: {}", cfg.getServiceThreadPoolSize());
        log.info("SnapshotThreadPoolSize: {}", cfg.getSnapshotThreadPoolSize());
        log.info("SystemThreadPoolSize: {}", cfg.getSystemThreadPoolSize());
        log.info("DataStreamerThreadPoolSize: {}", cfg.getDataStreamerThreadPoolSize());
        log.info("UtilityCacheThreadPoolSize: {}", cfg.getUtilityCacheThreadPoolSize());
        log.info("PeerClassLoadingThreadPoolSize: {}", cfg.getPeerClassLoadingThreadPoolSize());

        return cfg;
    }
}

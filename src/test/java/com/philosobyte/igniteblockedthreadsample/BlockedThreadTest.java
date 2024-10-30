package com.philosobyte.igniteblockedthreadsample;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@SpringBootTest(classes = {TestIgniteConfiguration.class, BlockedThreadTest.Config.class})
public class BlockedThreadTest {
    @Configuration
    static class Config {
        @Bean
        @Qualifier("outerCache")
        public IgniteCache<String, String> outerCache(Ignite clientIgnite) {
            CacheConfiguration<String, String> cfg = new CacheConfiguration<>("outer");
            cfg.setCacheMode(CacheMode.PARTITIONED);
            cfg.setBackups(0);
            cfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
            return clientIgnite.getOrCreateCache(cfg);
        }

        @Bean
        @Qualifier("innerCache")
        public IgniteCache<String, String> innerCache(Ignite clientIgnite) {
            CacheConfiguration<String, String> cfg = new CacheConfiguration<>("inner");
            cfg.setCacheMode(CacheMode.PARTITIONED);
            cfg.setBackups(0);
            cfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
            return clientIgnite.getOrCreateCache(cfg);
        }

        @Bean
        public OuterEntryProcessor outerEntryProcessor(InnerEntryProcessor innerEntryProcessor) {
            return new OuterEntryProcessor(innerEntryProcessor);
        }

        @Bean
        public InnerEntryProcessor innerEntryProcessor() {
            return new InnerEntryProcessor();
        }
    }

    static class InnerEntryProcessor implements CacheEntryProcessor<String, String, String> {
        @Override
        public String process(MutableEntry<String, String> mutableEntry, Object... objects) throws EntryProcessorException {
            String innerKey = mutableEntry.getKey();
            log.info("Entered InnerEntryProcessor with inner key: {}", innerKey);
            mutableEntry.setValue(innerKey);
            log.info("Exiting InnerEntryProcessor with inner key: {}", innerKey);
            return innerKey;
        }
    }

    @RequiredArgsConstructor
    static class OuterEntryProcessor implements CacheEntryProcessor<String, String, String> {
        private final InnerEntryProcessor innerEntryProcessor;
        private IgniteCache<String, String> inner;

        @IgniteInstanceResource
        public void setIgnite(Ignite ignite) {
            this.inner = ignite.cache("inner");
        }


        @Override
        @SneakyThrows
        public String process(MutableEntry<String, String> mutableEntry, Object... objects) throws EntryProcessorException {
            String outerKey = mutableEntry.getKey();
            List<String> innerKeys = (ArrayList<String>) objects[0];
            // sleeping gives the test runner time to finish invoking all outer entry processors before
            // the outer entry processors begin doing work
            log.info("Entered OuterEntryProcessor with key {}. Sleeping before further actions...", outerKey);
            Thread.sleep(100L);

            List<IgniteFuture<String>> futures = innerKeys.stream().map(innerKey -> {
                log.info("Invoking with inner key: {}", innerKey);
                var future = inner.invokeAsync(innerKey, innerEntryProcessor);
                log.info("Invoked with inner key: {}", innerKey);
                return future;
            }).toList();

            for (int i = 0; i < futures.size(); i++) {
                log.info("OuterEntryProcessor with key {} waiting for inner future {}", outerKey, i);
                futures.get(i).get();
            }
            log.info("OuterEntryProcessor with key {} done waiting for all inner futures", outerKey);

            mutableEntry.setValue(outerKey);
            return outerKey;
        }
    }

    @Autowired
    @Qualifier("outerCache")
    private IgniteCache<String, String> outerCache;

    @Autowired
    private OuterEntryProcessor outerEntryProcessor;

    @Test
    public void test() {
        log.info("Starting test");

        int numOuterKeys = 2;
        int numInnerKeys = 2;

        // populate a map which looks like {
        //     "outer0": [ "outer0-inner0", "outer0-inner1" ],
        //     "outer1": [ "outer1-inner0", "outer1-inner1" ]
        // }
        Map<String, List<String>> outerKeysToInnerKeys = new TreeMap<>();
        for (int i = 0; i < numOuterKeys; i++) {
            String outerKey = "outer" + i;

            List<String> innerKeys = new ArrayList<>(numInnerKeys);
            for (int j = 0; j < numInnerKeys; j++) {
                innerKeys.add(outerKey + "-inner" + j);
            }
            outerKeysToInnerKeys.put(outerKey, innerKeys);
        }

        List<IgniteFuture<String>> futures = outerKeysToInnerKeys.keySet().stream().map(outerKey -> {
            List<String> innerKeys = outerKeysToInnerKeys.get(outerKey);
            log.info("Invoking OuterEntryProcessor with key {}", outerKey);
            var future = outerCache.invokeAsync(outerKey, outerEntryProcessor, innerKeys);
            log.info("Invoked OuterEntryProcessor with key {}", outerKey);
            return future;
        }).toList();
        log.info("Done invoking OuterEntryProcessors. Waiting for them to complete...");

        for (int i = 0; i < futures.size(); i++) {
            log.info("Test Runner waiting for outer future {}", i);
            futures.get(i).get();
        }
        log.info("Done waiting for all outer futures");
    }
}

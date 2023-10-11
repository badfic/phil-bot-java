package com.badfic.philbot.service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.random.RandomGenerator;
import org.springframework.stereotype.Service;

@Service
public class RandomNumberService implements RandomGenerator {
    private final AtomicLong seed;

    public RandomNumberService() {
        this.seed = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public long nextLong() {
        // SplitMix64 https://prng.di.unimi.it/splitmix64.c
        long z = seed.updateAndGet(s -> s + 0x9e3779b97f4a7c15L);
        z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >> 31);
    }

}

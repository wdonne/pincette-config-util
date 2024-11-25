package net.pincette.config;

import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static net.pincette.util.Util.tryToGetSilent;

import com.typesafe.config.Config;
import java.io.File;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.pincette.util.State;

/**
 * Utilities for Typesafe config.
 *
 * @author Werner Donné
 */
public class Util {
  private Util() {}

  private static Optional<File> configFile(final Config config) {
    return ofNullable(config.origin().filename()).map(File::new);
  }

  public static Supplier<Config> getConfig(final Supplier<Config> load) {
    final State<Config> config = new State<>();
    final State<Instant> loaded = new State<>();

    return () -> {
      if (config.get() == null || hasChanged(config.get(), loaded.get())) {
        config.set(load.get());
        loaded.set(now());
      }

      return config.get();
    };
  }

  public static <T> Optional<T> configValue(final Function<String, T> fn, final String path) {
    return tryToGetSilent(() -> fn.apply(path));
  }

  private static boolean hasChanged(final Config config, final Instant loaded) {
    return configFile(config)
        .map(File::lastModified)
        .map(modified -> modified > loaded.toEpochMilli())
        .orElse(false);
  }
}

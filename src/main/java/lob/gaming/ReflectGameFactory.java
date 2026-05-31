package lob.gaming;

import lob.LotsOfBallsException;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Factory that uses reflection to discover and instantiate {@link GameAnimation}
 * subclasses found in a given package.
 *
 * <p>Design pattern: <b>Factory</b> + <b>Reflection</b>.
 *
 * <p>Implementation notes (per the project FAQ):
 * <ul>
 *   <li>{@link #LOADER} is a single, static {@link ClassLoader} instance shared by
 *       all factory operations.</li>
 *   <li>{@link #collectGameClassesFromPackage(String)} obtains the directory of the
 *       package via {@code LOADER.getResource(...)}, then iterates over the files
 *       using {@link Files#list(Path)} and converts each path back to a binary
 *       class name with {@link #getClassName(Path)}.</li>
 *   <li>{@link #createInstance(Class)} uses {@code clazz.getDeclaredConstructor().newInstance()}
 *       so the no-arg constructor of each game is honoured.</li>
 * </ul>
 *
 * @param <T> the type of game produced (a subtype of {@link GameAnimation}).
 */
public class ReflectGameFactory<T extends GameAnimation> {

    /** Shared class loader used to resolve resources and load classes. */
    private static final ClassLoader LOADER = ReflectGameFactory.class.getClassLoader();

    /** Discovered games keyed by their {@code GAME_NAME} static field value. */
    private final Map<String, Class<T>> gameClasses = new HashMap<>();

    /**
     * Default constructor — auto-discovers every game found in
     * {@code lob.gaming.games}.
     *
     * @throws LotsOfBallsException if the games package cannot be located.
     */
    public ReflectGameFactory() throws LotsOfBallsException {
        collectGameClassesFromPackage("lob.gaming.games");
    }

    // ---------------------------------------------------------------- discovery

    /**
     * Discovers every {@link GameAnimation} subclass found in {@code packageName}
     * across <b>every</b> classpath root and registers it under its
     * {@code GAME_NAME}.
     *
     * <p>The original implementation used {@link ClassLoader#getResource(String)},
     * which returns only the <i>first</i> classpath entry that contains the
     * package directory. That was fine in production, but it broke the unit
     * tests: when the test classes live on a separate output root (e.g.
     * {@code out/test/classes}) from the production classes
     * ({@code out/production/classes}), only one of the two was scanned, so
     * either the production games <i>or</i> the {@code MockGame} were missed
     * &mdash; depending on classpath order. Switching to
     * {@link ClassLoader#getResources(String)} (plural) walks every matching
     * directory, so the factory now sees every game regardless of whether it
     * was compiled into production or test output.
     *
     * @param packageName fully-qualified package name (e.g.
     *                    {@code "lob.gaming.games"}).
     * @throws LotsOfBallsException if the package cannot be found in any
     *                              classpath entry, or if any directory cannot
     *                              be enumerated.
     */
    public void collectGameClassesFromPackage(String packageName) throws LotsOfBallsException {
        String resourcePath = packageName.replace('.', '/');
        Enumeration<URL> resources;
        try {
            resources = LOADER.getResources(resourcePath);
        } catch (Exception e) {
            throw new LotsOfBallsException("Cannot enumerate package " + packageName, e);
        }
        if (!resources.hasMoreElements())
            throw new LotsOfBallsException("Package not found: " + packageName);

        try {
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                Path dir = Paths.get(resource.toURI());
                try (Stream<Path> stream = Files.list(dir)) {
                    stream
                        .filter(p -> p.getFileName().toString().endsWith(".class"))
                        .filter(p -> !p.getFileName().toString().contains("$"))
                        .forEach(p -> {
                            String className = packageName + "."
                                    + p.getFileName().toString().replace(".class", "");
                            loadGameClass(className);
                        });
                }
            }
        } catch (Exception e) {
            throw new LotsOfBallsException("Cannot list package " + packageName, e);
        }
    }

    // ---------------------------------------------------------------- loading

    /**
     * Loads a class by binary name and registers it as a game if it extends
     * {@link GameAnimation} and exposes a {@code GAME_NAME} field.
     *
     * @return the loaded class, or {@code null} if it cannot be loaded or is not
     *         a {@link GameAnimation}.
     */
    @SuppressWarnings("unchecked")
    public Class<T> loadGameClass(String className) {
        try {
            Class<?> raw = LOADER.loadClass(className);
            if (!GameAnimation.class.isAssignableFrom(raw)) return null;
            Class<T> clazz = (Class<T>) raw;
            String name = getGameName(clazz);
            if (name != null) gameClasses.put(name, clazz);
            return clazz;
        } catch (Exception e) {
            return null;
        }
    }

    /** Convenience overload for {@link Path}-based loading. */
    public Class<T> loadGameClass(Path path) {
        return loadGameClass(getClassName(path));
    }

    /** Manual registration — handy for tests or hand-written extensions. */
    public void loadGameClass(Class<T> clazz, String name) {
        gameClasses.put(name, clazz);
    }

    // ---------------------------------------------------------------- naming

    /**
     * Reads the {@code GAME_NAME} static field from a game class.
     *
     * @return the game name, or {@code null} if the field is missing or
     *         inaccessible.
     */
    public String getGameName(Class<T> clazz) {
        try {
            return (String) clazz.getField("GAME_NAME").get(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a filesystem path into a binary class name.
     * Example: {@code lob/gaming/games/CannonPractice.class} →
     *          {@code lob.gaming.games.CannonPractice}.
     */
    public String getClassName(Path path) {
        String s = path.toString().replace(File.separatorChar, '/').replace('/', '.');
        if (s.endsWith(".class")) s = s.substring(0, s.length() - 6);
        return s;
    }

    // ---------------------------------------------------------------- factory

    /** All discovered game names. */
    public Set<String> getAvailableGameNames() { return gameClasses.keySet(); }

    /**
     * Instantiates the game registered under {@code name}.
     *
     * @throws LotsOfBallsException if no game is registered under that name.
     */
    public T getGame(String name) throws LotsOfBallsException {
        Class<T> clazz = gameClasses.get(name);
        if (clazz == null) throw new LotsOfBallsException("Game not found: " + name);
        return createInstance(clazz);
    }

    /**
     * Creates a new instance of the given game class via its no-arg constructor.
     *
     * @throws LotsOfBallsException if instantiation fails.
     */
    public T createInstance(Class<T> clazz) throws LotsOfBallsException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new LotsOfBallsException("Cannot instantiate " + clazz.getName(), e);
        }
    }
}

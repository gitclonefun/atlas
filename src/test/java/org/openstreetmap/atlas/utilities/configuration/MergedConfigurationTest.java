package org.openstreetmap.atlas.utilities.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.StringInputStream;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Test Cases for MergedConfiguration implementations
 *
 * @author brian_l_davis
 * @author jklamer
 * @author cameron_frenette
 */
public class MergedConfigurationTest
{
    private static final String BASE_CONFIGURATION_JSON = "org/openstreetmap/atlas/utilities/configuration/application.json";
    private static final String BASE_CONFIGURATION_YAML = "org/openstreetmap/atlas/utilities/configuration/application.json";

    private static final String KEYWORD_OVERRIDDEN_CONFIGURATION_JSON = "org/openstreetmap/atlas/utilities/configuration/keywordOverridingApplication.json";
    private static final String KEYWORD_OVERRIDDEN_CONFIGURATION_YAML = "org/openstreetmap/atlas/utilities/configuration/keywordOverridingApplication.yml";

    private static final String KEYWORD_OVERRIDDEN_DEV_CONFIGURATION_JSON = "org/openstreetmap/atlas/utilities/configuration/developmentOverriding.json";
    private static final String KEYWORD_OVERRIDDEN_DEV_CONFIGURATION_YAML = "org/openstreetmap/atlas/utilities/configuration/developmentOverriding.yml";

    private static final String OVERRIDE_CONFIGURATION_JSON = "org/openstreetmap/atlas/utilities/configuration/development.json";
    private static final String OVERRIDE_CONFIGURATION_YAML = "org/openstreetmap/atlas/utilities/configuration/development.yml";

    private static final String PARTIAL_CONFIGURATION_JSON = "org/openstreetmap/atlas/utilities/configuration/feature.json";
    private static final String PARTIAL_CONFIGURATION_YAML = "org/openstreetmap/atlas/utilities/configuration/feature.yml";

    @Test
    public void testConfigurationDataKeySetAllJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_JSON);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION_JSON))
        {
            testConfigurationDataKeySet(base, override);
        }
    }

    @Test
    public void testConfigurationDataKeySetAllYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_YAML);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION_YAML))
        {
            testConfigurationDataKeySet(base, override);
        }
    }

    private void testConfigurationDataKeySet(final InputStream base, final InputStream override)
    {

        final Configuration configuration = new MergedConfiguration(new InputStreamResource(base),
                new InputStreamResource(override));

        final Set<String> compareTo = new HashSet<>();
        compareTo.add("feature");
        Assert.assertEquals(configuration.configurationDataKeySet(), compareTo);

    }

    @Test
    public void testDotFormat() throws IOException
    {
        final String expandedSource = "{\"a\":{\"b\":{\"c\":0,\"d\":1}}}";
        final String expandedOverrideSource = "{\"a\":{\"b\":{\"c\":2}}}";
        final String flattenedSource = "{\"a.b.c\": 0, \"a.b.d\": 1}";
        final String flattenedOverrideSource = "{\"a.b.c\": 2}";

        validateSame(expandedSource, expandedOverrideSource, flattenedSource,
                flattenedOverrideSource);
        validateSame(expandedSource, flattenedOverrideSource, flattenedSource,
                expandedOverrideSource);
    }

    @Test
    public void testLayeredJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_JSON);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION_JSON))
        {
            testLayered(base, override);
        }
    }

    @Test
    public void testLayeredYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_YAML);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION_YAML))
        {
            testLayered(base, override);
        }
    }

    @Test
    public void testLayeredYamlOnJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_JSON);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION_YAML))
        {
            testLayered(base, override);
        }
    }

    @Test
    public void testLayeredJsonOnYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_YAML);
                InputStream override = loader.getResourceAsStream(OVERRIDE_CONFIGURATION_JSON))
        {
            testLayered(base, override);
        }
    }

    private void testLayered(final InputStream base, final InputStream override)
    {

        final Configuration configuration = new MergedConfiguration(new InputStreamResource(base),
                new InputStreamResource(override));

        final String keyword = "ABC";
        final String key = String.format("feature.%s.range", keyword);

        final Optional<Map<String, Double>> rangeOption = configuration.get(key).valueOption();

        Assert.assertTrue(rangeOption.isPresent());
        rangeOption.ifPresent(range ->
        {
            Assert.assertEquals(5.0, range.get("min"), 0.1);
            Assert.assertEquals(30.0, range.get("max"), 0.1);
        });

        final String baseKeyword = "ABC";
        final String baseKey = String.format("feature.%s.range", baseKeyword);
        final Double max = configuration.get(baseKey + ".min", Double.NaN).value();
        Assert.assertNotEquals(Double.NaN, max);

        Assert.assertFalse(configuration.get("foo").valueOption().isPresent());
    }

    @Test
    public void testMergedOverriddenConfigurationsJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream keywordOverriddenBaseConfiguration = loader
                .getResourceAsStream(KEYWORD_OVERRIDDEN_CONFIGURATION_JSON);
                InputStream developmentConfiguration = loader
                        .getResourceAsStream(KEYWORD_OVERRIDDEN_DEV_CONFIGURATION_JSON))
        {
            testMergedOverriddenConfigurations(keywordOverriddenBaseConfiguration,
                    developmentConfiguration);
        }
    }

    @Test
    public void testMergedOverriddenConfigurationsYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream keywordOverriddenBaseConfiguration = loader
                .getResourceAsStream(KEYWORD_OVERRIDDEN_CONFIGURATION_YAML);
                InputStream developmentConfiguration = loader
                        .getResourceAsStream(KEYWORD_OVERRIDDEN_DEV_CONFIGURATION_YAML))
        {
            testMergedOverriddenConfigurations(keywordOverriddenBaseConfiguration,
                    developmentConfiguration);
        }
    }

    @Test
    public void testMergedOverriddenConfigurationsYamlOnJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream keywordOverriddenBaseConfiguration = loader
                .getResourceAsStream(KEYWORD_OVERRIDDEN_CONFIGURATION_JSON);
                InputStream developmentConfiguration = loader
                        .getResourceAsStream(KEYWORD_OVERRIDDEN_DEV_CONFIGURATION_YAML))
        {
            testMergedOverriddenConfigurations(keywordOverriddenBaseConfiguration,
                    developmentConfiguration);
        }
    }

    private void testMergedOverriddenConfigurations(
            final InputStream keywordOverriddenBaseConfiguration,
            final InputStream developmentConfiguration) throws IOException
    {

        final Configuration configuration = new MergedConfiguration(
                new InputStreamResource(keywordOverriddenBaseConfiguration),
                new InputStreamResource(developmentConfiguration));

        final String keyword1 = "ABC";
        final String keyword2 = "XYZ";
        final String keyword3 = "ZZZ";
        final Configuration configuration1 = configuration.configurationForKeyword(keyword1);
        final Configuration configuration2 = configuration.configurationForKeyword(keyword2);

        final String minKey = "feature.range.min";
        final String maxKey = "feature.range.max";

        // Assert that configuration not changed for not overriding keywords.
        Assert.assertEquals(configuration, configuration.configurationForKeyword(keyword3));
        // value derived from default of higher layer development configuration instead of
        // keyword specific view of base configuration
        Assert.assertEquals(Angle.degrees(35.0),
                configuration2.get(maxKey, Angle::degrees).value());
        // value derived from keyword specific view of development configuration
        Assert.assertEquals(Angle.degrees(70.0),
                configuration1.get(maxKey, Angle::degrees).value());
        // value derived from default of base configuration
        Assert.assertEquals(Angle.degrees(10.0),
                configuration1.get(minKey, Angle::degrees).value());
        Assert.assertEquals(Angle.degrees(10.0),
                configuration2.get(minKey, Angle::degrees).value());

    }

    @Test
    public void testPartialJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_JSON);
                InputStream partial = loader.getResourceAsStream(PARTIAL_CONFIGURATION_JSON))
        {
            testPartial(base, partial);
        }
    }

    @Test
    public void testPartialYaml() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_YAML);
                InputStream partial = loader.getResourceAsStream(PARTIAL_CONFIGURATION_YAML))
        {
            testPartial(base, partial);
        }
    }

    @Test
    public void testPartialYamlOnJson() throws IOException
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream base = loader.getResourceAsStream(BASE_CONFIGURATION_JSON);
                InputStream partial = loader.getResourceAsStream(PARTIAL_CONFIGURATION_YAML))
        {
            testPartial(base, partial);
        }
    }

    private void testPartial(final InputStream base, final InputStream partial)
    {
        final Configuration configuration = new MergedConfiguration(new InputStreamResource(base),
                new InputStreamResource(partial));

        final String keyword = "XYZ";
        final String key = String.format("feature.%s.range", keyword);

        final Optional<Map<String, Double>> rangeOption = configuration.get(key).valueOption();

        Assert.assertTrue(rangeOption.isPresent());
        rangeOption.ifPresent(range ->
        {
            Assert.assertEquals(20.0, range.get("min"), 0.1);
            Assert.assertEquals(40.0, range.get("max"), 0.1);
        });
        Assert.assertFalse(configuration.get("foo").valueOption().isPresent());

    }

    private void validateSame(final String leftBase, final String leftOverride,
            final String rightBase, final String rightOverride) throws IOException
    {
        try (InputStream leftBaseStream = new StringInputStream(leftBase);
                InputStream leftOverrideStream = new StringInputStream(leftOverride);
                InputStream rightBaseStream = new StringInputStream(rightBase);
                InputStream rightOverrideStream = new StringInputStream(rightOverride))
        {
            final Configuration left = new MergedConfiguration(
                    new InputStreamResource(leftBaseStream),
                    new InputStreamResource(leftOverrideStream));
            final Configuration right = new MergedConfiguration(
                    new InputStreamResource(rightBaseStream),
                    new InputStreamResource(rightOverrideStream));

            Assert.assertEquals(2L, (long) left.get("a.b.c").value());
            Assert.assertEquals(1L, (long) left.get("a.b.d").value());
            Assert.assertEquals((long) left.get("a.b.c").value(),
                    (long) right.get("a.b.c").value());
            Assert.assertEquals((long) left.get("a.b.d").value(),
                    (long) right.get("a.b.d").value());

            final Map<String, Object> leftMap = left.get("a.b").value();
            final Map<String, Object> rightMap = right.get("a.b").value();

            Assert.assertEquals(2L, leftMap.get("c"));
            Assert.assertEquals(2L, rightMap.get("c"));
            Assert.assertEquals(1L, leftMap.get("d"));
            Assert.assertEquals(1L, rightMap.get("d"));
        }
    }
}

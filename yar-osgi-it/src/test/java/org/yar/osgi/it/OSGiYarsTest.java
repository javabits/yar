/*
 * Copyright 2013 Romain Gilles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yar.osgi.it;

import com.google.inject.Injector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.yar.guice.RegistryModule;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.yar.guice.osgi.OSGiYars.newInjector;

/**
 * TODO comment
 * Date: 3/13/13
 * Time: 6:40 PM
 *
 * @author Romain Gilles
 */
@RunWith(PaxExam.class)
public class OSGiYarsTest {
    @Inject
    BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        return options(
                mavenBundle().groupId("org.yar").artifactId("yar-guice-osgi").versionAsInProject()
                , mavenBundle().groupId("org.yar").artifactId("yar-guice").versionAsInProject()
                , mavenBundle().groupId("org.yar").artifactId("yar-api").versionAsInProject()
                , mavenBundle().groupId("com.google.inject").artifactId("guice").versionAsInProject()
                , mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.aopalliance").version("1.0_5")
                , mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.javax-inject").version("1_2")
                , mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject()
                , junitBundles()
        );
    }

    @Test
    public void testNewInjector() {
        Injector injector = newInjector(bundleContext, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(IOSGiYarsTest.class).to(OSGiYarsTestImpl.class);
            }
        });
        assertThat(injector, is(not(nullValue())));
    }

    static interface IOSGiYarsTest {

    }

    static class OSGiYarsTestImpl implements IOSGiYarsTest {
    }
}

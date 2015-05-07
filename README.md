Yar
===========

Yar stands for: Yet Another Registry

This project is a registry implementation based on OSGi service registry.
It aims to provide dynamic registry feature as OSGi does. The main difference is on the object / service lifecycle.
In OSFi service in registry are instance object except if you are using the ServiceFactory facility. But it is quite
complex to have a new instance for each call. There you come quickly with shared mutability. I like the Guice approach
and in an enterprise environment I prefer "spend time" on object creation than on thread-safety. This is where this
project try to introduce a registry where you can store Guava Supplier (i.e. Jax-inject Provider). By this way it is up
to the service provider to decide the lifecycle of its object.
This project aims to join Guice object lifecycle and OSGi.

Registry
--------

The registry is design around the ```Id<T>``` and the ```Supplier<T>``` (c.f. guava) where Id defines the ```Type```
of the Supplier and optionally a qualifying annotation.

The main driver is the generic type and then the qualifying annotation can be used to refine the filtering.
If you provide an ```Id<T>``` without annotation then all the ```Supplier<T>``` will be returned even if their
associated key in the registry are qualifyed with an annotation.

More formally imagine that you have tow ```DataSource``` registered in the registry one for the DB1 and the other one
for the DB2 using ```yar-guice``` capability:

```java
public class DataSourceRegistrationModule extends AbstractRegistryModule {
    public void configureRegistry() {
        register(Key.get(DataSource.class, named("DB1"))).to(DB1DataSource.class);
        register(Key.get(DataSource.class, named("DB2"))).to(DB2DataSource.class);
    }
}
```

Then following query on the registry will produce:

```java
    public void testRetriveAllDataSourcesByClass(Registry registry) {
        List<DataSource> dataSources = registry.getAll(DataSource.class);
        assertThat(dataSources.size(), is(2));
    }

    public void testRetriveAllDataSourcesId(Registry registry) {
        List<DataSource> dataSources = registry.getAll(GuiceId.of(DataSource.class));
        assertThat(dataSources.size(), is(2));
    }

    public void testRetriveAllDataSourcesIdAndName(Registry registry) {
        List<DataSource> dataSources = registry.getAll(GuiceId.of(DataSource.class, named("DB2")));
        assertThat(dataSources.size(), is(1));
    }
```

### Implementation detail
All modifications to the registry are done by a single thread (reactor like pattern) to simplify the implementation.
They are queued by the registry mutation methods and them consumed by the mutation unique thread.


Guice Integration
-----------------

This project provide an extension of the Guice EDSL to tightly integrate the registry features to Guice.
You can register or pull Suppliers to the registry as follow:

```java
new RegistryModule() {
    @Override
    protected void configureRegistry() {
        //bind an interface from the registry
        bind(MyInterface.class).toRegistry();
        //register an implementation to the registry
        register(MyInterface2.class).to(MyImpl2.class);
        // listen to a specific service implementations
        bindRegistryListener( new AbstractMatcher<Key<Hello>>() {...}, , new RegistryListener<Hello>() {..});
        // listen to a wildcard of generic service implementations
        bindRegistryListener( new AbstractMatcher<Key<MyGenericInterface<?>>>() {...}, , new RegistryListener<MyGenericInterface<?>>() {..});
    }
}
```

As you can see you just have to create a registry module and then leverage on the Guice's EDSL extension.
The `AbstractRegistryModule` javadoc provide a complete description on the capability added to the Guice EDSL.

OSGi Guice Integration
----------------------

The main target of this project is to replace the original OSGi registry by providing a Type-safe registry and where
the lifecycle of the component is not by default a singleton or a per-bundle client singleton.
In our registry we strongly advice you to use the instance guice's scope. This is the default guice scope and by
transitivity the Yar default scope.

This project provide a bundle with an activator that will create and register the Guice registry into the OSGi registry.
This component will take care of your bundle life-cycle to cleanup the registry and avoid any memory leaks
as ClassLoader leak.

Release the project
-------------------
Run the following command:

```
mvn release:prepare -DautoVersionSubmodules=true -DscmCommentPrefix="[maven-release-plugin] #XX: " -DreleaseVersion=2.0.0.MX -DdevelopmentVersion=2.0-SNAPSHOT
```
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
        assertThat(dataSources.get(0)..getClass().<annotations(Named.class)>.value(), is("DB2"));
    }

```

### Implementation detail
All modifications to the registry are done by a single thread to simplify the implementation. They are queued by
the registry mutation methods and them consumed by the mutation unique thread.

Guice Integration
-----------------

TODO

OSGi Guice Integration
----------------

TODO
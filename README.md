Yar
===========

YAR stands for: Yet Another Registry

This project is a registry implementation based on OSGi service registry.
It aims to provide dynamic registry feature as OSGi does. The main difference is on the object / service lifecycle.
In OSFi service in registry are instance object except if you are using the ServiceFactory facility. But it is quite
complex to have a new instance for each call. There you come quickly with shared mutability. I like the Guice approach
and in an enterprise environment I prefer "spend time" on object creation than on thread-safety. This is where this
project try to introduce a registry where you can store Guava Supplier (i.e. Jax-inject Provider). By this way it is up
to the service provider to decide the lifecycle of its object.
This project aims to join Guice object lifecycle and OSGi.

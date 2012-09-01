---
layout: layout
---
<!---Licensed by Andrea Parodi under http://creativecommons.org/licenses/by-sa/3.0/deed.it-->

#Getting started


###1) Install the module

Thanks to the modules engine of Vert.x, there is no need to install anything to use
this module, just [deploy a module][1] giving it a name of "eban.alchemy-persistor-v0.1"
and a JSON configuration as explained in [documentation][2].

However, since the module has not yet been submitted to the official vert.x
repository, you have to specify to use my repo fork when running your vertx verticles.

You can do this adding the option `-repo http://github.com/parroit` when
you run vertx.


###2) Start play with it

To start working with the module, you can see the examples source code






[1]: http://vertx.io/core_manual_python.html#deploying-a-module-programmatically
[2]: /documentation.html
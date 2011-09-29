Puppet Redis Module
===================

Module for configuring Redis.

Tested on Debian GNU/Linux 6.0 Squeeze and Ubuntu 10.4 LTS with
Puppet 2.6. Patches for other operating systems welcome.


TODO
----

* Ability to configure snapshotting intervals.
* Ability to configure the slow log.


Installation
------------

Clone this repo to a redis directory under your Puppet
modules directory:

    git clone git://github.com/uggedal/puppet-module-redis.git redis

If you don't have a Puppet Master you can create a manifest file
based on the notes below and run Puppet in stand-alone mode
providing the module directory you cloned this repo to:

    puppet apply --modulepath=modules test_redis.pp


Usage
-----

To install and configure Redis, include the module:

    include redis::server

Note that you'll need to define a global search path for the `exec`
resource to make the `redis::server` class function properly. This
should ideally be placed in `manifests/site.pp`:

    Exec {
      path => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
    }

You'll also need to install some build dependencies:

    include redis::dependencies
    package { $redis::dependencies::packages:
      ensure => present,
    }

You can override defaults in the Redis config by including
the module with this special syntax:

    class { "redis::server":
      version => "2.4.0",
      bind => "178.79.120.100",
      port => 6379,
      requirepass => "MY_SUPER_SECRET_PASSWORD",
    }

You can also configure a slave which connects to another Redis master
instance:

    class { "redis::server":
      bind => "127.0.0.1",
      port => 6379,
      masterip => "178.79.120.100",
      masterport => 6379,
      masterauth => "MY_SUPER_SECRET_PASSWORD",
    }

By default Redis saves the database to disk through snapshotting. You can
enable AOF in stead:

    class { "redis::server":
      aof => true,
    }

newseasons
==========

A simple Clojure webapp that notifies users when new seasons of their favorite
TV shows hit iTunes.

Requirements
------------

* [Vagrant](http://vagrantup.com/)

Usage
-----

Clone down the repo:

    hg clone http://bitbucket.org/sjl/newseasons
    # or
    git clone http://github.com/sjl/newseasons

    cd newseasons

Spin up a VM, SSH in, and set up the environment:

    vagrant up

    vagrant ssh
    cd /vagrant

    cp src/newseasons/settings-vagrant.clj src/newseasons/settings.clj
    lein plugin install lein-noir 1.2.0
    lein deps

Run the web server (while SSH'ed in):

    lein run

Visit <http://localhost:4565/> on your local machine to view the site.

To run the check-for-new-seasons/notify-users loop:

    lein run :refresh

License
-------

Copyright (C) 2011 Steve Losh, MIT Licensed


Exec {
  path => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
}

class ubuntu {
  group { "puppet": ensure => "present"; } ->
  group { "vagrant": ensure => "present"; } ->
  user { "vagrant": ensure => "present"; } ->
  file { "/home/vagrant/bin":
      ensure  => "directory",
      owner   => "vagrant",
      group   => "vagrant",
      mode    => "755",
  }

  class { "java": }
  class { "leiningen": }
  class { "environ": }

  $niceties = ["htop", "dtach", "sudo", "vim", "rlwrap"]
  package { $niceties: ensure => "installed" }
}

class clojurebox {
  class { "ubuntu": }

  include redis::dependencies
  package { $redis::dependencies::packages:
    ensure => present,
  }
  class { "redis::server":
    version => "2.4.0",
    bind => "127.0.0.1",
    port => 6379,
    requirepass => "devpass",
    aof => true,
  }
}

class { "clojurebox": }


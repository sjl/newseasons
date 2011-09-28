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

  $niceties = [ "htop", "dtach", "sudo", "vim" ]
  package { $niceties: ensure => "installed" }
}

class { "ubuntu": }


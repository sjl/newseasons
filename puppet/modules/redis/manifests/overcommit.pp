class redis::overcommit($ensure=present) {

  file { "/etc/sysctl.d/overcommit.conf":
    ensure => $ensure,
    content => "vm.overcommit_memory=1",
  }

  if $ensure == "present" {
    exec { "overcommit-memory":
      command => "sysctl vm.overcommit_memory=1",
      unless => "test `sysctl -n vm.overcommit_memory` = 1",
    }
  }
}

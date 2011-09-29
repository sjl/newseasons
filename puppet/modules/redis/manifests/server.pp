class redis::server($ensure=present,
                    $version='2.3.9',
                    $tar_version="2.4.0-rc6",
                    $bind="127.0.0.1",
                    $port=6379,
                    $masterip="",
                    $masterport=6379,
                    $masterauth="",
                    $requirepass="",
                    $aof=false,
                    $aof_auto_rewrite_percentage=100,
                    $aof_auto_rewrite_min_size="64mb") {

  $is_present = $ensure == "present"
  $is_absent = $ensure == "absent"
  $bin_dir = '/usr/local/bin'
  $redis_home = "/var/lib/redis"
  $redis_log = "/var/log/redis"

  class { "redis::overcommit":
    ensure => $ensure,
  }

  redis::install { $version:
    ensure => $ensure,
    bin_dir => $bin_dir,
    tar_version => $tar_version,
  }

  file { "/etc/redis":
    ensure => $ensure ? {
      'present' => "directory",
      default => $ensure,
    },
    force => $is_absent,
    before => $ensure ? {
      'present' => File["/etc/redis/redis.conf"],
      default => undef,
    },
    require => $ensure ? {
      'absent' => File["/etc/redis/redis.conf"],
      default => undef,
    },
  }

  file { "/etc/redis/redis.conf":
    ensure => $ensure,
    content => template("redis/redis.conf.erb"),
    require => Redis::Install[$version],
  }

  group { "redis":
    ensure => $ensure,
    allowdupe => false,
  }

  user { "redis":
    ensure => $ensure,
    allowdupe => false,
    home => $redis_home,
    managehome => true,
    gid => "redis",
    shell => "/bin/false",
    comment => "Redis Server",
    require => $ensure ? {
      'present' => Group["redis"],
      default => undef,
    },
    before => $ensure ? {
      'absent' => Group["redis"],
      default => undef,
    },
  }

  file { [$redis_home, $redis_log]:
    ensure => $ensure ? {
      'present' => directory,
      default => $ensure,
    },
    owner => $ensure ? {
      'present' => "redis",
      default => undef,
    },
    group => $ensure ? {
      'present' => "redis",
      default => undef,
    },
    require => $ensure ? {
      'present' => Group["redis"],
      default => undef,
    },
    before => $ensure ? {
      'absent' => Group["redis"],
      default => undef,
    },
    force => $is_absent,
  }

  file { "/etc/init.d/redis-server":
    ensure => $ensure,
    source => "puppet:///modules/redis/redis-server.init",
    mode => 744,
  }

  file { "/etc/logrotate.d/redis-server":
    ensure => $ensure,
    source => "puppet:///modules/redis/redis-server.logrotate",
  }

  service { "redis-server":
    ensure => $is_present,
    enable => $is_present,
    pattern => "${bin_dir}/redis-server",
    hasrestart => true,
    subscribe => $ensure ? {
      'present' => [File["/etc/init.d/redis-server"],
                    File["/etc/redis/redis.conf"],
                    Redis::Install[$version],
                    Class["redis::overcommit"]],
      default => undef,
    },
    require => $ensure ? {
      'present' => [User["redis"], File["/etc/init.d/redis-server"]],
      default => undef,
    },
    before => $ensure ? {
      'absent' => [User["redis"], File["/etc/init.d/redis-server"]],
      default => undef,
    },
  }
}
